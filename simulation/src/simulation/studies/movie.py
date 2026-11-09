"""Item a) animations: one realization per scheme, agents coloured by their scheme in each frame.

Saved with the names the slides expect:
    figures/a/anim_<scheme>.png          still for the delivered PDF (\\figanim, first argument)
    figures/anim_lite/anim_<scheme>.pdf  frames for the live version (\\figanim, second argument)
    animations/anim_<scheme>.mp4         video to upload (YouTube link under the still)
dt2 = speed / fps, so the video plays `speed` simulated seconds per second. In EDMD and the hybrid
the write events change the rounding, so this realization differs from the studies' one with the
same seed (DECISIONS.md, etapa 4): it is only illustrative.
"""

from __future__ import annotations

from dataclasses import dataclass, field
from typing import Final

from simulation.animation import render_animation
from simulation.constants import DT, N, ZONE_RADIUS, cap_for
from simulation.engine import EVENT_DRIVEN, HYBRID, SCHEMES, TIME_STEP, Case, EngineRun, dump_every_for
from simulation.errors import ConfigError
from simulation.observables import summarize_run
from simulation.options import option
from simulation.studies.base import FIGURES_DIR, Study, StudyContext

STEMS: Final = {TIME_STEP: "anim_tsdmd", EVENT_DRIVEN: "anim_edmd", HYBRID: "anim_hibrido"}


@dataclass(frozen=True)
class MovieConfig:
    schemes: tuple[str, ...] = field(default=SCHEMES, metadata=option("Esquemas", choices=SCHEMES))
    n: int = field(default=N, metadata=option("Número de agentes N"))
    zone_radius: float = field(default=ZONE_RADIUS, metadata=option("Híbrido: R_s (m)"))
    dt: float = field(default=DT, metadata=option("Paso dt (s)"))
    seeds: tuple[int, ...] = field(
        default=(1, 1, 2),
        metadata=option("Semilla de cada esquema, en el orden de arriba (híbrido: la 1 se traba)"),
    )
    speed: float = field(default=5.0, metadata=option("Segundos simulados por segundo de video"))
    fps: int = field(default=25, metadata=option("Cuadros por segundo del video"))
    arrows: bool = field(default=True, metadata=option("¿Dibujar velocidades? (sí/no)"))
    max_time: float = field(
        default=cap_for(N), metadata=option("Tope de seguridad del tiempo simulado (s)")
    )

    def __post_init__(self) -> None:
        if self.fps < 1 or self.speed <= 0:
            raise ConfigError("fps y speed deben ser positivos")
        dump_every_for(self.dt, self.speed / self.fps)
        if len(self.seeds) != len(self.schemes):
            raise ConfigError("Hace falta una semilla por esquema")


def _quick() -> MovieConfig:
    return MovieConfig(schemes=(HYBRID,), seeds=(1,), n=50, max_time=200.0)


def _cases(config: MovieConfig, context: StudyContext) -> list[Case]:
    return [
        context.case(
            STEMS[scheme],
            EngineRun(
                scheme=scheme,
                dt=config.dt,
                dump_every=dump_every_for(config.dt, config.speed / config.fps),
                n=config.n,
                max_time=config.max_time,
                seed=seed,
                zone_radius=config.zone_radius if scheme == HYBRID else None,
            ),
        )
        for scheme, seed in zip(config.schemes, config.seeds, strict=True)
    ]


def _analyze(config: MovieConfig, context: StudyContext) -> None:
    for case, runs in context.load(_cases(config, context)):
        run = runs[0]
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
        target = context.output / FIGURES_DIR / "a" / still.name
        target.parent.mkdir(parents=True, exist_ok=True)
        still.replace(target)
        outputs[1] = target
        for output in outputs:
            context.report(f"  archivo: {output}")


STUDY = Study(
    key="animation",
    item="a",
    title="a) Animaciones de los tres esquemas (MP4, fotograma PNG y PDF para la versión en vivo)",
    config_type=MovieConfig,
    quick=_quick,
    cases=_cases,
    analyze=_analyze,
)
