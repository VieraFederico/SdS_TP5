"""Defaults shared by the studies (DECISIONS.md, "Etapa 5"). Analyses always read the values the
engine actually used from each run's params.txt."""

from typing import Final

DT: Final = 1e-3
"""Chosen with the dt study: every dt in [1e-4, 2.5e-3] s gives the same <t_evac>."""
N: Final = 200
"""Items a-c [Consideraciones, ii]."""
SIDE: Final = 50.0
REALIZATIONS: Final = 10
"""At least 10 [Enunciado, ítem b]."""
DT2: Final = 0.5
"""Saving interval of the studies; animations and wall-clock use their own."""
CAP_PER_AGENT: Final = 5.0
"""Safety cap of the simulated time, 5 s per agent: 1000 s for N = 200, 7-13 times the t_evac of a
free run. Only jammed runs reach it, and they are reported."""
ZONE_RADIUS: Final = 5.0
"""Default R_s of the hybrid (engine default)."""
INITIAL_DENSITY: Final = 0.2
"""N / L^2 of the statement (500 / 50^2), kept constant in item d a)."""


def cap_for(n: int) -> float:
    return CAP_PER_AGENT * n
