"""Defaults shared by the studies (DECISIONS.md, "Etapa 5"). Analyses always read the values the
engine actually used from each run's params.txt."""

import math
from typing import Final

DT: Final = 1e-3
"""Chosen with the dt study: the largest dt whose <t_evac> is within one std of the smallest dt's."""
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
FLIGHT_THRESHOLD: Final = 0.3
"""Default t_s of the hybrid, the engine's (★21b, revised 2026-11-27): about 5 pi/omega, the cheapest
of the item c sweep."""
CONTACT_DURATION: Final = math.pi / math.sqrt(1.2e5 / 40.0)
"""pi/omega of two agents (k_n = 1.2e5 N/m, reduced mass 40 kg), 5.7e-2 s: the former default t_s,
kept as a point of the item c sweep."""
INITIAL_DENSITY: Final = 0.2
"""N / L^2 of the statement (500 / 50^2), kept constant in item d a)."""


def cap_for(n: int) -> float:
    return CAP_PER_AGENT * n
