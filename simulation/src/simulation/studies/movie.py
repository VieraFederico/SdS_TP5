"""Animations of every item, plus the typical time evolutions of items c and d [Guia 2.4.1-2.4.2].

One realization per scene, agents coloured by their scheme in each frame. Saved with the names
the slides expect:
    figures/<item>/anim_<scene>.png          still for the delivered PDF (\\figanim, 1st argument)
    figures/anim_lite/anim_<scene>.pdf       frames for the live version (\\figanim, 2nd argument)
    animations/anim_<scene>.mp4              video to upload (YouTube link under the still)
    figures/c/ts_fraction_vs_time.png        f_TS(t) of the two R_s extremes (one realization each)
    figures/d/population_vs_time.png         N_in(t) of the two N extremes (one realization each)
Besides, three demonstration scenes of the criteria E1, E2 and E3 (note of the professor): a few
agents placed by hand (--initial) in a 10 m room, each built so that one criterion is the one that
switches agents. They are illustrative only, not used by any observable:
    criterio_E1   R_s = 3 m, t_s = 0: agents walk into the zone, nobody is touching anybody
    criterio_E2   R_s = 1.5 m, t_s = 0: a jam inside the zone; the ones that arrive switch before touching
    criterio_E3   R_s = 0: no zone; the first switches come from collisions closer than t_s at the door
dt2 = speed / fps, so the video plays `speed` simulated seconds per second. In EDMD and the hybrid
the write events change the rounding, so a scene differs from the studies' run with the same seed
(DECISIONS.md, etapa 4): it is only illustrative. A scene whose seed jams is reported; the seeds
below were picked so that every scene finishes.
"""

from __future__ import annotations

import math
from dataclasses import dataclass, field
from typing import Final

import matplotlib.pyplot as plt

from simulation.animation import render_animation
from simulation.constants import DT, N, SIDE, cap_for
from simulation.engine import EVENT_DRIVEN, HYBRID, TIME_STEP, Case, EngineRun, dump_every_for
from simulation.errors import ConfigError
from simulation.observables import population_curve, summarize_run, ts_fraction
from simulation.options import option
from simulation.plotting import (
    AXIS_POPULATION,
    AXIS_TIME,
    AXIS_TS_FRACTION_SINGLE,
)
from simulation.reader import Run
from simulation.studies.base import FIGURES_DIR, Study, StudyContext


@dataclass(frozen=True)
class Scene:
    item: str
    scheme: str
    seed: int
    n: int = N
    side: float = SIDE
    zone_radius: float | None = None
    v_max: float | None = None
    flight_threshold: float | None = None
    initial: tuple[tuple[float, float, float], ...] | None = None
    """Agents placed by hand, (x, y, R) each; None places them at random with `seed`."""
    speed: float | None = None
    """Simulated seconds per video second; None uses the study's."""


RADIUS: Final = 0.2
"""Radius of the hand-placed agents (inside U[0.18, 0.25] m of the model)."""
SCENE_SIDE: Final = 10.0
DOOR_X: Final = SCENE_SIDE / 2


def _grid(inner: float, outer: float, spacing: float, low: float) -> tuple[tuple[float, float, float], ...]:
    """Agents on a square grid of `spacing` around the door, inner <= r <= outer and y >= low."""
    count = int(outer / spacing) + 1
    agents = []
    for i in range(-count, count + 1):
        for j in range(count + 1):
            x, y = DOOR_X + i * spacing, low + j * spacing
            if inner <= math.hypot(x - DOOR_X, y) <= outer:
                agents.append((round(x, 6), round(y, 6), RADIUS))
    return tuple(agents)


# E1: six agents far apart; they reach r = 3 m at different times and none touches another before.
_E1 = tuple((x, y, RADIUS) for x, y in ((1.5, 8.0), (3.5, 9.2), (5.0, 7.0), (6.6, 9.0), (8.5, 8.0), (2.2, 5.5)))
# E2: a jam inside the zone (TSDMD from t = 0, gaps of 0.1 m) and a looser crowd behind it, outside.
_E2 = _grid(0.0, 1.4, 0.5, 0.3) + _grid(2.0, 3.4, 0.7, 0.3)
# E3: agents that reach the door along the wall from both sides and squeeze at its edges.
_E3 = tuple(
    (x, y, RADIUS)
    for x, y in ((2.0, 0.3), (2.6, 0.3), (3.2, 0.3), (8.0, 0.3), (7.4, 0.3), (6.8, 0.3),
                 (3.0, 1.2), (7.0, 1.2), (4.2, 1.8), (5.8, 1.8), (5.0, 2.4))
)  # fmt: skip

SCENES: Final[dict[str, Scene]] = {
    # a) the three schemes; R_s = 5 m is the default of the hybrid.
    "tsdmd": Scene("a", TIME_STEP, 1),
    "edmd": Scene("a", EVENT_DRIVEN, 1),
    "hibrido": Scene("a", HYBRID, 2, zone_radius=5.0),
    # c) the two ends of the R_s sweep that differ in f_TS: the plateau and a wide zone.
    "hibrido_Rs0": Scene("c", HYBRID, 2, zone_radius=0.0),
    "hibrido_Rs20": Scene("c", HYBRID, 2, zone_radius=20.0),
    # d) the two ends of N at L = 50 m, hybrid with the v_max of item d.
    "N100": Scene("d", HYBRID, 1, n=100, zone_radius=5.0, v_max=8.0),
    "N1000": Scene("d", HYBRID, 4, n=1000, zone_radius=5.0, v_max=8.0),
    # Criteria of the hybrid, a few agents placed by hand (note of the professor).
    "criterio_E1": Scene("criterios", HYBRID, 1, n=len(_E1), side=SCENE_SIDE, zone_radius=3.0,
                         flight_threshold=0.0, initial=_E1, speed=1.0),
    "criterio_E2": Scene("criterios", HYBRID, 1, n=len(_E2), side=SCENE_SIDE, zone_radius=1.5,
                         flight_threshold=0.0, initial=_E2, speed=1.0),
    "criterio_E3": Scene("criterios", HYBRID, 1, n=len(_E3), side=SCENE_SIDE, zone_radius=0.0,
                         initial=_E3, speed=1.0),
}  # fmt: skip


@dataclass(frozen=True)
class MovieConfig:
    scenes: tuple[str, ...] = field(
        default=tuple(SCENES), metadata=option("Escenas", choices=tuple(SCENES))
    )
    dt: float = field(default=DT, metadata=option("Paso dt (s)"))
    speed: float = field(default=5.0, metadata=option("Segundos simulados por segundo de video"))
    fps: int = field(default=25, metadata=option("Cuadros por segundo del video"))
    arrows: bool = field(default=True, metadata=option("¿Dibujar velocidades? (sí/no)"))
    cap_per_agent: float = field(
        default=5.0, metadata=option("Tope del tiempo simulado por agente (s)")
    )

    def __post_init__(self) -> None:
        if self.fps < 1 or self.speed <= 0:
            raise ConfigError("fps y speed deben ser positivos")
        dump_every_for(self.dt, self.speed / self.fps)


def _quick() -> MovieConfig:
    return MovieConfig(scenes=("hibrido",), cap_per_agent=1.0)


def _name(scene: str) -> str:
    return f"anim_{scene}"


def _cases(config: MovieConfig, context: StudyContext) -> list[Case]:
    cases = []
    for key in config.scenes:
        scene = SCENES[key]
        initial = None
        if scene.initial is not None:
            path = context.output / "scenes" / f"{key}.txt"
            path.parent.mkdir(parents=True, exist_ok=True)
            path.write_text("".join(f"{x!r} {y!r} {r!r}\n" for x, y, r in scene.initial), encoding="utf-8")
            initial = str(path)
        speed = scene.speed or config.speed
        cases.append(
            context.case(
                _name(key),
                EngineRun(
                    scheme=scene.scheme,
                    dt=config.dt,
                    dump_every=dump_every_for(config.dt, speed / config.fps),
                    n=scene.n,
                    side=scene.side,
                    max_time=config.cap_per_agent * scene.n if scene.n != N else cap_for(N),
                    seed=scene.seed,
                    zone_radius=scene.zone_radius,
                    flight_threshold=scene.flight_threshold,
                    v_max=scene.v_max,
                    initial=initial,
                ),
            )
        )
    return cases


def _analyze(config: MovieConfig, context: StudyContext) -> None:
    runs: dict[str, Run] = {}
    for case, loaded in context.load(_cases(config, context)):
        key = case.name.removeprefix("anim_")
        run = runs[key] = loaded[0]
        summary = summarize_run(run)
        if not summary.complete:
            context.report(f"  AVISO {case.name}: {summary.describe()}; probar otra semilla")
        outputs = render_animation(
            run,
            context.output / "animations" / case.name,
            fps=config.fps,
            arrows=config.arrows,
            lite=context.output / FIGURES_DIR / "anim_lite" / f"{case.name}.pdf",
            report=context.report,
        )
        still = outputs[1]
        target = context.output / FIGURES_DIR / SCENES[key].item / still.name
        target.parent.mkdir(parents=True, exist_ok=True)
        still.replace(target)
        outputs[1] = target
        for output in outputs:
            context.report(f"  archivo: {output}")
    _plot_zone_extremes(runs, context)
    _plot_size_extremes(runs, context)


def _plot_zone_extremes(runs: dict[str, Run], context: StudyContext) -> None:
    """f_TS(t) for the two R_s ends, one realization each: the evolution of item c."""
    keys = [key for key in ("hibrido_Rs0", "hibrido_Rs20") if key in runs]
    if not keys:
        return
    fig, ax = plt.subplots()
    for key, style in zip(keys, ("-", "--"), strict=False):
        run = runs[key]
        radius = SCENES[key].zone_radius
        ax.plot(run.trajectory.times, ts_fraction(run.trajectory), linestyle=style,
                label=rf"$R_s$ = {radius:g} m")  # fmt: skip
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(AXIS_TS_FRACTION_SINGLE)
    ax.set_xlim(left=0)
    ax.set_ylim(0, 1.05)
    ax.legend(loc="lower right")
    context.save_figure(fig, "ts_fraction_vs_time", item="c")


def _plot_size_extremes(runs: dict[str, Run], context: StudyContext) -> None:
    """N_in(t) for the two N ends at L = 50 m, one realization each: the evolution of item d."""
    keys = [key for key in ("N100", "N1000") if key in runs]
    if not keys:
        return
    fig, ax = plt.subplots()
    for key, style in zip(keys, ("-", "--"), strict=False):
        times, counts = population_curve(runs[key])
        ax.step(times, counts, where="post", linestyle=style,
                label=f"$N$ = {SCENES[key].n}")  # fmt: skip
    ax.set_xlabel(AXIS_TIME)
    ax.set_ylabel(AXIS_POPULATION)
    ax.set_xlim(left=0)
    ax.set_ylim(bottom=0)
    ax.legend(loc="upper right")
    context.save_figure(fig, "population_vs_time", item="d")


STUDY = Study(
    key="animation",
    item="a",
    items="acd",
    title="Animaciones (a, c, d) y evoluciones típicas de c y d",
    config_type=MovieConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
)
