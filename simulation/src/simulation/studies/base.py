"""What every study shares: where it writes, how it runs its cases and loads their runs.

Layout of a study session (output/<items>-<study>/<YYYY-MM-DD_HH-MM-SS>[_quick]/):
    config.json                 parameters of the session
    runs/<case>/run-NNN/        engine outputs (static, dynamic, exits, params) + case.json
    figures/<item>/*.png        figures for the presentation, by item (dt, a, b, c, d)
    *.csv                       observables behind each figure, incomplete runs included
"""

from __future__ import annotations

from collections.abc import Callable, Mapping, Sequence
from dataclasses import dataclass, field
from pathlib import Path
from typing import Any

import pandas as pd
from matplotlib.figure import Figure

from simulation.engine import Case, CaseStatus, EngineRun, case_status, run_cases
from simulation.errors import ConfigError, MissingResultsError
from simulation.observables import RunSummary
from simulation.options import with_overrides
from simulation.plotting import save_figure
from simulation.reader import Run, find_runs
from simulation.sessions import LATEST, create_session, find_session, load_config, save_config

Reporter = Callable[[str], None]
FIGURES_DIR = "figures"


@dataclass(frozen=True)
class StudyContext:
    root: Path
    output: Path
    jobs: int
    item: str
    overwrite: bool = False
    report: Reporter = field(default=print)

    @property
    def runs_dir(self) -> Path:
        return self.output / "runs"

    def case(self, name: str, run: EngineRun) -> Case:
        return Case(name=name, run=run, directory=self.runs_dir / name)

    def save_figure(self, fig: Figure, name: str, *, item: str | None = None) -> Path:
        """<session>/figures/<item>/<name>.png; `item` defaults to the study's own item."""
        path = save_figure(fig, self.output / FIGURES_DIR / (item or self.item) / f"{name}.png")
        self.report(f"  figura: {path}")
        return path

    def save_table(self, table: pd.DataFrame, name: str) -> Path:
        path = self.output / f"{name}.csv"
        path.parent.mkdir(parents=True, exist_ok=True)
        table.to_csv(path, index=False)
        self.report(f"  tabla:  {path}")
        return path

    def load(self, cases: Sequence[Case]) -> list[tuple[Case, list[Run]]]:
        """Runs of every finished case; missing ones are reported. Fails only if none exist."""
        loaded: list[tuple[Case, list[Run]]] = []
        missing: list[str] = []
        for case in cases:
            if case_status(case) is CaseStatus.DONE:
                loaded.append((case, find_runs(case.directory)))
            else:
                missing.append(case.name)
        if missing:
            self.report(f"  AVISO: faltan {len(missing)} caso(s) sin simular: {', '.join(missing)}")
        if not loaded:
            raise MissingResultsError(
                f"No hay resultados en {self.runs_dir}. Simular antes de analizar."
            )
        return loaded

    def report_incomplete(self, case: str, summaries: Sequence[RunSummary]) -> None:
        """Every run with complete = false is listed: jams are a valid result [Consideraciones,
        iii], never dropped in silence. Means use the complete runs and say how many."""
        incomplete = [summary for summary in summaries if not summary.complete]
        total = len(summaries)
        self.report(f"  {case}: {total - len(incomplete)}/{total} corridas completas")
        for summary in incomplete:
            self.report(f"    {summary.describe()}")


def summary_rows(case: str, summaries: Sequence[RunSummary], **extra: Any) -> list[dict[str, Any]]:
    """One CSV row per realization, complete or not, with the case parameters in `extra`."""
    return [
        {
            "case": case,
            **extra,
            "run": summary.label,
            "complete": summary.complete,
            "evacuation_time": summary.evacuation_time,
            "evacuated": summary.evacuated,
            "n": summary.n,
            "last_crossing": summary.last_crossing,
            "flow": summary.flow,
            "cap": summary.cap,
            "elapsed": summary.elapsed,
        }
        for summary in summaries
    ]


def _unchanged(config: Any, root: Path) -> Any:
    return config


@dataclass(frozen=True)
class Study[C]:
    key: str
    item: str
    """Item of the statement (dt, a, b, c, d): names presentation/figuras/<item>/."""
    title: str
    config_type: type[C]
    quick: Callable[[], C]
    cases: Callable[[C, StudyContext], list[Case]]
    analyze: Callable[[C, StudyContext], None]
    sequential: bool = False
    """Runtime measurements must not compete for the CPU: run one engine at a time."""
    resolve: Callable[[C, Path], C] = _unchanged
    """Pins references to other sessions (e.g. `latest`) before the config is saved."""
    items: str | None = None
    """Items the study answers, when more than its figure item (schemes: a and b)."""

    @property
    def folder(self) -> str:
        """output/<items>-<key>, so every folder says which item of the statement it answers."""
        return f"{self.items or self.item}-{self.key}"

    def full(self) -> C:
        return self.config_type()

    def execute(
        self, config: C, context: StudyContext, *, simulate: bool = True, analyze: bool = True
    ) -> None:
        if simulate:
            cases = self.cases(config, context)
            if cases:
                jobs = 1 if self.sequential else context.jobs
                context.report(f"Simulando {len(cases)} caso(s) con {jobs} proceso(s) Java")
                run_cases(cases, jobs=jobs, overwrite=context.overwrite, report=context.report)
        if analyze:
            context.report("Analizando")
            self.analyze(config, context)


def open_session[C](
    study: Study[C],
    root: Path,
    *,
    session: str | None,
    quick: bool,
    overrides: Mapping[str, str],
    jobs: int,
    overwrite: bool = False,
) -> tuple[C, StudyContext]:
    """A new session (`session=None`) or an existing one, whose saved parameters are the base.

    `quick` picks the quick preset for a new session, or restricts `latest` to quick sessions.
    """
    parent = root / study.folder
    if session is None:
        config = study.resolve(
            with_overrides(study.quick() if quick else study.full(), overrides), root
        )
        directory = create_session(parent, quick=quick)
    else:
        if quick and session != LATEST:
            raise ConfigError("--quick no se combina con una sesión con nombre")
        directory = find_session(parent, session, quick=quick or None)
        config, quick = load_config(directory, study.config_type)
        config = study.resolve(with_overrides(config, overrides), root)
    save_config(directory, config, study=study.key, quick=quick)
    context = StudyContext(
        root=root, output=directory, jobs=jobs, item=study.item, overwrite=overwrite
    )
    return config, context
