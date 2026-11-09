"""Statistics as presented in Teorica_0 (diapos 60-62 and 69-71): mean, sample std, fits."""

from __future__ import annotations

import math
from collections.abc import Callable, Iterable
from dataclasses import dataclass

import numpy as np
from numpy.typing import NDArray

FloatArray = NDArray[np.float64]


@dataclass(frozen=True)
class Summary:
    """Mean and sample standard deviation (M - 1) of the finite values; `count` of them."""

    mean: float
    std: float
    count: int

    def __str__(self) -> str:
        return format_measurement(self.mean, self.std)


def summarize(values: Iterable[float]) -> Summary:
    data = np.asarray(list(values), dtype=float)
    data = data[np.isfinite(data)]
    if data.size == 0:
        return Summary(math.nan, math.nan, 0)
    std = float(np.std(data, ddof=1)) if data.size > 1 else 0.0
    return Summary(float(np.mean(data)), std, int(data.size))


def format_measurement(mean: float, std: float) -> str:
    """'45.4 ± 0.3': the error keeps one significant digit and fixes the value's precision."""
    if not math.isfinite(mean):
        return "—"
    if not math.isfinite(std) or std <= 0:
        return f"{mean:.4g}"
    digits = -math.floor(math.log10(std))
    error = round(std, digits)
    if error >= 10 ** (1 - digits):
        digits -= 1
        error = round(std, digits)
    decimals = max(digits, 0)
    return f"{round(mean, digits):.{decimals}f} ± {error:.{decimals}f}"


@dataclass(frozen=True)
class FitResult:
    parameter: float
    error: float
    grid: FloatArray
    errors: FloatArray


def fit_single_parameter(
    model: Callable[[FloatArray, float], FloatArray],
    x: FloatArray,
    y: FloatArray,
    *,
    lower: float,
    upper: float,
    points: int = 2001,
    refinements: int = 3,
) -> FitResult:
    """Minimizes E(c) = sum_i [y_i - f(x_i, c)]^2 by scanning c, then refining around the minimum.

    Returns the best c, E(c*), and the first (coarse) scan so E(c) can be plotted [Teorica_0,
    diapos 75-82].
    """

    def scan(low: float, high: float) -> tuple[FloatArray, FloatArray]:
        grid = np.linspace(low, high, points)
        errors = np.array([np.sum((y - model(x, c)) ** 2) for c in grid])
        return grid, errors

    grid, errors = scan(lower, upper)
    coarse = (grid, errors)
    for _ in range(refinements):
        best = int(np.argmin(errors))
        grid, errors = scan(grid[max(best - 1, 0)], grid[min(best + 1, points - 1)])
    best = int(np.argmin(errors))
    return FitResult(float(grid[best]), float(errors[best]), coarse[0], coarse[1])
