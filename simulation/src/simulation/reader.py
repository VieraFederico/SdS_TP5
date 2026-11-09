"""Readers for one engine realization directory (run-NNN). See DESIGN.md, section 4.

    static.txt        dynamic.txt                         exits.txt              params.txt
    N                 t                                   id t_door t_retire     key value
    L                 id x y vx vy scheme location        ...                    ...
    d                 ... (always the N rows)
    r1 c1             t
    ...               ...
    rN cN

static.txt and dynamic.txt follow Teorica_1, diapos 36-37; dynamic.txt carries the whole state of
every agent at every dt2 [Correcciones_TP4, Código]. scheme: 0 EDMD, 1 TSDMD. location: 0 inside,
1 past the door, 2 retired. exits.txt has NaN for an agent that never crossed or retired.
"""

from __future__ import annotations

import math
from collections.abc import Mapping
from dataclasses import dataclass
from functools import cached_property
from pathlib import Path
from typing import Final

import numpy as np
import pandas as pd
from numpy.typing import NDArray

from simulation.errors import OutputFormatError

STATIC_FILE: Final = "static.txt"
DYNAMIC_FILE: Final = "dynamic.txt"
EXITS_FILE: Final = "exits.txt"
PARAMS_FILE: Final = "params.txt"
RUN_GLOB: Final = "run-[0-9][0-9][0-9]"

EVENT_DRIVEN: Final = 0
TIME_STEP: Final = 1
INSIDE: Final = 0
PASSED_DOOR: Final = 1
RETIRED: Final = 2

FloatArray = NDArray[np.float64]
IntArray = NDArray[np.int8]


@dataclass(frozen=True)
class StaticData:
    side: float
    door: float
    radii: FloatArray
    schemes: IntArray
    """Initial scheme of each agent (the colour column of Teorica_1, diapo 37)."""

    @property
    def n(self) -> int:
        return len(self.radii)


@dataclass(frozen=True)
class Trajectory:
    """Saved frames: times (F,), kinematics (F, N, 4) = x, y, vx, vy, schemes and locations (F, N)."""

    times: FloatArray
    states: FloatArray
    schemes: IntArray
    locations: IntArray

    @property
    def positions(self) -> FloatArray:
        return self.states[:, :, 0:2]

    @property
    def velocities(self) -> FloatArray:
        return self.states[:, :, 2:4]

    def nearest_frame(self, time: float) -> int:
        return int(np.argmin(np.abs(self.times - time)))


@dataclass(frozen=True)
class Exits:
    """Per agent, the instant it crossed the door (y = 0) and the instant it retired (y = -1 m)."""

    door: FloatArray
    retire: FloatArray


@dataclass(frozen=True)
class RunParams:
    values: Mapping[str, str]

    def text(self, key: str) -> str:
        try:
            return self.values[key]
        except KeyError as error:
            raise OutputFormatError(f"{PARAMS_FILE} no tiene la clave '{key}'") from error

    def number(self, key: str) -> float:
        try:
            return float(self.text(key))
        except ValueError as error:
            raise OutputFormatError(f"{PARAMS_FILE}: '{key}' no es numérico") from error

    @property
    def scheme(self) -> str:
        return self.text("scheme")

    @property
    def complete(self) -> bool:
        return self.text("complete") == "true"

    @property
    def elapsed_seconds(self) -> float:
        """Wall-clock of the loop, measured by the engine: the one exception [DESIGN.md, 27]."""
        return self.number("elapsed_ns") * 1e-9

    @property
    def n(self) -> int:
        return int(self.number("N"))

    @property
    def dt(self) -> float:
        return self.number("dt")

    @property
    def dt2(self) -> float:
        return self.number("dt2")

    @property
    def max_time(self) -> float:
        return self.number("max_time")


def read_static(path: Path) -> StaticData:
    lines = _lines(path)
    try:
        n = int(lines[0])
        side = float(lines[1])
        door = float(lines[2])
        rows = [line.split() for line in lines[3:]]
        radii = np.array([float(row[0]) for row in rows])
        schemes = np.array([int(row[1]) for row in rows], dtype=np.int8)
    except (IndexError, ValueError) as error:
        raise OutputFormatError(f"{path}: se esperaba 'N', 'L', 'd' y filas 'r c'") from error
    if len(rows) != n or any(len(row) != 2 for row in rows):
        raise OutputFormatError(f"{path}: N={n} pero hay {len(rows)} filas 'r c'")
    return StaticData(side=side, door=door, radii=radii, schemes=schemes)


def read_dynamic(path: Path, n: int) -> Trajectory:
    try:
        table = pd.read_csv(path, sep=" ", header=None, names=range(7), dtype=np.float64)
    except (pd.errors.EmptyDataError, pd.errors.ParserError, ValueError) as error:
        raise OutputFormatError(f"{path}: no se pudo leer ({error})") from error
    values = table.to_numpy()
    rows_per_frame = n + 1
    if len(values) == 0 or len(values) % rows_per_frame:
        raise OutputFormatError(
            f"{path}: {len(values)} filas no son múltiplo de N+1={rows_per_frame}"
        )
    frames = values.reshape(-1, rows_per_frame, 7)
    headers = frames[:, 0, :]
    rows = frames[:, 1:, :]
    if np.isnan(headers[:, 0]).any() or not np.isnan(headers[:, 1:]).all():
        raise OutputFormatError(f"{path}: cada cuadro debe empezar con una línea con sólo t")
    if np.isnan(rows).any():
        raise OutputFormatError(f"{path}: cada fila debe tener 'id x y vx vy scheme location'")
    if not (rows[:, :, 0] == np.arange(n)).all():
        raise OutputFormatError(f"{path}: las filas de cada cuadro deben ir de id 0 a N-1")
    return Trajectory(
        times=np.ascontiguousarray(headers[:, 0]),
        states=np.ascontiguousarray(rows[:, :, 1:5]),
        schemes=rows[:, :, 5].astype(np.int8),
        locations=rows[:, :, 6].astype(np.int8),
    )


def read_exits(path: Path, n: int) -> Exits:
    try:
        rows = [line.split() for line in _lines(path)]
        ids = [int(row[0]) for row in rows]
        door = np.array([float(row[1]) for row in rows])
        retire = np.array([float(row[2]) for row in rows])
    except (IndexError, ValueError) as error:
        raise OutputFormatError(f"{path}: se esperaban filas 'id t_door t_retire'") from error
    if ids != list(range(n)):
        raise OutputFormatError(f"{path}: se esperaban los ids 0..{n - 1} en orden")
    return Exits(door=door, retire=retire)


def read_params(path: Path) -> RunParams:
    values: dict[str, str] = {}
    for line in _lines(path):
        parts = line.split()
        if len(parts) != 2:
            raise OutputFormatError(f"{path}: línea mal formada '{line}'")
        values[parts[0]] = parts[1]
    return RunParams(values=values)


def is_finished_run(directory: Path) -> bool:
    """The engine wrote params.txt, its last file: the run ended (complete or cut at the cap)."""
    return (directory / PARAMS_FILE).is_file() and (directory / EXITS_FILE).is_file()


class Run:
    """One realization. Small files load eagerly; dynamic.txt loads on first access."""

    def __init__(self, directory: Path) -> None:
        self.directory = directory
        self.static = read_static(directory / STATIC_FILE)
        self.params = read_params(directory / PARAMS_FILE)
        self.exits = read_exits(directory / EXITS_FILE, self.static.n)

    @cached_property
    def trajectory(self) -> Trajectory:
        return read_dynamic(self.directory / DYNAMIC_FILE, self.static.n)

    @property
    def label(self) -> str:
        return self.directory.name

    @property
    def complete(self) -> bool:
        """Every agent retired before the cap (`max_time` or `max_events`)."""
        return self.params.complete and not np.isnan(self.exits.retire).any()

    @property
    def last_time(self) -> float:
        """Last saved instant: the end of the simulation, complete or not."""
        times = self.trajectory.times
        return float(times[-1]) if len(times) else math.nan

    def __repr__(self) -> str:
        return f"Run({self.directory})"


def find_runs(directory: Path) -> list[Run]:
    return [Run(path) for path in sorted(directory.glob(RUN_GLOB)) if path.is_dir()]


def _lines(path: Path) -> list[str]:
    try:
        text = path.read_text(encoding="utf-8")
    except FileNotFoundError as error:
        raise OutputFormatError(f"No existe {path}") from error
    return [line.strip() for line in text.splitlines() if line.strip()]
