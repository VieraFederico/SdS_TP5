"""Repository locations. The package lives in <repo>/simulation/src/simulation."""

from pathlib import Path
from typing import Final

REPO_ROOT: Final[Path] = Path(__file__).resolve().parents[3]
ENGINE_SOURCES: Final[Path] = REPO_ROOT / "experiment" / "src"
ENGINE_CHECKS: Final[Path] = REPO_ROOT / "experiment" / "checks"
ENGINE_CLASSES: Final[Path] = REPO_ROOT / "experiment" / "out" / "classes"
CHECK_CLASSES: Final[Path] = REPO_ROOT / "experiment" / "out" / "checks"
OUTPUT_ROOT: Final[Path] = REPO_ROOT / "output"
PRESENTATION_FIGURES: Final[Path] = REPO_ROOT / "presentation" / "figuras"
