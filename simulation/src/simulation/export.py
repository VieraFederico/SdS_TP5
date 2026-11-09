"""Copies the figures of a study session to presentation/figuras/<item>/ [DESIGN.md, decision 32].

The slides reference figuras/<item>/<name>.png with \\figres, so the .tex never changes: a new
session only replaces the files. Quick sessions are never exported by `latest`.
"""

from __future__ import annotations

import shutil
from collections.abc import Callable, Collection
from pathlib import Path

from simulation.errors import ConfigError, MissingResultsError
from simulation.paths import PRESENTATION_FIGURES
from simulation.sessions import LATEST, find_session
from simulation.studies import STUDIES
from simulation.studies.base import FIGURES_DIR


def export_figures(
    keys: Collection[str],
    root: Path,
    *,
    session: str = LATEST,
    target: Path = PRESENTATION_FIGURES,
    report: Callable[[str], None] = print,
) -> list[Path]:
    """Copies <session>/figures/<item>/*.png of each study; studies without sessions are skipped."""
    unknown = set(keys) - set(STUDIES)
    if unknown:
        raise ConfigError(f"Estudios desconocidos: {', '.join(sorted(unknown))}")
    copied: list[Path] = []
    for key in (key for key in STUDIES if key in keys):
        try:
            directory = find_session(
                root / STUDIES[key].folder, session, quick=False if session == LATEST else None
            )
        except MissingResultsError:
            report(f"  {key}: sin sesiones completas, se omite")
            continue
        figures = directory / FIGURES_DIR
        files = sorted(figures.rglob("*.png")) + sorted(figures.rglob("*.pdf"))
        if not files:
            report(f"  {key}: la sesión {directory.name} no tiene figuras (analizar antes)")
            continue
        for source in files:
            destination = target / source.relative_to(figures)
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copy2(source, destination)
            copied.append(destination)
        report(f"  {key}: {len(files)} figura(s) de {directory.name}")
    return copied
