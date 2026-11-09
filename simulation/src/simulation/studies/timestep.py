"""Choice of dt for TSDMD (DESIGN.md 2.2): pure TSDMD, k_t = 0, N = 200, M realizations per dt.

Reference dt: 0.1 sqrt(m / k_n) = 2.6e-3 s [Teorica_5, diapo 19]. Criterion: the largest dt whose
<t_evac> differs from that of the smallest dt by less than one sigma of the smallest dt; the
threshold band is drawn in the figure [Correcciones_TP4, D17]. The same seeds are used for every
dt, but the dynamics is chaotic: realizations are compared only through their statistics.

Runs that do not finish (jams, complete = false) are listed per dt and counted in the table; the
means use the complete ones. The cap is a safety stop far beyond the t_evac of a free run.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field

import matplotlib.pyplot as plt
import numpy as np
import pandas as pd

from simulation.engine import TIME_STEP, Case, EngineRun, dump_every_for
from simulation.errors import ConfigError
from simulation.observables import max_overlap, population_curve, summarize_run
from simulation.options import option
from simulation.plotting import (
    AXIS_DT,
    AXIS_EVACUATION,
    AXIS_FLOW,
    AXIS_POPULATION,
    AXIS_TIME,
    mean_errorbar,
    partial_legend,
    sci_label,
)
from simulation.stats import summarize
from simulation.studies.base import Study, StudyContext, summary_rows

DEFAULT_DTS = (2.5e-3, 1e-3, 5e-4, 2.5e-4, 1e-4)


@dataclass(frozen=True)
class TimestepConfig:
    n: int = field(default=200, metadata=option("Número de agentes N"))
    dts: tuple[float, ...] = field(default=DEFAULT_DTS, metadata=option("Pasos dt (s)"))
    realizations: int = field(default=10, metadata=option("Realizaciones M por dt"))
    seed: int = field(default=1, metadata=option("Semilla de la primera realización"))
    dt2: float = field(default=0.5, metadata=option("Intervalo de escritura dt2 (s)"))
    max_time: float = field(
        default=1000.0, metadata=option("Tope de seguridad del tiempo simulado (s)")
    )
    chosen_dt: float = field(
        default=1e-3, metadata=option("dt marcado como elegido (0 = el que sugiere el criterio)")
    )
    overlaps: bool = field(
        default=True, metadata=option("Calcular la superposición máxima (lee dynamic.txt)")
    )

    def __post_init__(self) -> None:
        if self.realizations < 2:
            raise ConfigError("Hacen falta al menos 2 realizaciones para el desvío")
        if len(self.dts) < 2:
            raise ConfigError("Hacen falta al menos 2 valores de dt")
        for dt in self.dts:
            dump_every_for(dt, self.dt2)


def _quick() -> TimestepConfig:
    return TimestepConfig(dts=(2.5e-3, 1e-3), realizations=2, max_time=300.0, overlaps=False)


def _cases(config: TimestepConfig, context: StudyContext) -> list[Case]:
    return [
        context.case(
            f"dt={dt:g}",
            EngineRun(
                scheme=TIME_STEP,
                dt=dt,
                dump_every=dump_every_for(dt, config.dt2),
                n=config.n,
                max_time=config.max_time,
                repeats=config.realizations,
                seed=config.seed,
            ),
        )
        for dt in config.dts
    ]


def _analyze(config: TimestepConfig, context: StudyContext) -> None:
    loaded = sorted(context.load(_cases(config, context)), key=lambda item: item[0].run.dt)
    rows, per_run = [], []
    examples = {}
    for case, runs in loaded:
        dt = case.run.dt
        summaries = [summarize_run(run) for run in runs]
        context.report_incomplete(case.name, summaries)
        evacuation = summarize(s.evacuation_time for s in summaries if s.complete)
        flow = summarize(s.flow for s in summaries)
        overlap = max(map(max_overlap, runs)) if config.overlaps else math.nan
        rows.append(
            {
                "dt": dt,
                "realizations": len(runs),
                "complete": evacuation.count,
                "evacuation_time": evacuation.mean,
                "evacuation_time_std": evacuation.std,
                "flow": flow.mean,
                "flow_std": flow.std,
                "max_overlap": overlap,
                "wall_clock_mean": float(np.mean([s.elapsed for s in summaries])),
            }
        )
        per_run += summary_rows(case.name, summaries, dt=dt)
        complete = [run for run, s in zip(runs, summaries, strict=True) if s.complete]
        if complete:
            examples[dt] = complete[0]
        detail = f", ξ_max = {overlap * 1e3:.1f} mm" if math.isfinite(overlap) else ""
        context.report(
            f"  dt = {dt:g} s: <t_evac> = {evacuation} s ({evacuation.count}/{len(runs)}), "
            f"<Q> = {flow} p/s{detail}"
        )
    table = pd.DataFrame(rows)
    context.save_table(table, "evacuation_vs_dt")
    context.save_table(pd.DataFrame(per_run), "runs")

    reference = table.iloc[0]
    tolerance = float(reference["evacuation_time_std"])
    accepted = table[
        (table["complete"] >= 2)
        & ((table["evacuation_time"] - reference["evacuation_time"]).abs() < tolerance)
    ]
    suggested = float(accepted["dt"].max()) if not accepted.empty else math.nan
    chosen = config.chosen_dt or suggested
    context.report(
        f"  criterio: |<t_evac>(dt) - <t_evac>({reference['dt']:g})| < σ = {tolerance:.1f} s; "
        f"pasan dt = {', '.join(f'{dt:g}' for dt in accepted['dt'])}"
    )
    context.report(f"  dt sugerido: {suggested:g} s; marcado en la figura: {chosen:g} s")
    _plot_evacuation(table, reference, tolerance, chosen, context)
    _plot_flow(table, context)
    _plot_population(examples, context)


def _plot_evacuation(
    table: pd.DataFrame, reference: pd.Series, tolerance: float, chosen: float, ctx: StudyContext
) -> None:
    fig, ax = plt.subplots()
    low = reference["evacuation_time"] - tolerance
    high = reference["evacuation_time"] + tolerance
    ax.axhspan(low, high, color="0.85", label="Tolerancia")
    partial = table["complete"] < table["realizations"]
    mean_errorbar(
        ax, table["dt"], table["evacuation_time"], table["evacuation_time_std"], partial,
        color="C0",
    )  # fmt: skip
    if math.isfinite(chosen):
        ax.axvline(chosen, color="black", linestyle=":", label=r"$\Delta t$ elegido")
    ax.set_xscale("log")
    ax.set_xlabel(AXIS_DT)
    ax.set_ylabel(AXIS_EVACUATION)
    partial_legend(ax, bool(partial.any()), loc="best")
    ctx.save_figure(fig, "evacuation_vs_dt")


def _plot_flow(table: pd.DataFrame, ctx: StudyContext) -> None:
    fig, ax = plt.subplots()
    mean_errorbar(ax, table["dt"], table["flow"], table["flow_std"], color="C0")
    ax.set_xscale("log")
    ax.set_xlabel(AXIS_DT)
    ax.set_ylabel(AXIS_FLOW)
    ctx.save_figure(fig, "flow_vs_dt")


def _plot_population(examples: dict, ctx: StudyContext) -> None:
    """One complete realization per dt: N(t) is only illustrative [Guia 2.4.2]."""
    if not examples:
        return
    fig, ax = plt.subplots()
    for dt, run in sorted(examples.items(), reverse=True):
        times, counts = population_curve(run)
        ax.step(times, counts, where="post", label=rf"$\Delta t$ = {sci_label(dt)} s")
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(AXIS_POPULATION)
    ax.set_xlim(left=0)
    ax.set_ylim(bottom=0)
    ax.legend(loc="lower left")
    ctx.save_figure(fig, "population_vs_time")


STUDY = Study(
    key="timestep",
    item="dt",
    title="Estudio de dt (TSDMD puro, k_t = 0)",
    config_type=TimestepConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
)
