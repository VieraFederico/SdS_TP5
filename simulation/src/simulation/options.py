"""Study parameters are frozen dataclasses; this module turns their fields into editable text.

Each field declares `metadata=option("help", choices=...)`. The same description drives the
argparse flags and the interactive questionary prompts.
"""

from __future__ import annotations

import dataclasses
from collections.abc import Mapping
from typing import Any, get_args, get_origin, get_type_hints

from simulation.errors import ConfigError

_TRUE = frozenset({"true", "1", "si", "sí", "s", "yes", "y"})
_FALSE = frozenset({"false", "0", "no", "n"})


def option(help: str, *, choices: tuple[str, ...] | None = None) -> dict[str, Any]:
    return {"help": help, "choices": choices}


@dataclasses.dataclass(frozen=True)
class OptionSpec:
    name: str
    help: str
    hint: Any
    choices: tuple[str, ...] | None
    default: Any = None

    @property
    def flag(self) -> str:
        return "--" + self.name.replace("_", "-")

    @property
    def is_bool(self) -> bool:
        return self.hint is bool


def specs(config_type: type) -> list[OptionSpec]:
    hints = get_type_hints(config_type)
    return [
        OptionSpec(
            name=field.name,
            help=field.metadata.get("help", field.name),
            hint=hints[field.name],
            choices=field.metadata.get("choices"),
            default=field.default,
        )
        for field in dataclasses.fields(config_type)
    ]


def format_value(value: Any) -> str:
    if isinstance(value, tuple):
        return ", ".join(format_value(item) for item in value)
    if isinstance(value, bool):
        return "sí" if value else "no"
    if isinstance(value, float):
        return repr(value)
    return str(value)


def parse_value(spec: OptionSpec, text: str) -> Any:
    if get_origin(spec.hint) is tuple:
        element = get_args(spec.hint)[0]
        items = [item.strip() for item in text.split(",") if item.strip()]
        if not items and spec.default != ():
            raise ConfigError(f"{spec.flag}: se esperaba al menos un valor")
        return tuple(_parse_scalar(spec, element, item) for item in items)
    return _parse_scalar(spec, spec.hint, text.strip())


def with_overrides[C](config: C, overrides: Mapping[str, str]) -> C:
    by_name = {spec.name: spec for spec in specs(type(config))}
    unknown = set(overrides) - set(by_name)
    if unknown:
        raise ConfigError(f"Parámetros desconocidos: {', '.join(sorted(unknown))}")
    values = {name: parse_value(by_name[name], text) for name, text in overrides.items()}
    return dataclasses.replace(config, **values)  # type: ignore[type-var]


def _parse_scalar(spec: OptionSpec, hint: Any, text: str) -> Any:
    try:
        if hint is bool:
            lowered = text.lower()
            if lowered not in _TRUE | _FALSE:
                raise ValueError(text)
            value: Any = lowered in _TRUE
        elif hint is int:
            value = int(text)
        elif hint is float:
            value = float(text)
        else:
            value = text
    except ValueError as error:
        raise ConfigError(f"{spec.flag}: valor inválido '{text}'") from error
    if spec.choices is not None and value not in spec.choices:
        raise ConfigError(f"{spec.flag}: '{text}' no está entre {', '.join(spec.choices)}")
    return value
