"""Registry of the studies, in the order of the statement (dt first: every other study uses it)."""

from typing import Any, Final

from simulation.studies import movie, runtime, scaling, schemes, timestep
from simulation.studies.base import Study, StudyContext

STUDIES: Final[dict[str, Study[Any]]] = {
    study.key: study
    for study in (timestep.STUDY, schemes.STUDY, runtime.STUDY, scaling.STUDY, movie.STUDY)
}

__all__ = ["STUDIES", "Study", "StudyContext"]
