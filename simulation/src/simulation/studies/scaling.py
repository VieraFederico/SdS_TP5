"""Item d): how the wall-clock of the three schemes scales with N, in two variants.

- density: L = sqrt(N / 0.2), the initial density of the statement (500 / 50^2) kept constant;
- side: L = 50 m, the density grows with N.

Measured as in item c): one engine at a time, warmup runs in the same JVM, large dt2. The cap of
the simulated time grows with N (1.5 s per agent, 3-4 times the expected t_evac): the wall-clock
of a jammed run is not used, so a longer cap would only cost time. Runs that do not finish are
reported and left out of the means. Figures (figures/d/): wall-clock vs N in log-log [Guia 2.4.7]
and <t_evac> vs N, one per variant.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from typing import Final

import matplotlib.pyplot as plt
import pandas as pd

from simulation.constants import DT, INITIAL_DENSITY, SIDE, ZONE_RADIUS
from simulation.engine import EVENT_DRIVEN, HYBRID, SCHEMES, Case, EngineRun, dump_every_for
from simulation.errors import ConfigError
from simulation.observables import summarize_run
from simulation.options import option
from simulation.plotting import (
    AXIS_AGENTS,
    AXIS_EVACUATION,
    AXIS_WALL_CLOCK,
    SCHEME_COLORS,
    SCHEME_LABELS,
    SCHEME_MARKERS,
    mean_errorbar,
    partial_legend,
)
from simulation.stats import summarize
from simulation.studies.base import Study, StudyContext, summary_rows

VARIANTS: Final = ("density", "side")
DEFAULT_NS: Final = (100, 200, 300, 500, 700, 1000)


def side_for(variant: str, n: int) -> float:
    return round(math.sqrt(n / INITIAL_DENSITY), 4) if variant == "density" else SIDE


@dataclass(frozen=True)
class ScalingConfig:
    ns: tuple[int, ...] = field(default=DEFAULT_NS, metadata=option("Valores de N"))
    variants: tuple[str, ...] = field(
        default=VARIANTS,
        metadata=option("Variantes: density (L = √(N/0.2)) y side (L = 50 m)", choices=VARIANTS),
    )
    schemes: tuple[str, ...] = field(default=SCHEMES, metadata=option("Esquemas", choices=SCHEMES))
    zone_radius: float = field(default=ZONE_RADIUS, metadata=option("Híbrido: R_s (m)"))
    v_max: float = field(
        default=8.0,
        metadata=option("Híbrido: v_max de la garantía de la piel (m/s); con N ≥ 700 se pasan 5"),
    )
    dt: float = field(default=DT, metadata=option("Paso dt (s)"))
    dt2: float = field(default=5.0, metadata=option("Intervalo de escritura dt2 (s), grande"))
    realizations: int = field(
        default=3, metadata=option("Realizaciones M (3 para revisar rápido; subir si hace falta)")
    )
    warmup: int = field(default=1, metadata=option("Corridas de calentamiento por caso"))
    cap_per_agent: float = field(
        default=1.5, metadata=option("Tope del tiempo simulado por agente (s): tope = valor × N")
    )
    seed: int = field(default=1, metadata=option("Semilla de la primera realización"))
    edmd_max_n: int = field(
        default=500, metadata=option("EDMD solo hasta este N (crece como N⁴–N⁵; 0 = sin límite)")
    )

    def __post_init__(self) -> None:
        if self.realizations < 2:
            raise ConfigError("Hacen falta al menos 2 realizaciones para el desvío")
        dump_every_for(self.dt, self.dt2)


def _quick() -> ScalingConfig:
    return ScalingConfig(ns=(100, 200), realizations=2, warmup=0)


def _cases(config: ScalingConfig, context: StudyContext) -> list[Case]:
    return [
        context.case(
            f"{variant}_{scheme}_N={n}",
            EngineRun(
                scheme=scheme,
                dt=config.dt,
                dump_every=dump_every_for(config.dt, config.dt2),
                n=n,
                side=side_for(variant, n),
                max_time=config.cap_per_agent * n,
                repeats=config.realizations,
                seed=config.seed,
                warmup=config.warmup,
                zone_radius=config.zone_radius if scheme == HYBRID else None,
                v_max=config.v_max if scheme == HYBRID else None,
            ),
        )
        for variant in config.variants
        for scheme in config.schemes
        for n in config.ns
        if not _shared(config, variant, n)
        and not (scheme == EVENT_DRIVEN and 0 < config.edmd_max_n < n)
    ]


def _shared(config: ScalingConfig, variant: str, n: int) -> bool:
    """With L = 50 m both variants are the same room (N = 500): simulated once, as density."""
    return variant == "side" and "density" in config.variants and side_for("density", n) == SIDE


def _analyze(config: ScalingConfig, context: StudyContext) -> None:
    rows, per_run = [], []
    for case, runs in context.load(_cases(config, context)):
        variant = case.name.split("_", 1)[0]
        summaries = [summarize_run(run) for run in runs]
        context.report_incomplete(case.name, summaries)
        complete = [s for s in summaries if s.complete]
        wall = summarize(s.elapsed for s in complete)
        evacuation = summarize(s.evacuation_time for s in complete)
        rows.append(
            {
                "variant": variant,
                "scheme": case.run.scheme,
                "n": case.run.n,
                "side": case.run.side,
                "realizations": len(runs),
                "complete": len(complete),
                "wall_clock": wall.mean,
                "wall_clock_std": wall.std,
                "evacuation_time": evacuation.mean,
                "evacuation_time_std": evacuation.std,
            }
        )
        per_run += summary_rows(
            case.name, summaries, variant=variant, scheme=case.run.scheme, side=case.run.side
        )
        context.report(
            f"  {case.name}: wall-clock = {wall} s, <t_evac> = {evacuation} s "
            f"({len(complete)}/{len(runs)} completas)"
        )
    table = pd.DataFrame(rows)
    if "side" in config.variants:
        shared = table[(table["variant"] == "density") & (table["side"] == SIDE)]
        table = pd.concat([table, shared.assign(variant="side")], ignore_index=True)
    context.save_table(table, "scaling")
    context.save_table(pd.DataFrame(per_run), "runs")
    for variant in config.variants:
        subset = table[table["variant"] == variant]
        if subset.empty:
            continue
        _plot(subset, "wall_clock", AXIS_WALL_CLOCK, f"wall_clock_vs_n_{variant}", True, context)
        _plot(
            subset, "evacuation_time", AXIS_EVACUATION, f"evacuation_vs_n_{variant}", False,
            context,
        )  # fmt: skip


def _plot(
    table: pd.DataFrame, column: str, label: str, name: str, log: bool, context: StudyContext
) -> None:
    fig, ax = plt.subplots()
    if log:  # before the bars, so a bar reaching <= 0 keeps only its upper half
        ax.set_xscale("log")
        ax.set_yscale("log")
    any_partial = False
    for scheme in SCHEMES:
        rows = table[table["scheme"] == scheme].sort_values("n")
        if rows.empty:
            continue
        partial = rows["complete"] < rows["realizations"]
        any_partial |= bool(partial.any())
        mean_errorbar(
            ax, rows["n"], rows[column], rows[f"{column}_std"], partial,
            color=SCHEME_COLORS[scheme], marker=SCHEME_MARKERS[scheme],
            label=SCHEME_LABELS[scheme],
        )  # fmt: skip
    ax.set_xlabel(AXIS_AGENTS)
    ax.set_ylabel(label)
    partial_legend(ax, any_partial, loc="upper left")
    context.save_figure(fig, name)


STUDY = Study(
    key="scaling",
    item="d",
    title="d) Escalamiento del tiempo de ejecución con N (densidad constante y L constante)",
    config_type=ScalingConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
    sequential=True,
)
