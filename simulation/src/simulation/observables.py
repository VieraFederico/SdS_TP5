"""Observables, computed only here from the engine files: the engine computes none [Correcciones_TP4,
Código; Teorica_0, diapo 20].

- N_in(t): agents inside the room (not yet across the door, y > 0), from exits.txt, exact in t.
- t_evac: instant the last agent crosses the door, max_i t_door,i. Undefined (NaN) when some agent
  never crosses: a jam. Jammed runs are reported, never silently dropped [Consideraciones, iii].
- f_TS(t) = N_TS(t) / N_in(t) over the agents inside, from dynamic.txt (scheme column).
- Q: flow through the door, crossings per second in the central part of the evacuation
  (between the 10 % and the 90 % crossing), which leaves out the arrival of the first agents and
  the tail of the last ones [Teorica_6, diapos 21-23: specific flow = Q / d].
"""

from __future__ import annotations

import math
import warnings
from dataclasses import dataclass
from typing import Final

import numpy as np

from simulation.reader import INSIDE, RETIRED, TIME_STEP, FloatArray, Run, Trajectory

FLOW_LOW: Final = 0.1
FLOW_HIGH: Final = 0.9


def crossing_times(run: Run) -> FloatArray:
    """Sorted door crossings of the agents that crossed."""
    door = run.exits.door
    return np.sort(door[np.isfinite(door)])


def population_curve(run: Run) -> tuple[FloatArray, FloatArray]:
    """N(t) as a step function (use `where="post"`): N at t = 0, then one less at each crossing."""
    times = crossing_times(run)
    n = run.static.n
    return np.concatenate(([0.0], times)), n - np.arange(len(times) + 1, dtype=float)


def population_on(run: Run, grid: FloatArray) -> FloatArray:
    """N(t) on a common time grid, to average the curves of several realizations."""
    return run.static.n - np.searchsorted(crossing_times(run), grid, side="right").astype(float)


def evacuation_time(run: Run) -> float:
    """max_i t_door,i if every agent crossed the door; NaN otherwise (jam or cut)."""
    door = run.exits.door
    return float(door.max()) if np.isfinite(door).all() else math.nan


def flow_rate(run: Run, *, low: float = FLOW_LOW, high: float = FLOW_HIGH) -> float:
    """(k_high - k_low) / (t_(k_high) - t_(k_low)) over the sorted crossings; NaN if not reached."""
    times = crossing_times(run)
    n = run.static.n
    first, last = math.ceil(low * n), math.floor(high * n)
    if last <= first or len(times) < last:
        return math.nan
    span = times[last - 1] - times[first - 1]
    return (last - first) / span if span > 0 else math.nan


def ts_fraction(trajectory: Trajectory) -> FloatArray:
    """f_TS at each saved frame; NaN once nobody is inside."""
    inside = trajectory.locations == INSIDE
    count = inside.sum(axis=1)
    time_step = (inside & (trajectory.schemes == TIME_STEP)).sum(axis=1)
    with np.errstate(invalid="ignore", divide="ignore"):
        return np.where(count > 0, time_step / count, np.nan)


def max_overlap(run: Run) -> float:
    """Largest agent-agent overlap xi = R_i + R_j - |r_j - r_i| over the saved frames (m).

    Retired agents keep their last position and are left out. A sanity check of the dynamics
    (overlaps of millimetres), not a result.
    """
    trajectory = run.trajectory
    radii = run.static.radii
    worst = 0.0
    for frame in range(len(trajectory.times)):
        active = trajectory.locations[frame] != RETIRED
        if active.sum() < 2:
            continue
        positions = trajectory.positions[frame][active]
        r = radii[active]
        delta = positions[:, None, :] - positions[None, :, :]
        distance = np.hypot(delta[..., 0], delta[..., 1])
        overlap = r[:, None] + r[None, :] - distance
        np.fill_diagonal(overlap, -np.inf)
        worst = max(worst, float(overlap.max()))
    return worst


@dataclass(frozen=True)
class RunSummary:
    """One realization at a glance; `complete` False means a jam or a cut at the cap."""

    label: str
    complete: bool
    evacuation_time: float
    evacuated: int
    n: int
    last_crossing: float
    flow: float
    cap: float
    elapsed: float

    def describe(self) -> str:
        if self.complete:
            return f"{self.label}: t_evac = {self.evacuation_time:.1f} s, Q = {self.flow:.2f} p/s"
        last = f"{self.last_crossing:.1f} s" if math.isfinite(self.last_crossing) else "—"
        return (
            f"{self.label}: SIN TERMINAR (complete = false), salieron {self.evacuated}/{self.n}, "
            f"último cruce a {last}, corte a {self.cap:g} s"
        )


def summarize_run(run: Run) -> RunSummary:
    times = crossing_times(run)
    return RunSummary(
        label=run.label,
        complete=run.complete,
        evacuation_time=evacuation_time(run) if run.complete else math.nan,
        evacuated=len(times),
        n=run.static.n,
        last_crossing=float(times[-1]) if len(times) else math.nan,
        flow=flow_rate(run),
        cap=run.params.max_time,
        elapsed=run.params.elapsed_seconds,
    )


def mean_population(runs: list[Run], grid: FloatArray) -> tuple[FloatArray, FloatArray]:
    """Mean and sample std (M - 1) of N(t) over `runs`, on a common grid."""
    curves = np.array([population_on(run, grid) for run in runs])
    std = curves.std(axis=0, ddof=1) if len(runs) > 1 else np.zeros(len(grid))
    return curves.mean(axis=0), std


def mean_ts_fraction(runs: list[Run]) -> tuple[FloatArray, FloatArray, FloatArray]:
    """Frame times, mean and sample std of f_TS(t) over the runs that still have agents inside.

    Every run saves at k dt2, so frame k is the same instant in all of them.
    """
    fractions = [ts_fraction(run.trajectory) for run in runs]
    longest = max(range(len(runs)), key=lambda i: len(fractions[i]))
    times = runs[longest].trajectory.times
    table = np.full((len(runs), len(times)), np.nan)
    for row, values in enumerate(fractions):
        table[row, : len(values)] = values
    with warnings.catch_warnings():
        warnings.simplefilter("ignore", RuntimeWarning)  # all-NaN columns: nobody left inside
        mean = np.nanmean(table, axis=0)
        std = np.nanstd(table, axis=0, ddof=1)
    return times, mean, std


def time_averaged_ts_fraction(run: Run) -> float:
    """Mean of f_TS over the saved frames with agents inside: one scalar per realization."""
    values = ts_fraction(run.trajectory)
    values = values[np.isfinite(values)]
    return float(values.mean()) if len(values) else math.nan

