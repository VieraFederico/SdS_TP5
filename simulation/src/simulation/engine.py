"""Compiles and invokes the Java engine (experiment/src). Python never integrates physics."""

from __future__ import annotations

import json
import os
import shutil
import subprocess
import threading
import time
from collections.abc import Callable, Sequence
from concurrent.futures import ThreadPoolExecutor, as_completed
from dataclasses import asdict, dataclass
from enum import Enum
from pathlib import Path
from typing import Final

from simulation.errors import ConfigError, EngineError
from simulation.paths import CHECK_CLASSES, ENGINE_CHECKS, ENGINE_CLASSES, ENGINE_SOURCES
from simulation.reader import RUN_GLOB, is_finished_run

TIME_STEP: Final = "time-step"
EVENT_DRIVEN: Final = "event-driven"
HYBRID: Final = "hybrid"
SCHEMES: Final = (TIME_STEP, EVENT_DRIVEN, HYBRID)
CASE_FILE: Final = "case.json"
JAVA_RELEASE: Final = "21"
CHECKS_CLASS: Final = "EngineChecks"
_BUILD_STAMP: Final = ".build-stamp"
_compile_lock = threading.Lock()

Reporter = Callable[[str], None]


def dump_every_for(dt: float, dt2: float) -> int:
    """Steps between saved frames; dt2 must be an integer number of dt [Teorica_5, diapo 20]."""
    if dt <= 0 or dt2 <= 0:
        raise ConfigError(f"dt={dt} y dt2={dt2} deben ser positivos")
    ratio = dt2 / dt
    rounded = round(ratio)
    if rounded < 1 or abs(ratio - rounded) > 1e-6 * ratio:
        raise ConfigError(f"dt2={dt2} no es un múltiplo entero de dt={dt}")
    return rounded


@dataclass(frozen=True)
class EngineRun:
    """One engine invocation: `repeats` realizations with consecutive seeds.

    `None` keeps the engine default (RunConfig.java), so case.json only pins what a study sets.
    """

    scheme: str
    dt: float
    dump_every: int
    n: int = 200
    side: float = 50.0
    max_time: float = 1e5
    repeats: int = 1
    seed: int = 1
    warmup: int = 0
    zone_radius: float | None = None
    flight_threshold: float | None = None
    v_max: float | None = None
    """Speed bound of the skin guarantee (hybrid only); None keeps the engine default, 5 m/s."""

    def __post_init__(self) -> None:
        if self.scheme not in SCHEMES:
            raise ConfigError(f"Esquema desconocido: {self.scheme}")
        if self.dt <= 0 or self.max_time <= 0 or self.side <= 0:
            raise ConfigError("dt, max_time y L deben ser positivos")
        if self.n < 1 or self.dump_every < 1 or self.repeats < 1 or self.warmup < 0:
            raise ConfigError("N, dump_every y repeats deben ser enteros positivos")
        hybrid_only = (self.zone_radius, self.flight_threshold, self.v_max)
        if self.scheme != HYBRID and any(value is not None for value in hybrid_only):
            raise ConfigError("R_s, t_s y v_max son parámetros del esquema híbrido")

    @property
    def dt2(self) -> float:
        return self.dt * self.dump_every

    def arguments(self, out: Path) -> list[str]:
        args = [
            "--scheme", self.scheme,
            "--N", str(self.n),
            "--L", repr(self.side),
            "--dt", repr(self.dt),
            "--dump-every", str(self.dump_every),
            "--max-time", repr(self.max_time),
            "--repeats", str(self.repeats),
            "--warmup", str(self.warmup),
            "--seed", str(self.seed),
            "--out", str(out),
        ]  # fmt: skip
        if self.zone_radius is not None:
            args += ["--Rs", repr(self.zone_radius)]
        if self.flight_threshold is not None:
            args += ["--ts", repr(self.flight_threshold)]
        if self.v_max is not None:
            args += ["--v-max", repr(self.v_max)]
        return args


@dataclass(frozen=True)
class Case:
    """A named engine invocation whose realizations live in `directory/run-NNN`."""

    name: str
    run: EngineRun
    directory: Path


class CaseStatus(Enum):
    DONE = "done"
    MISSING = "missing"
    PARTIAL = "partial"
    STALE = "stale"


def case_status(case: Case) -> CaseStatus:
    """DONE when every realization finished, complete or cut: a jam is a result, not a failure."""
    if not case.directory.exists():
        return CaseStatus.MISSING
    record = case.directory / CASE_FILE
    if not record.is_file():
        return CaseStatus.PARTIAL
    if not _same_run(json.loads(record.read_text(encoding="utf-8")), asdict(case.run)):
        return CaseStatus.STALE
    runs = sorted(case.directory.glob(RUN_GLOB))
    if len(runs) != case.run.repeats or not all(is_finished_run(run) for run in runs):
        return CaseStatus.PARTIAL
    return CaseStatus.DONE


def _same_run(saved: dict, expected: dict) -> bool:
    """A field added after the case was simulated counts as its default (None) if absent."""
    return {key: saved.get(key) for key in expected} == expected and set(saved) <= set(expected)


def compile_engine(*, force: bool = False) -> Path:
    """Compiles experiment/src into experiment/out/classes when any source is newer."""
    with _compile_lock:
        sources = sorted(ENGINE_SOURCES.rglob("*.java"))
        if not sources:
            raise EngineError(f"No hay fuentes Java en {ENGINE_SOURCES}")
        stamp = ENGINE_CLASSES / _BUILD_STAMP
        newest = max(source.stat().st_mtime for source in sources)
        if not force and stamp.is_file() and stamp.stat().st_mtime >= newest:
            return ENGINE_CLASSES
        if ENGINE_CLASSES.exists():
            shutil.rmtree(ENGINE_CLASSES)
        ENGINE_CLASSES.mkdir(parents=True)
        command = [_jdk_tool("javac"), "--release", JAVA_RELEASE, "-encoding", "UTF-8"]
        command += ["-d", str(ENGINE_CLASSES), *map(str, sources)]
        result = subprocess.run(command, capture_output=True, text=True)
        if result.returncode:
            raise EngineError(f"javac falló:\n{result.stderr.strip()}")
        stamp.touch()
        return ENGINE_CLASSES


def run_engine_checks(report: Reporter = print) -> None:
    """Compiles engine + experiment/checks into a separate folder and runs EngineChecks."""
    sources = sorted(ENGINE_SOURCES.rglob("*.java")) + sorted(ENGINE_CHECKS.rglob("*.java"))
    if CHECK_CLASSES.exists():
        shutil.rmtree(CHECK_CLASSES)
    CHECK_CLASSES.mkdir(parents=True)
    command = [_jdk_tool("javac"), "--release", JAVA_RELEASE, "-encoding", "UTF-8"]
    result = subprocess.run(
        [*command, "-d", str(CHECK_CLASSES), *map(str, sources)], capture_output=True, text=True
    )
    if result.returncode:
        raise EngineError(f"javac falló:\n{result.stderr.strip()}")
    checks = subprocess.run(
        [_jdk_tool("java"), "-cp", str(CHECK_CLASSES), CHECKS_CLASS], capture_output=True, text=True
    )
    for line in checks.stdout.splitlines():
        report(line)
    if checks.returncode:
        raise EngineError(f"{CHECKS_CLASS} falló:\n{checks.stderr.strip()}")


def run_case(case: Case, *, overwrite: bool = False) -> bool:
    """Runs the case unless identical finished results exist. Returns True if it simulated."""
    status = case_status(case)
    if status is CaseStatus.DONE:
        return False
    if status is CaseStatus.STALE and not overwrite:
        raise EngineError(
            f"{case.directory} tiene resultados con otros parámetros. "
            "Usar --overwrite para reemplazarlos o correr en una sesión nueva."
        )
    if status is not CaseStatus.MISSING:
        shutil.rmtree(case.directory)
    classes = compile_engine()
    command = [_jdk_tool("java"), "-cp", str(classes), "Main", *case.run.arguments(case.directory)]
    result = subprocess.run(command, capture_output=True, text=True)
    if result.returncode:
        message = result.stderr.strip() or result.stdout.strip()
        raise EngineError(f"{case.name}: {message}")
    (case.directory / CASE_FILE).write_text(
        json.dumps(asdict(case.run), indent=2), encoding="utf-8"
    )
    return True


def run_cases(
    cases: Sequence[Case], *, jobs: int, overwrite: bool = False, report: Reporter = print
) -> None:
    """Runs every case, `jobs` engine processes at a time; failures are collected, then raised."""
    if not cases:
        return
    compile_engine()
    failures: list[str] = []
    total = len(cases)
    with ThreadPoolExecutor(max_workers=max(1, jobs)) as pool:
        futures = {pool.submit(_timed_run, case, overwrite): case for case in cases}
        for done, future in enumerate(as_completed(futures), start=1):
            case = futures[future]
            try:
                simulated, seconds = future.result()
            except EngineError as error:
                failures.append(str(error))
                report(f"  [{done}/{total}] {case.name}: ERROR")
                continue
            detail = f"{seconds:.1f} s" if simulated else "ya existía"
            report(f"  [{done}/{total}] {case.name}: {detail}")
    if failures:
        raise EngineError(f"{len(failures)} caso(s) fallaron:\n  " + "\n  ".join(failures))


def default_jobs() -> int:
    return max(1, (os.cpu_count() or 2) - 1)


def _timed_run(case: Case, overwrite: bool) -> tuple[bool, float]:
    started = time.perf_counter()
    simulated = run_case(case, overwrite=overwrite)
    return simulated, time.perf_counter() - started


def _jdk_tool(name: str) -> str:
    java_home = os.environ.get("JAVA_HOME")
    if java_home:
        for candidate in (Path(java_home) / "bin" / name, Path(java_home) / "bin" / f"{name}.exe"):
            if candidate.is_file():
                return str(candidate)
    found = shutil.which(name)
    if found is None:
        raise EngineError(
            f"No se encontró '{name}'. Instalar un JDK {JAVA_RELEASE}+ o definir JAVA_HOME."
        )
    return found
