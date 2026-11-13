"""Item c): wall-clock of the whole simulation for the three schemes, and the hybrid vs R_s.

The engine measures the wall-clock around its loop (System.nanoTime, elapsed_ns in params.txt):
the one measure only it can take [DESIGN.md, 27]. One engine at a time on the same machine,
`warmup` unmeasured runs in the same JVM before the measured ones, and a large dt2 so writing
does not weigh. Runs that do not finish ran until the cap: their wall-clock is not comparable,
so they are reported and left out of the means.

Figures (figures/c/): wall-clock and <t_evac> vs R_s, with pure TSDMD and pure EDMD as reference
bands, the time-averaged f_TS vs R_s, and the wall-clock of the three schemes at the default R_s.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Final

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from simulation.constants import DT, N, REALIZATIONS, ZONE_RADIUS, cap_for
from simulation.engine import (
    EVENT_DRIVEN,
    HYBRID,
    SCHEMES,
    TIME_STEP,
    Case,
    EngineRun,
    dump_every_for,
)
from simulation.errors import ConfigError
from simulation.observables import summarize_run, time_averaged_ts_fraction
from simulation.options import option
from simulation.plotting import (
    AXIS_EVACUATION,
    AXIS_TS_FRACTION_AVERAGE,
    AXIS_WALL_CLOCK,
    AXIS_ZONE_RADIUS,
    HYBRID_COLOR,
    SCHEME_COLORS,
    SCHEME_LABELS,
    SCHEME_MARKERS,
    mean_errorbar,
    partial_legend,
)
from simulation.stats import summarize
from simulation.studies.base import Study, StudyContext, summary_rows

DEFAULT_RADII: Final = (0.0, 1.0, 2.0, 3.0, 5.0, 8.0, 12.0, 20.0, 35.0, 56.0)
"""DECISIONS.md, ★22: R_s = 56 m covers the whole room (pure TSDMD inside the hybrid)."""


@dataclass(frozen=True)
class RuntimeConfig:
    n: int = field(default=N, metadata=option("Número de agentes N"))
    zone_radii: tuple[float, ...] = field(
        default=DEFAULT_RADII, metadata=option("Híbrido: valores de R_s (m)")
    )
    default_radius: float = field(
        default=ZONE_RADIUS, metadata=option("R_s del híbrido en la comparación de esquemas (m)")
    )
    dt: float = field(default=DT, metadata=option("Paso dt (s)"))
    dt2: float = field(default=5.0, metadata=option("Intervalo de escritura dt2 (s), grande"))
    realizations: int = field(default=REALIZATIONS, metadata=option("Realizaciones M"))
    warmup: int = field(default=2, metadata=option("Corridas de calentamiento por caso"))
    seed: int = field(default=1, metadata=option("Semilla de la primera realización"))
    max_time: float = field(
        default=cap_for(N), metadata=option("Tope de seguridad del tiempo simulado (s)")
    )

    def __post_init__(self) -> None:
        if self.realizations < 2:
            raise ConfigError("Hacen falta al menos 2 realizaciones para el desvío")
        if self.default_radius not in self.zone_radii:
            raise ConfigError("El R_s de la comparación tiene que estar entre los barridos")
        dump_every_for(self.dt, self.dt2)


def _quick() -> RuntimeConfig:
    return RuntimeConfig(
        zone_radii=(0.0, 5.0, 56.0), realizations=2, warmup=0, max_time=300.0
    )


def _name(scheme: str, radius: float | None) -> str:
    return scheme if radius is None else f"{scheme}_Rs={radius:g}"


def _cases(config: RuntimeConfig, context: StudyContext) -> list[Case]:
    def run(scheme: str, radius: float | None) -> Case:
        return context.case(
            _name(scheme, radius),
            EngineRun(
                scheme=scheme,
                dt=config.dt,
                dump_every=dump_every_for(config.dt, config.dt2),
                n=config.n,
                max_time=config.max_time,
                repeats=config.realizations,
                seed=config.seed,
                warmup=config.warmup,
                zone_radius=radius,
            ),
        )

    pure = [run(TIME_STEP, None), run(EVENT_DRIVEN, None)]
    return pure + [run(HYBRID, radius) for radius in config.zone_radii]


def _analyze(config: RuntimeConfig, context: StudyContext) -> None:
    rows, per_run = [], []
    for case, runs in context.load(_cases(config, context)):
        summaries = [summarize_run(run) for run in runs]
        context.report_incomplete(case.name, summaries)
        complete = [s for s in summaries if s.complete]
        wall = summarize(s.elapsed for s in complete)
        evacuation = summarize(s.evacuation_time for s in complete)
        fraction = summarize(time_averaged_ts_fraction(run) for run in runs)
        radius = case.run.zone_radius
        rows.append(
            {
                "case": case.name,
                "scheme": case.run.scheme,
                "zone_radius": np.nan if radius is None else radius,
                "realizations": len(runs),
                "complete": len(complete),
                "wall_clock": wall.mean,
                "wall_clock_std": wall.std,
                "evacuation_time": evacuation.mean,
                "evacuation_time_std": evacuation.std,
                "ts_fraction_mean": fraction.mean,
                "ts_fraction_std": fraction.std,
            }
        )
        per_run += summary_rows(case.name, summaries, scheme=case.run.scheme, zone_radius=radius)
        context.report(
            f"  {case.name}: wall-clock = {wall} s, <t_evac> = {evacuation} s, "
            f"<f_TS> = {fraction} ({len(complete)}/{len(runs)} completas)"
        )
    table = pd.DataFrame(rows)
    context.save_table(table, "runtime")
    context.save_table(pd.DataFrame(per_run), "runs")

    hybrid = table[table["scheme"] == HYBRID].sort_values("zone_radius")
    pure = {s: table[table["scheme"] == s].iloc[0] for s in (TIME_STEP, EVENT_DRIVEN)
            if (table["scheme"] == s).any()}  # fmt: skip
    _plot_vs_radius(hybrid, pure, "wall_clock", AXIS_WALL_CLOCK, "wall_clock_vs_rs", context)
    # Same data without the EDMD band, whose scale flattens the hybrid against TSDMD (professor).
    time_step_only = {s: row for s, row in pure.items() if s == TIME_STEP}
    _plot_vs_radius(
        hybrid, time_step_only, "wall_clock", AXIS_WALL_CLOCK, "wall_clock_vs_rs_no_edmd", context
    )
    _plot_vs_radius(hybrid, pure, "evacuation_time", AXIS_EVACUATION, "evacuation_vs_rs", context)
    _plot_vs_radius(
        hybrid, pure, "ts_fraction_mean", AXIS_TS_FRACTION_AVERAGE, "ts_fraction_vs_rs", context
    )
    _plot_schemes(table, config.default_radius, context)


def _plot_vs_radius(
    hybrid: pd.DataFrame,
    pure: dict[str, pd.Series],
    column: str,
    label: str,
    name: str,
    context: StudyContext,
) -> None:
    """Hybrid vs R_s, with pure TSDMD and pure EDMD as mean ± std bands [Enunciado, ítem c]."""
    fig, ax = plt.subplots()
    std_column = column.replace("_mean", "") + "_std"
    for scheme, row in pure.items():
        if column == "ts_fraction_mean":
            continue  # 1 and 0 by construction
        color = SCHEME_COLORS[scheme]
        low, high = row[column] - row[std_column], row[column] + row[std_column]
        ax.axhspan(low, high, color=color, alpha=0.15, linewidth=0)
        ax.axhline(row[column], color=color, linestyle="--", label=SCHEME_LABELS[scheme])
    # f_TS uses every run; wall-clock and t_evac only the complete ones.
    partial = (hybrid["complete"] < hybrid["realizations"]) & (column != "ts_fraction_mean")
    mean_errorbar(
        ax, hybrid["zone_radius"], hybrid[column], hybrid[std_column], partial,
        color=HYBRID_COLOR, marker=SCHEME_MARKERS[HYBRID], label=SCHEME_LABELS[HYBRID],
    )  # fmt: skip
    ax.set_xlabel(AXIS_ZONE_RADIUS)
    ax.set_ylabel(label)
    if column == "ts_fraction_mean":
        ax.set_ylim(0, 1.05)
        partial_legend(ax, bool(partial.any()), loc="best")
    else:  # room above the reference bands, so the legend never covers a line
        low, high = ax.get_ylim()
        ax.set_ylim(low, high + 0.5 * (high - low))
        partial_legend(ax, bool(partial.any()), loc="upper center", ncols=2)
    context.save_figure(fig, name)


def _plot_schemes(table: pd.DataFrame, radius: float, context: StudyContext) -> None:
    """Wall-clock of the three schemes, the hybrid at its default R_s."""
    chosen = [
        table[(table["scheme"] == scheme)
              & ((scheme != HYBRID) | (table["zone_radius"] == radius))]
        for scheme in SCHEMES
    ]  # fmt: skip
    rows = [frame.iloc[0] for frame in chosen if not frame.empty]
    fig, ax = plt.subplots()
    partial = [row["complete"] < row["realizations"] for row in rows]
    for position, (row, hollow) in enumerate(zip(rows, partial, strict=True)):
        mean_errorbar(
            ax, [position], [row["wall_clock"]], [row["wall_clock_std"]], [hollow],
            linestyle="none", marker=SCHEME_MARKERS[row["scheme"]],
            color=SCHEME_COLORS[row["scheme"]],
        )  # fmt: skip
    ax.set_xticks(range(len(rows)), [SCHEME_LABELS[row["scheme"]] for row in rows])
    ax.set_xlim(-0.5, len(rows) - 0.5)
    ax.set_ylim(bottom=0)
    ax.set_ylabel(AXIS_WALL_CLOCK)
    partial_legend(ax, any(partial), loc="best")
    context.save_figure(fig, "wall_clock_by_scheme")


STUDY = Study(
    key="runtime",
    item="c",
    title="c) Tiempo de ejecución de los tres esquemas y barrido de R_s del híbrido",
    config_type=RuntimeConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
    sequential=True,
)
