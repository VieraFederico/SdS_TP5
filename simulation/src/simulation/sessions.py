"""Every study invocation writes to its own session folder, so runs never overwrite each other.

    output/<item>-<study>/<YYYY-MM-DD_HH-MM-SS>[_quick]/
        config.json   study parameters, reloaded when resuming or re-analyzing the session

A session is resumed or re-analyzed by name, or with `latest`.
"""

from __future__ import annotations

import itertools
import json
from datetime import datetime
from pathlib import Path
from typing import Any

from simulation.errors import ConfigError, MissingResultsError
from simulation.options import format_value, specs, with_overrides

SESSION_FORMAT = "%Y-%m-%d_%H-%M-%S"
QUICK_TAG = "_quick"
LATEST = "latest"
CONFIG_FILE = "config.json"
_STAMP_LENGTH = len(datetime(2000, 1, 1).strftime(SESSION_FORMAT))


def create_session(parent: Path, *, quick: bool, now: datetime | None = None) -> Path:
    stem = (now or datetime.now()).strftime(SESSION_FORMAT) + (QUICK_TAG if quick else "")
    parent.mkdir(parents=True, exist_ok=True)
    for attempt in itertools.count(1):
        path = parent / (stem if attempt == 1 else f"{stem}-{attempt}")
        try:
            path.mkdir()
        except FileExistsError:
            continue
        return path
    raise AssertionError("unreachable")


def is_quick(session: Path) -> bool:
    return session.name[_STAMP_LENGTH:].startswith(QUICK_TAG)


def list_sessions(parent: Path) -> list[Path]:
    """Sessions of one study, oldest first (names start with their creation time)."""
    if not parent.is_dir():
        return []
    return sorted(path for path in parent.iterdir() if (path / CONFIG_FILE).is_file())


def find_session(parent: Path, name: str, *, quick: bool | None = None) -> Path:
    """`name` is a session folder or `latest`; `quick` restricts `latest` to quick sessions."""
    sessions = list_sessions(parent)
    if name == LATEST:
        candidates = [s for s in sessions if quick is None or is_quick(s) == quick]
        if not candidates:
            kind = {None: "", True: " rápidas", False: " completas"}[quick]
            raise MissingResultsError(f"No hay sesiones{kind} en {parent}")
        return candidates[-1]
    path = parent / name
    if path not in sessions:
        available = ", ".join(s.name for s in sessions[-5:]) or "ninguna"
        raise ConfigError(f"No existe la sesión '{name}' en {parent}. Últimas: {available}")
    return path


def save_config(session: Path, config: Any, *, study: str, quick: bool) -> None:
    data = {
        "study": study,
        "quick": quick,
        "saved": datetime.now().isoformat(timespec="seconds"),
        "parameters": {
            spec.name: format_value(getattr(config, spec.name)) for spec in specs(type(config))
        },
    }
    (session / CONFIG_FILE).write_text(
        json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8"
    )


def load_config[C](session: Path, config_type: type[C]) -> tuple[C, bool]:
    """Parameters saved in the session and whether it was a quick session."""
    try:
        data = json.loads((session / CONFIG_FILE).read_text(encoding="utf-8"))
        parameters = data["parameters"]
        quick = bool(data["quick"])
    except (OSError, ValueError, KeyError) as error:
        raise ConfigError(f"{session / CONFIG_FILE} ilegible: {error}") from error
    return with_overrides(config_type(), parameters), quick
