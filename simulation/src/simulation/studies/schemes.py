"""Items a) and b): the three schemes with the same M seeds, N = 200, dt = 1e-3 s.

a) f_TS(t) = N_TS(t) / N(t) of the hybrid: one realization (fraction in each scheme) and the mean
   over realizations. Figures in figures/a/.
b) Population curves N(t) and t_evac of the three schemes, with pure TSDMD as the reference.
   Figures in figures/b/.

Runs that do not finish are listed per scheme and counted; means use the complete ones. The
animations of item a) are the `animation` study.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from simulation.constants import DT, DT2, N, REALIZATIONS, ZONE_RADIUS, cap_for
from simulation.engine import HYBRID, SCHEMES, TIME_STEP, Case, EngineRun, dump_every_for
from simulation.errors import ConfigError
from simulation.observables import (
    mean_population,
    mean_ts_fraction,
    population_curve,
    scheme_switches,
    summarize_run,
    time_averaged_ts_fraction,
    ts_fraction,
)
from simulation.options import option
from simulation.plotting import (
    AXIS_EVACUATION,
    AXIS_FLOW,
    AXIS_POPULATION,
    AXIS_POPULATION_MEAN,
    AXIS_SCHEME_FRACTION,
    AXIS_TIME,
    AXIS_TS_FRACTION,
    EVENT_DRIVEN_COLOR,
    SCHEME_COLORS,
    SCHEME_LABELS,
    SCHEME_MARKERS,
    TIME_STEP_COLOR,
    mean_errorbar,
    partial_legend,
)
from simulation.reader import Run
from simulation.stats import summarize
from simulation.studies.base import Study, StudyContext, summary_rows


@dataclass(frozen=True)
class SchemesConfig:
    n: int = field(default=N, metadata=option("Número de agentes N"))
    schemes: tuple[str, ...] = field(
        default=SCHEMES, metadata=option("Esquemas", choices=SCHEMES)
    )
    zone_radius: float = field(default=ZONE_RADIUS, metadata=option("Híbrido: R_s (m)"))
    dt: float = field(default=DT, metadata=option("Paso dt (s)"))
    dt2: float = field(default=DT2, metadata=option("Intervalo de escritura dt2 (s)"))
    realizations: int = field(default=REALIZATIONS, metadata=option("Realizaciones M"))
    seed: int = field(default=1, metadata=option("Semilla de la primera realización"))
    max_time: float = field(
        default=cap_for(N), metadata=option("Tope de seguridad del tiempo simulado (s)")
    )

    def __post_init__(self) -> None:
        if self.realizations < 2:
            raise ConfigError("Hacen falta al menos 2 realizaciones para el desvío")
        dump_every_for(self.dt, self.dt2)


def _quick() -> SchemesConfig:
    return SchemesConfig(realizations=2, max_time=300.0)


def _cases(config: SchemesConfig, context: StudyContext) -> list[Case]:
    return [
        context.case(
            scheme,
            EngineRun(
                scheme=scheme,
                dt=config.dt,
                dump_every=dump_every_for(config.dt, config.dt2),
                n=config.n,
                max_time=config.max_time,
                repeats=config.realizations,
                seed=config.seed,
                zone_radius=config.zone_radius if scheme == HYBRID else None,
            ),
        )
        for scheme in config.schemes
    ]


def _analyze(config: SchemesConfig, context: StudyContext) -> None:
    loaded = context.load(_cases(config, context))
    rows, per_run = [], []
    complete: dict[str, list[Run]] = {}
    for case, runs in loaded:
        scheme = case.run.scheme
        summaries = [summarize_run(run) for run in runs]
        context.report_incomplete(SCHEME_LABELS[scheme], summaries)
        evacuation = summarize(s.evacuation_time for s in summaries if s.complete)
        flow = summarize(s.flow for s in summaries)
        fraction = summarize(time_averaged_ts_fraction(run) for run in runs)
        switches = [scheme_switches(run.trajectory) for run in runs]
        to_ts = summarize(float(s[0]) for s in switches)
        to_ed = summarize(float(s[1]) for s in switches)
        rows.append(
            {
                "scheme": scheme,
                "realizations": len(runs),
                "complete": evacuation.count,
                "evacuation_time": evacuation.mean,
                "evacuation_time_std": evacuation.std,
                "flow": flow.mean,
                "flow_std": flow.std,
                "ts_fraction_mean": fraction.mean,
                "ts_fraction_std": fraction.std,
                "switches_to_ts": to_ts.mean,
                "switches_to_ts_std": to_ts.std,
                "switches_to_ed": to_ed.mean,
                "switches_to_ed_std": to_ed.std,
            }
        )
        per_run += summary_rows(case.name, summaries, scheme=scheme)
        complete[scheme] = [r for r, s in zip(runs, summaries, strict=True) if s.complete]
        context.report(
            f"  {SCHEME_LABELS[scheme]}: <t_evac> = {evacuation} s "
            f"({evacuation.count}/{len(runs)}), <Q> = {flow} p/s, <f_TS> = {fraction}"
        )
        if scheme == HYBRID:
            context.report(
                f"    saltos por corrida (cada dt2, cota inferior): EDMD -> TSDMD = {to_ts}, "
                f"TSDMD -> EDMD = {to_ed}"
            )
    table = pd.DataFrame(rows)
    _compare_with_reference(table, context)
    context.save_table(table, "schemes")
    context.save_table(pd.DataFrame(per_run), "runs")

    if HYBRID in complete and complete[HYBRID]:
        _plot_fraction_example(complete[HYBRID][0], context)
        _plot_fraction_mean(complete[HYBRID], context)
    _plot_population_examples(complete, context)
    _plot_population_mean(complete, context)
    _plot_scalar(
        table, "evacuation_time", AXIS_EVACUATION, "evacuation_by_scheme", context,
        only_complete=True,
    )  # fmt: skip
    _plot_scalar(table, "flow", AXIS_FLOW, "flow_by_scheme", context)


def _compare_with_reference(table: pd.DataFrame, context: StudyContext) -> None:
    """Differences with pure TSDMD, in seconds and in units of the combined standard error."""
    reference = table[table["scheme"] == TIME_STEP]
    if reference.empty:
        return
    ref = reference.iloc[0]
    differences, significance = [], []
    for _, row in table.iterrows():
        delta = row["evacuation_time"] - ref["evacuation_time"]
        error = math.hypot(
            row["evacuation_time_std"] / math.sqrt(max(row["complete"], 1)),
            ref["evacuation_time_std"] / math.sqrt(max(ref["complete"], 1)),
        )
        differences.append(delta)
        significance.append(delta / error if error > 0 else math.nan)
        if row["scheme"] != TIME_STEP:
            context.report(
                f"  {SCHEME_LABELS[row['scheme']]} − TSDMD puro: Δt_evac = {delta:+.1f} s "
                f"({delta / error:+.1f} errores estándar)"
            )
    table["delta_vs_time_step"] = differences
    table["delta_in_standard_errors"] = significance


def _plot_fraction_example(run: Run, context: StudyContext) -> None:
    """Fraction of the agents inside in each scheme, one realization [Enunciado, ítem a]."""
    times = run.trajectory.times
    fraction = ts_fraction(run.trajectory)
    fig, ax = plt.subplots()
    ax.plot(times, fraction, color=TIME_STEP_COLOR, label=r"TSDMD, $f_{\mathrm{TS}}(t)$")
    ax.plot(
        times, 1 - fraction, color=EVENT_DRIVEN_COLOR, linestyle="--",
        label=r"EDMD, $1 - f_{\mathrm{TS}}(t)$",
    )  # fmt: skip
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(AXIS_SCHEME_FRACTION)
    ax.set_xlim(left=0)
    ax.set_ylim(0, 1.05)
    ax.legend(loc="center right")
    context.save_figure(fig, "fraction_vs_time", item="a")


def _plot_fraction_mean(runs: list[Run], context: StudyContext) -> None:
    times, mean, std = mean_ts_fraction(runs)
    fig, ax = plt.subplots()
    ax.fill_between(times, mean - std, mean + std, color=TIME_STEP_COLOR, alpha=0.25, linewidth=0)
    ax.plot(times, mean, color=TIME_STEP_COLOR)
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(AXIS_TS_FRACTION)
    ax.set_xlim(left=0)
    ax.set_ylim(0, 1.05)
    context.save_figure(fig, "ts_fraction_mean_vs_time", item="a")


def _plot_population_examples(complete: dict[str, list[Run]], context: StudyContext) -> None:
    """One complete realization per scheme, same seed when possible [Guia 2.4.2]."""
    fig, ax = plt.subplots()
    for scheme, runs in complete.items():
        if not runs:
            continue
        times, counts = population_curve(runs[0])
        ax.step(times, counts, where="post", color=SCHEME_COLORS[scheme],
                label=SCHEME_LABELS[scheme])  # fmt: skip
    _population_axes(ax, AXIS_POPULATION)
    context.save_figure(fig, "population_vs_time", item="b")


def _plot_population_mean(complete: dict[str, list[Run]], context: StudyContext) -> None:
    """Mean N(t) ± std over the complete realizations of each scheme."""
    end = max((max(r.exits.door) for runs in complete.values() for r in runs), default=0.0)
    if end <= 0:
        return
    grid = np.linspace(0.0, end, 1000)
    fig, ax = plt.subplots()
    for scheme, runs in complete.items():
        if not runs:
            continue
        mean, std = mean_population(runs, grid)
        color = SCHEME_COLORS[scheme]
        ax.fill_between(grid, mean - std, mean + std, color=color, alpha=0.2, linewidth=0)
        ax.plot(grid, mean, color=color, label=SCHEME_LABELS[scheme])
    _population_axes(ax, AXIS_POPULATION_MEAN)
    context.save_figure(fig, "population_mean_vs_time", item="b")


def _population_axes(ax: plt.Axes, label: str) -> None:
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(label)
    ax.set_xlim(left=0)
    ax.set_ylim(bottom=0)
    ax.legend(loc="upper right")


def _plot_scalar(
    table: pd.DataFrame,
    column: str,
    label: str,
    name: str,
    context: StudyContext,
    *,
    only_complete: bool = False,
) -> None:
    """Mean ± std per scheme; the schemes are categories, so no joining line. With
    `only_complete` (t_evac), a scheme with jammed runs gets a hollow marker and its legend."""
    fig, ax = plt.subplots()
    positions = np.arange(len(table))
    any_partial = False
    for position, (_, row) in zip(positions, table.iterrows(), strict=True):
        partial = only_complete and row["complete"] < row["realizations"]
        any_partial |= partial
        mean_errorbar(
            ax, [position], [row[column]], [row[f"{column}_std"]], [partial], linestyle="none",
            marker=SCHEME_MARKERS[row["scheme"]], color=SCHEME_COLORS[row["scheme"]],
        )  # fmt: skip
    ax.set_xticks(positions, [SCHEME_LABELS[scheme] for scheme in table["scheme"]])
    ax.set_xlim(-0.5, len(table) - 0.5)
    ax.set_ylabel(label)
    partial_legend(ax, any_partial, loc="best")
    context.save_figure(fig, name, item="b")


STUDY = Study(
    key="schemes",
    item="b",
    items="ab",
    title="a) y b) Los tres esquemas: fracción TSDMD, curvas de población y t_evac",
    config_type=SchemesConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
)
