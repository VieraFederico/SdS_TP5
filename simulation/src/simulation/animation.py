"""Offline animation of a saved run: the video never depends on the simulation speed.

Each agent is coloured by the scheme that resolves it in that frame (dynamic.txt column `scheme`):
blue EDMD, red TSDMD, as in Figure 1 of the statement [Enunciado, ítem a]. Retired agents are
not drawn. Outputs: an MP4, a PNG still for the delivered PDF [Guia 2.4.8] and a multi-page PDF of
frames for the live version (\\figanim plays figuras/anim_lite/<name>.pdf at 5 fps).
"""

from __future__ import annotations

import math
from collections.abc import Callable
from pathlib import Path
from typing import Final

import imageio_ffmpeg
import matplotlib.pyplot as plt
import numpy as np
from matplotlib.animation import FFMpegWriter, FuncAnimation
from matplotlib.backends.backend_pdf import PdfPages
from matplotlib.collections import EllipseCollection
from matplotlib.colors import to_rgba_array
from matplotlib.figure import Figure
from matplotlib.lines import Line2D
from matplotlib.patches import Circle

from simulation.errors import ConfigError
from simulation.observables import crossing_times, ts_fraction
from simulation.plotting import (
    AXIS_X,
    AXIS_Y,
    EVENT_DRIVEN_COLOR,
    TIME_STEP_COLOR,
    WALL_COLOR,
)
from simulation.reader import RETIRED, TIME_STEP, Run

VIDEO_SIZE_INCHES: Final = 8.0
VIDEO_DPI: Final = 110
STILL_DPI: Final = 200
LITE_FPS: Final = 5
"""Frame rate of \\animategraphics in the template [TP4_Presentacion.tex L133-139]."""
LITE_PAGES: Final = 60
ARROW_SECONDS: Final = 0.5
"""Arrows are the displacement over this time: v_d = 1.5 m/s -> 0.75 m."""
OUTSIDE: Final = 2.0
"""Metres drawn below the door wall, so the retirement line y = -1 m is visible."""


def representative_time(run: Run) -> float:
    """Hybrid: first frame with f_TS >= 0.5, so both colours share the room. Pure schemes: the
    instant a quarter of the agents has crossed, with the bottleneck already formed."""
    if run.params.scheme == "hybrid":
        reached = np.flatnonzero(ts_fraction(run.trajectory) >= 0.5)
        if reached.size:
            return float(run.trajectory.times[reached[0]])
    times = crossing_times(run)
    quarter = run.static.n // 4
    if len(times) >= quarter > 0:
        return float(times[quarter - 1])
    return float(run.trajectory.times[-1]) / 2


def render_animation(
    run: Run,
    stem: Path,
    *,
    fps: int = 25,
    every: int = 1,
    arrows: bool = True,
    still_time: float | None = None,
    lite: Path | None = None,
    report: Callable[[str], None] = print,
) -> list[Path]:
    """Writes <stem>.mp4, <stem>.png and, if `lite` is given, that multi-page PDF."""
    video = stem.parent / (stem.name + ".mp4")
    report(f"Renderizando {video}")
    outputs = [animate_run(run, video, fps=fps, every=every, arrows=arrows, report=report)]
    still = stem.parent / (stem.name + ".png")
    outputs.append(render_still(run, still, time=still_time, arrows=arrows))
    if lite is not None:
        outputs.append(render_lite(run, lite, arrows=arrows))
    return outputs


def animate_run(
    run: Run,
    output: Path,
    *,
    fps: int = 25,
    every: int = 1,
    arrows: bool = True,
    report: Callable[[str], None] = print,
) -> Path:
    if fps < 1 or every < 1:
        raise ConfigError("fps y every deben ser enteros positivos")
    fig, draw = _scene(run, arrows=arrows, clock=True)
    frames = range(0, len(run.trajectory.times), every)
    output.parent.mkdir(parents=True, exist_ok=True)
    plt.rcParams["animation.ffmpeg_path"] = imageio_ffmpeg.get_ffmpeg_exe()
    writer = FFMpegWriter(fps=fps, codec="libx264", extra_args=["-pix_fmt", "yuv420p"])
    animation = FuncAnimation(fig, draw, frames=frames, blit=False)
    total = len(frames)
    step = max(1, total // 10)

    def progress(index: int, _: int) -> None:
        if index % step == 0:
            report(f"  cuadro {index + 1}/{total}")

    animation.save(str(output), writer=writer, dpi=VIDEO_DPI, progress_callback=progress)
    plt.close(fig)
    return output


def render_still(
    run: Run, output: Path, *, time: float | None = None, arrows: bool = True
) -> Path:
    """PNG of the saved frame nearest to `time`, with no text over the room [Guia 1.7]: t and the
    parameters go beside it on the slide. The legend (scheme colours) sits below the room."""
    fig, draw = _scene(run, arrows=arrows, clock=False)
    draw(run.trajectory.nearest_frame(representative_time(run) if time is None else time))
    output.parent.mkdir(parents=True, exist_ok=True)
    fig.savefig(output, dpi=STILL_DPI, bbox_inches="tight")
    plt.close(fig)
    return output


def render_lite(run: Run, output: Path, *, arrows: bool = True) -> Path:
    """At most LITE_PAGES frames, evenly spaced, one per PDF page, for \\animategraphics."""
    fig, draw = _scene(run, arrows=arrows, clock=True)
    count = len(run.trajectory.times)
    pages = np.unique(np.linspace(0, count - 1, min(LITE_PAGES, count)).round().astype(int))
    output.parent.mkdir(parents=True, exist_ok=True)
    with PdfPages(output) as pdf:
        for frame in pages:
            draw(int(frame))
            pdf.savefig(fig)
    plt.close(fig)
    return output


def _scene(run: Run, *, arrows: bool, clock: bool) -> tuple[Figure, Callable[[int], None]]:
    """The room and a function that draws saved frame `index` onto it."""
    trajectory = run.trajectory
    side, door = run.static.side, run.static.door
    left, right = (side - door) / 2, (side + door) / 2

    fig, ax = plt.subplots(figsize=(VIDEO_SIZE_INCHES, VIDEO_SIZE_INCHES))
    ax.set_xlim(-0.5, side + 0.5)
    ax.set_ylim(-OUTSIDE, side + 0.5)
    ax.set_aspect("equal")
    ax.set_xlabel(AXIS_X)
    ax.set_ylabel(AXIS_Y)
    wall = {"color": WALL_COLOR, "linewidth": 2.5}
    ax.plot([0, left], [0, 0], **wall)
    ax.plot([right, side], [0, 0], **wall)
    ax.plot([0, 0, side, side], [0, side, side, 0], **wall)
    ax.axhline(-1.0, color="0.6", linestyle=":", linewidth=1.2)
    if run.params.scheme == "hybrid":
        zone = run.params.number("Rs")
        if 0 < zone < side * math.sqrt(2):
            ax.add_patch(
                Circle((side / 2, 0), zone, fill=False, linestyle="--", color="0.4", linewidth=1.2)
            )

    diameters = 2 * run.static.radii
    agents = EllipseCollection(
        diameters,
        diameters,
        np.zeros_like(diameters),
        units="xy",
        offsets=trajectory.positions[0],
        offset_transform=ax.transData,
        edgecolors="black",
        linewidths=0.3,
    )
    ax.add_collection(agents)
    quiver = None
    if arrows:
        first = trajectory.states[0]
        quiver = ax.quiver(
            first[:, 0], first[:, 1], first[:, 2], first[:, 3],
            angles="xy", scale_units="xy", scale=1 / ARROW_SECONDS, width=0.002, color="0.2",
        )  # fmt: skip
    handles = [
        Line2D([], [], marker="o", linestyle="", markersize=12, color=color, label=label)
        for color, label in ((EVENT_DRIVEN_COLOR, "EDMD"), (TIME_STEP_COLOR, "TSDMD"))
    ]
    ax.legend(handles=handles, loc="upper center", bbox_to_anchor=(0.5, -0.12), ncols=2)
    counter = ax.text(0.02, 0.98, "", transform=ax.transAxes, ha="left", va="top")

    def draw(frame: int) -> None:
        state = trajectory.states[frame]
        visible = trajectory.locations[frame] != RETIRED
        colors = np.where(
            trajectory.schemes[frame] == TIME_STEP, TIME_STEP_COLOR, EVENT_DRIVEN_COLOR
        )
        rgba = to_rgba_array(colors)
        rgba[~visible, 3] = 0.0
        edges = np.zeros_like(rgba)
        edges[visible, 3] = 1.0
        agents.set_offsets(state[:, 0:2])
        agents.set_facecolors(rgba)
        agents.set_edgecolors(edges)
        if quiver is not None:
            quiver.set_offsets(state[:, 0:2])
            quiver.set_UVC(
                np.where(visible, state[:, 2], 0.0), np.where(visible, state[:, 3], 0.0)
            )
        if clock:
            counter.set_text(f"t = {trajectory.times[frame]:.1f} s")

    return fig, draw
