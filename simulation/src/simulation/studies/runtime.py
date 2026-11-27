"""Item c): wall-clock of the whole simulation for the three schemes, and the hybrid vs t_s.

The engine measures the wall-clock around its loop (System.nanoTime, elapsed_ns in params.txt):
the one measure only it can take [DESIGN.md, 27]. One engine at a time on the same machine,
`warmup` unmeasured runs in the same JVM before the measured ones, and a large dt2 so writing
does not weigh. Runs that do not finish ran until the cap: their wall-clock and their time-averaged f_TS (1 until
the cap) are not comparable, so they are reported and left out of every mean.

The main parameter of the hybrid is t_s, the threshold of its only criterion. The sweep runs the
engine default (0.3 s, about 5 pi/omega) plus the values below, pi/omega among them; the x of every
point is the t_s the engine wrote in params.txt. t_s = 0 is pure EDMD, already a reference, so the sweep starts above it (log axis).

Figures (figures/c/): wall-clock and <t_evac> vs t_s, with pure TSDMD and pure EDMD as reference
bands and the default t_s marked, the time-averaged f_TS vs t_s, and the wall-clock of the three
schemes at the default t_s.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Final

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from simulation.constants import CONTACT_DURATION, DT, N, REALIZATIONS, cap_for
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
    AXIS_FLIGHT_THRESHOLD,
    AXIS_TS_FRACTION_AVERAGE,
    AXIS_WALL_CLOCK,
    HYBRID_COLOR,
    SCHEME_COLORS,
    SCHEME_LABELS,
    SCHEME_MARKERS,
    mean_errorbar,
    partial_legend,
)
from simulation.stats import summarize
from simulation.studies.base import Study, StudyContext, summary_rows

DEFAULT_THRESHOLDS: Final = (0.001, 0.003, 0.01, 0.03, CONTACT_DURATION, 0.1, 1.0, 3.0)
"""From dt to 3 s, log-spaced, with pi/omega = 0.057 s (the former default); the engine default,
0.3 s, is run besides them."""


@dataclass(frozen=True)
class RuntimeConfig:
    n: int = field(default=N, metadata=option("Número de agentes N"))
    flight_thresholds: tuple[float, ...] = field(
        default=DEFAULT_THRESHOLDS,
        metadata=option("Híbrido: valores de t_s (s), además del de por defecto (0.3 s)"),
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
        if any(threshold <= 0 for threshold in self.flight_thresholds):
            raise ConfigError("t_s > 0: t_s = 0 es EDMD puro, que ya se corre aparte")
        dump_every_for(self.dt, self.dt2)


def _quick() -> RuntimeConfig:
    return RuntimeConfig(
        flight_thresholds=(0.01, 1.0), realizations=2, warmup=0, max_time=300.0
    )


def _name(scheme: str, threshold: float | None) -> str:
    """'hybrid_ts=default' for the engine default; the pure schemes have no t_s."""
    if scheme != HYBRID:
        return scheme
    return f"{scheme}_ts={'default' if threshold is None else f'{threshold:g}'}"


def _cases(config: RuntimeConfig, context: StudyContext) -> list[Case]:
    def run(scheme: str, threshold: float | None) -> Case:
        return context.case(
            _name(scheme, threshold),
            EngineRun(
                scheme=scheme,
                dt=config.dt,
                dump_every=dump_every_for(config.dt, config.dt2),
                n=config.n,
                max_time=config.max_time,
                repeats=config.realizations,
                seed=config.seed,
                warmup=config.warmup,
                flight_threshold=threshold,
            ),
        )

    fixed = [run(TIME_STEP, None), run(EVENT_DRIVEN, None), run(HYBRID, None)]
    return fixed + [run(HYBRID, threshold) for threshold in config.flight_thresholds]


def _analyze(config: RuntimeConfig, context: StudyContext) -> None:
    rows, per_run = [], []
    for case, runs in context.load(_cases(config, context)):
        summaries = [summarize_run(run) for run in runs]
        context.report_incomplete(case.name, summaries)
        complete = [s for s in summaries if s.complete]
        wall = summarize(s.elapsed for s in complete)
        evacuation = summarize(s.evacuation_time for s in complete)
        # A jammed run sits at f_TS = 1 until the cap: only the complete runs, as every other mean.
        fraction = summarize(
            time_averaged_ts_fraction(run)
            for run, s in zip(runs, summaries, strict=True)
            if s.complete
        )
        hybrid = case.run.scheme == HYBRID
        threshold = runs[0].params.number("ts") if hybrid and runs else np.nan
        rows.append(
            {
                "case": case.name,
                "scheme": case.run.scheme,
                "flight_threshold": threshold,
                "default": hybrid and case.run.flight_threshold is None,
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
        per_run += summary_rows(
            case.name, summaries, scheme=case.run.scheme, flight_threshold=threshold
        )
        context.report(
            f"  {case.name}: wall-clock = {wall} s, <t_evac> = {evacuation} s, "
            f"<f_TS> = {fraction} ({len(complete)}/{len(runs)} completas)"
        )
    table = pd.DataFrame(rows)
    context.save_table(table, "runtime")
    context.save_table(pd.DataFrame(per_run), "runs")

    hybrid = table[table["scheme"] == HYBRID].sort_values("flight_threshold")
    pure = {s: table[table["scheme"] == s].iloc[0] for s in (TIME_STEP, EVENT_DRIVEN)
            if (table["scheme"] == s).any()}  # fmt: skip
    _plot_vs_threshold(hybrid, pure, "wall_clock", AXIS_WALL_CLOCK, "wall_clock_vs_ts", context)
    # Same data without the EDMD band, whose scale flattens the hybrid against TSDMD (professor).
    time_step_only = {s: row for s, row in pure.items() if s == TIME_STEP}
    _plot_vs_threshold(
        hybrid, time_step_only, "wall_clock", AXIS_WALL_CLOCK, "wall_clock_vs_ts_no_edmd", context
    )
    _plot_vs_threshold(
        hybrid, pure, "evacuation_time", AXIS_EVACUATION, "evacuation_vs_ts", context
    )
    _plot_vs_threshold(
        hybrid, pure, "ts_fraction_mean", AXIS_TS_FRACTION_AVERAGE, "ts_fraction_vs_ts", context
    )
    _plot_schemes(table, context)


def _plot_vs_threshold(
    hybrid: pd.DataFrame,
    pure: dict[str, pd.Series],
    column: str,
    label: str,
    name: str,
    context: StudyContext,
) -> None:
    """Hybrid vs t_s (log axis), with pure TSDMD and pure EDMD as mean ± std bands [Enunciado, ítem c]."""
    fig, ax = plt.subplots()
    std_column = column.replace("_mean", "") + "_std"
    for scheme, row in pure.items():
        if column == "ts_fraction_mean":
            continue  # 1 and 0 by construction
        color = SCHEME_COLORS[scheme]
        low, high = row[column] - row[std_column], row[column] + row[std_column]
        ax.axhspan(low, high, color=color, alpha=0.15, linewidth=0)
        ax.axhline(row[column], color=color, linestyle="--", label=SCHEME_LABELS[scheme])
    # Every mean uses the complete runs only: a hollow marker where some run got jammed.
    partial = hybrid["complete"] < hybrid["realizations"]
    mean_errorbar(
        ax, hybrid["flight_threshold"], hybrid[column], hybrid[std_column], partial,
        color=HYBRID_COLOR, marker=SCHEME_MARKERS[HYBRID], label=SCHEME_LABELS[HYBRID],
    )  # fmt: skip
    for chosen in hybrid.loc[hybrid["default"], "flight_threshold"]:  # the default t_s, as the chosen dt
        ax.axvline(chosen, color="gray", linestyle=":", label=r"$t_s$ elegido")
    ax.set_xscale("log")
    ax.set_xlabel(AXIS_FLIGHT_THRESHOLD)
    ax.set_ylabel(label)
    if column == "ts_fraction_mean":
        ax.set_ylim(0, 1.05)
        partial_legend(ax, bool(partial.any()), loc="best")
    else:  # room above the reference bands, so the legend never covers a line
        low, high = ax.get_ylim()
        ax.set_ylim(low, high + 0.5 * (high - low))
        partial_legend(ax, bool(partial.any()), loc="upper center", ncols=2)
    context.save_figure(fig, name)


def _plot_schemes(table: pd.DataFrame, context: StudyContext) -> None:
    """Wall-clock of the three schemes, the hybrid at its default t_s."""
    chosen = [
        table[(table["scheme"] == scheme) & ((scheme != HYBRID) | table["default"])]
        for scheme in SCHEMES
    ]
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
    title="c) Tiempo de ejecución de los tres esquemas y barrido de t_s del híbrido",
    config_type=RuntimeConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
    sequential=True,
)
