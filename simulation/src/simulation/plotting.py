"""Common figure style: the presentation guide and both corrections, applied by default.

- Axis names in words with MKS units in parentheses; no title and no caption [Guia 1.7-1.8].
- Every letter and number at least 20 pt [Guia 1.8; Correcciones_TP3, "fuentes ilegibles"].
- Powers of ten as superscripts, never 1E3 or 10^3 [Guia 1.9].
- Means with a marker and an error bar; straight lines only as a guide [Guia 2.4.6].
- Legends inside the axes, labelled by parameter value, never "promedio ± desvío" nor A1, A2
  [Correcciones_TP3, D21-D29]. M, N and the fixed parameters go beside the figure, on the slide.
- One name per axis across all figures (the AXIS constants below) [Correcciones_TP3, D26].
"""

from __future__ import annotations

import math
from pathlib import Path
from typing import Any, Final

import matplotlib
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.axes import Axes
from matplotlib.ticker import NullLocator
from matplotlib.figure import Figure
from matplotlib.lines import Line2D
from numpy.typing import ArrayLike

from simulation.stats import format_measurement

FIGURE_DPI: Final = 200
FONT_SIZE: Final = 20

EVENT_DRIVEN_COLOR: Final = "#1f77b4"
"""Blue: EDMD, as in Figure 1 of the statement (diluted zone)."""
TIME_STEP_COLOR: Final = "#d62728"
"""Red: TSDMD, as in Figure 1 of the statement (bottleneck)."""
HYBRID_COLOR: Final = "#2ca02c"
WALL_COLOR: Final = "black"

SCHEME_LABELS: Final = {
    "time-step": "TSDMD puro",
    "event-driven": "EDMD puro",
    "hybrid": "Híbrido",
}
SCHEME_COLORS: Final = {
    "time-step": TIME_STEP_COLOR,
    "event-driven": EVENT_DRIVEN_COLOR,
    "hybrid": HYBRID_COLOR,
}
SCHEME_MARKERS: Final = {"time-step": "s", "event-driven": "o", "hybrid": "^"}

AXIS_TIME: Final = "Tiempo $t$ (s)"
AXIS_POPULATION: Final = r"Agentes en la habitación $N_{\mathrm{in}}(t)$"
"""N is the number of agents; the agents inside get their own symbol [Correcciones_TP4, D12/D14]."""
AXIS_POPULATION_MEAN: Final = r"Agentes en la habitación $\langle N_{\mathrm{in}}(t)\rangle$"
AXIS_EVACUATION: Final = r"Tiempo de evacuación $\langle t_{\mathrm{evac}}\rangle$ (s)"
AXIS_DT: Final = r"Paso temporal $\Delta t$ (s)"
AXIS_SCHEME_FRACTION: Final = "Fracción de agentes"
AXIS_TS_FRACTION: Final = r"Fracción en TSDMD $\langle f_{\mathrm{TS}}(t)\rangle$"
AXIS_TS_FRACTION_AVERAGE: Final = r"Fracción media en TSDMD $\langle \bar{f}_{\mathrm{TS}}\rangle$"
AXIS_WALL_CLOCK: Final = r"Tiempo de ejecución $\langle T_{\mathrm{ej}}\rangle$ (s)"
"""T_ej, not t: t is the simulated time [Correcciones_TP4, Para el TP5]; tau is the SFM relaxation."""
AXIS_ZONE_RADIUS: Final = r"Radio de la zona TSDMD $R_s$ (m)"
AXIS_AGENTS: Final = "Número de agentes $N$"
AXIS_FLOW: Final = r"Caudal $\langle Q\rangle$ (s$^{-1}$)"
AXIS_X: Final = "Posición $x$ (m)"
AXIS_Y: Final = "Posición $y$ (m)"
PARTIAL_LABEL: Final = "Con atascos"
"""Hollow marker: mean over the complete runs only, some got jammed. Always with its legend entry
[Correcciones_TP4, D30: "marcador vacío sin leyenda"]."""


def apply_style() -> None:
    matplotlib.use("Agg")
    plt.rcParams.update(
        {
            "figure.figsize": (8, 5.5),
            "font.size": FONT_SIZE,
            "axes.labelsize": FONT_SIZE,
            "axes.titlesize": FONT_SIZE,
            "legend.fontsize": FONT_SIZE,
            "legend.title_fontsize": FONT_SIZE,
            "figure.titlesize": FONT_SIZE,
            "figure.labelsize": FONT_SIZE,
            "xtick.labelsize": FONT_SIZE,
            "ytick.labelsize": FONT_SIZE,
            "axes.grid": False,
            "lines.linewidth": 1.8,
            "lines.markersize": 8,
            "errorbar.capsize": 3,
            # Powers of ten only outside [10⁻², 10³): an axis from 0 to 300 s reads 0-300.
            "axes.formatter.limits": (-2, 3),
            "axes.formatter.use_mathtext": True,
            "axes.formatter.useoffset": False,
            "legend.frameon": False,
        }
    )


def sci_label(value: float, digits: int = 3) -> str:
    """'2.5×10⁻⁴' as mathtext, with up to `digits` significant digits; '10⁻³' without '1×'."""
    if not math.isfinite(value):
        return "—"
    if value == 0:
        return "$0$"
    mantissa, exponent = f"{value:.{digits - 1}e}".split("e")
    if "." in mantissa:
        mantissa = mantissa.rstrip("0").rstrip(".")
    if mantissa in ("1", "-1"):
        return rf"${mantissa[:-1]}10^{{{int(exponent)}}}$"
    return rf"${mantissa}\times10^{{{int(exponent)}}}$"


def sci_measurement(mean: float, std: float) -> str:
    """'(1.25 ± 0.03)×10⁻²' as mathtext: format_measurement applied to the mantissa."""
    if not math.isfinite(mean) or mean == 0:
        return format_measurement(mean, std)
    if not math.isfinite(std) or std <= 0:
        return sci_label(mean, 4)
    exponent = math.floor(math.log10(abs(mean)))
    scale = 10.0**exponent
    text = format_measurement(mean / scale, std / scale).replace(" ± ", r" \pm ")
    return rf"$({text})\times10^{{{exponent}}}$"


def sci_ticks(ax: Axes, axis: str, values: ArrayLike) -> None:
    """Ticks exactly at the studied values, written as powers of ten."""
    values = list(np.asarray(values, dtype=float))
    labels = [sci_label(value) for value in values]
    target = ax.xaxis if axis == "x" else ax.yaxis
    target.set_ticks(values, labels)
    target.set_minor_locator(NullLocator())


def mean_errorbar(
    ax: Axes,
    x: ArrayLike,
    mean: ArrayLike,
    std: ArrayLike,
    partial: ArrayLike | None = None,
    **style: Any,
) -> str:
    """Mean ± std (M - 1) with a marker at each point and a straight line only as a guide
    [Guia 2.4.6], as in the TP4 (fmt "o-", capsize 3). Returns the series colour.

    `partial` marks the points where some runs got jammed: the mean uses the complete ones and the
    marker is hollow; call `partial_legend` so it never goes without its legend [Correcciones_TP4,
    D30]. On a log y axis a bar whose lower end would be <= 0 keeps only its upper half.
    """
    x, mean, std = (np.asarray(values, dtype=float) for values in (x, mean, std))
    style.setdefault("marker", "o")
    style.setdefault("linestyle", "-")
    style.setdefault("linewidth", 1.2)
    lower = np.where(std < mean, std, 0.0) if ax.get_yscale() == "log" else std
    bar = ax.errorbar(x, mean, yerr=[lower, std], **style)
    line = bar[0]
    if partial is not None:
        hollow = np.asarray(partial, dtype=bool)
        ax.plot(
            x[hollow], mean[hollow], linestyle="none", marker=line.get_marker(),
            markersize=line.get_markersize(), color=line.get_color(), markerfacecolor="white",
            zorder=line.get_zorder() + 0.1,
        )  # fmt: skip
    return line.get_color()


def partial_legend(ax: Axes, any_partial: bool, **legend: Any) -> None:
    """The axes legend plus, if some point is hollow, the entry that explains it."""
    handles, labels = ax.get_legend_handles_labels()
    if any_partial:
        handles.append(
            Line2D([], [], linestyle="none", marker="o", color="0.3", markerfacecolor="white")
        )
        labels.append(PARTIAL_LABEL)
    if handles:
        ax.legend(handles, labels, **legend)


def save_figure(fig: Figure, path: Path) -> Path:
    path.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(path, dpi=FIGURE_DPI, bbox_inches="tight")
    plt.close(fig)
    return path
