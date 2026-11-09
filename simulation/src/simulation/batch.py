"""Several studies in one go (up to the whole assignment), one new session per study."""

from __future__ import annotations

from collections.abc import Collection
from pathlib import Path
from typing import Final

from simulation.errors import ConfigError, SimulationError
from simulation.studies import STUDIES
from simulation.studies.base import open_session

ANIMATION: Final = "animation"
ASSIGNMENT: Final[tuple[str, ...]] = tuple(key for key in STUDIES if key != ANIMATION)


def run_batch(
    keys: Collection[str], root: Path, *, quick: bool, animations: bool, jobs: int
) -> bool:
    """Runs the chosen studies in statement order; True if all of them succeeded.

    A failing study is reported and the rest still run.
    """
    unknown = set(keys) - set(ASSIGNMENT)
    if unknown:
        raise ConfigError(
            f"Estudios desconocidos: {', '.join(sorted(unknown))}. "
            f"Opciones: {', '.join(ASSIGNMENT)}"
        )
    selected = [key for key in ASSIGNMENT if key in keys]
    if animations and ANIMATION in STUDIES:
        selected.append(ANIMATION)
    if not selected:
        raise ConfigError("Elegir al menos un estudio o activar las animaciones")

    sessions: dict[str, Path] = {}
    failed: list[str] = []
    for index, key in enumerate(selected, 1):
        study = STUDIES[key]
        print(f"\n[{index}/{len(selected)}] {study.title}")
        try:
            config, context = open_session(
                study, root, session=None, quick=quick, overrides={}, jobs=jobs
            )
            print(f"Sesión: {context.output}")
            study.execute(config, context)
        except SimulationError as error:
            print(f"Error: {error}")
            failed.append(key)
            continue
        sessions[key] = context.output

    print(f"\nCorrida terminada: {len(sessions)}/{len(selected)} estudio(s) completos")
    for key, session in sessions.items():
        print(f"  {key}: {session}")
    if failed:
        print(f"  Con errores: {', '.join(failed)}")
    return not failed
