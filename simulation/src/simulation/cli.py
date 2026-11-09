"""Entry point: `simulation` opens the interactive menu; `simulation <study> [...]` runs a study."""

from __future__ import annotations

import argparse
import io
import sys
from collections.abc import Sequence
from pathlib import Path
from typing import Any

import questionary
from questionary import Choice, Separator

from simulation.batch import ASSIGNMENT, run_batch
from simulation.engine import compile_engine, default_jobs, run_engine_checks
from simulation.errors import ConfigError, SimulationError
from simulation.export import export_figures
from simulation.options import OptionSpec, format_value, parse_value, specs, with_overrides
from simulation.paths import OUTPUT_ROOT, PRESENTATION_FIGURES
from simulation.plotting import apply_style
from simulation.reader import DYNAMIC_FILE, Run
from simulation.sessions import CONFIG_FILE, LATEST, list_sessions
from simulation.studies import STUDIES, Study
from simulation.studies.base import open_session

PARAM_PREFIX = "param_"
ANIMATIONS_DIR = "animations"


def main(argv: Sequence[str] | None = None) -> int:
    for stream in (sys.stdout, sys.stderr):
        if isinstance(stream, io.TextIOWrapper):
            stream.reconfigure(errors="replace")
    apply_style()
    args = _parser().parse_args(argv)
    # Not a parser default: a subparser would overwrite `simulation --output X <command>`.
    args.output = getattr(args, "output", OUTPUT_ROOT)
    try:
        match args.command:
            case None | "menu":
                _interactive(args.output)
            case "build":
                compile_engine(force=True)
                print("Motor compilado.")
                if args.check:
                    run_engine_checks()
            case "animate":
                _animate(args.run, args.stem, fps=args.fps, every=args.every, arrows=args.arrows)
            case "figures":
                keys = _split(args.studies) if args.studies else list(STUDIES)
                copied = export_figures(keys, args.output, session=args.session)
                print(f"{len(copied)} figura(s) copiadas a {PRESENTATION_FIGURES}")
            case "all":
                keys = _split(args.studies) if args.studies else ASSIGNMENT
                ok = run_batch(
                    keys, args.output, quick=args.quick, animations=args.animations, jobs=args.jobs
                )
                return 0 if ok else 1
            case key:
                _run_from_args(STUDIES[key], args)
    except SimulationError as error:
        print(f"Error: {error}", file=sys.stderr)
        return 1
    except KeyboardInterrupt:
        print("\nInterrumpido.", file=sys.stderr)
        return 130
    return 0


def _parser() -> argparse.ArgumentParser:
    # SUPPRESS keeps a subcommand's missing --output from overriding `simulation --output X ...`.
    common = argparse.ArgumentParser(add_help=False)
    common.add_argument(
        "--output",
        type=Path,
        default=argparse.SUPPRESS,
        help=f"Carpeta raíz de resultados (por defecto {OUTPUT_ROOT})",
    )
    parser = argparse.ArgumentParser(
        prog="simulation",
        parents=[common],
        description="Simulaciones (motor Java), análisis, figuras y animaciones.",
    )
    commands = parser.add_subparsers(dest="command", metavar="COMANDO")
    commands.add_parser("menu", parents=[common], help="Menú interactivo (por defecto)")
    build = commands.add_parser("build", help="Compilar el motor Java")
    build.add_argument("--check", action="store_true", help="Además correr EngineChecks")

    figures = commands.add_parser(
        "figures",
        parents=[common],
        help="Copiar las figuras de la última sesión completa a presentation/figuras/<ítem>/",
    )
    figures.add_argument("--studies", metavar="LISTA", help="Estudios separados por coma")
    figures.add_argument(
        "--session", default=LATEST, metavar="NOMBRE", help=f"Sesión a copiar ('{LATEST}')"
    )

    animate = commands.add_parser(
        "animate", help="Animar una corrida existente (run-NNN) en MP4, GIF y PNG"
    )
    animate.add_argument("run", type=Path, help="Carpeta run-NNN con static.txt y dynamic.txt")
    animate.add_argument(
        "--stem",
        type=Path,
        help=f"Ruta sin extensión de los archivos (por defecto en <sesión>/{ANIMATIONS_DIR}/)",
    )
    animate.add_argument("--fps", type=int, default=30)
    animate.add_argument("--every", type=int, default=1, help="Usar uno de cada n cuadros")
    animate.add_argument("--no-arrows", dest="arrows", action="store_false")

    batch = commands.add_parser(
        "all",
        parents=[common],
        help="Corrida completa: varios estudios (por defecto todo el TP), una sesión cada uno",
    )
    batch.add_argument(
        "--studies",
        metavar="LISTA",
        help=f"Estudios separados por coma, en el orden del enunciado [{', '.join(ASSIGNMENT)}]",
    )
    batch.add_argument("--quick", action="store_true", help="Parámetros de prueba rápida")
    batch.add_argument(
        "--no-animations",
        dest="animations",
        action="store_false",
        help="No generar la animación (MP4, GIF y PNG) al final",
    )
    batch.add_argument(
        "--jobs", type=int, default=default_jobs(), help="Procesos Java en paralelo"
    )

    for study in STUDIES.values():
        sub = commands.add_parser(study.key, parents=[common], help=study.title)
        sub.add_argument(
            "--quick",
            action="store_true",
            help="Sesión nueva con parámetros de prueba rápida (o la última rápida con --session)",
        )
        sub.add_argument(
            "--session",
            metavar="NOMBRE",
            help=f"Retomar o re-analizar una sesión de output/{study.folder}/ (o '{LATEST}')",
        )
        mode = sub.add_mutually_exclusive_group()
        mode.add_argument("--simulate-only", action="store_true")
        mode.add_argument(
            "--analyze-only",
            action="store_true",
            help=f"Sin simular; sin --session analiza la sesión '{LATEST}'",
        )
        sub.add_argument(
            "--jobs", type=int, default=default_jobs(), help="Procesos Java en paralelo"
        )
        sub.add_argument(
            "--overwrite",
            action="store_true",
            help="Al retomar con otros parámetros, reemplazar los casos que no coinciden",
        )
        defaults = study.full()
        for spec in specs(study.config_type):
            sub.add_argument(
                spec.flag,
                dest=PARAM_PREFIX + spec.name,
                metavar="VALOR",
                help=f"{spec.help} [{format_value(getattr(defaults, spec.name))}]",
            )
    return parser


def _run_from_args(study: Study[Any], args: argparse.Namespace) -> None:
    overrides = {
        name.removeprefix(PARAM_PREFIX): value
        for name, value in vars(args).items()
        if name.startswith(PARAM_PREFIX) and value is not None
    }
    config, context = open_session(
        study,
        args.output,
        session=args.session or (LATEST if args.analyze_only else None),
        quick=args.quick,
        overrides=overrides,
        jobs=args.jobs,
        overwrite=args.overwrite,
    )
    print(f"{study.title}\nSesión: {context.output}")
    study.execute(config, context, simulate=not args.analyze_only, analyze=not args.simulate_only)


def _default_stem(run_dir: Path) -> Path:
    """<session>/animations/<case>__run-NNN, or next to the run outside a session."""
    run_dir = run_dir.resolve()
    for session in run_dir.parents:
        if (session / CONFIG_FILE).is_file():
            parts = run_dir.relative_to(session).parts
            label = "__".join(parts[1:] if parts[0] == "runs" else parts)
            return session / ANIMATIONS_DIR / label
    return run_dir / "animation"


def _animate(run_dir: Path, stem: Path | None, *, fps: int, every: int, arrows: bool) -> None:
    if not (run_dir / DYNAMIC_FILE).is_file():
        raise ConfigError(f"{run_dir} no contiene {DYNAMIC_FILE}")
    from simulation.animation import render_animation  # imageio-ffmpeg only when animating

    outputs = render_animation(
        Run(run_dir), stem or _default_stem(run_dir), fps=fps, every=every, arrows=arrows
    )
    for output in outputs:
        print(f"  archivo: {output}")


def _split(text: str) -> list[str]:
    return [item.strip() for item in text.split(",") if item.strip()]


def _interactive(root: Path) -> None:
    while True:
        choices: list[Any] = [
            Choice("Corrida completa del TP (todos los estudios)", value="all"),
            Choice("Elegir varios estudios", value="several"),
            Separator(),
        ]
        choices += [Choice(study.title, value=key) for key, study in STUDIES.items()]
        choices += [
            Separator(),
            Choice("Animar una corrida existente", value="animate"),
            Choice("Copiar las figuras a la presentación", value="figures"),
            Choice("Compilar y verificar el motor Java", value="build"),
            Choice("Salir", value="exit"),
        ]
        selected = questionary.select("¿Qué querés hacer?", choices=choices).ask()
        if selected in (None, "exit"):
            return
        try:
            if selected == "build":
                compile_engine(force=True)
                run_engine_checks()
            elif selected == "animate":
                _interactive_animation(root)
            elif selected == "figures":
                export_figures(list(STUDIES), root)
            elif selected == "all":
                _interactive_batch(root, ASSIGNMENT)
            elif selected == "several":
                _interactive_batch(root, None)
            else:
                _interactive_study(STUDIES[selected], root)
        except SimulationError as error:
            print(f"Error: {error}")


def _interactive_batch(root: Path, keys: Sequence[str] | None) -> None:
    """`keys=None` lets the user pick the studies; otherwise runs exactly `keys`."""
    if keys is None:
        keys = questionary.checkbox(
            "Estudios (espacio para marcar, enter para confirmar)",
            choices=[Choice(STUDIES[key].title, value=key) for key in ASSIGNMENT],
        ).ask()
        if keys is None:
            return
    preset = questionary.select(
        "Parámetros",
        choices=[
            Choice("Los del enunciado", value="full"),
            Choice("Prueba rápida", value="quick"),
        ],
    ).ask()
    if preset is None:
        return
    animations = questionary.confirm(
        "¿Generar la animación (MP4, GIF y PNG) al final?", default=True
    ).ask()
    if animations is None:
        return
    if not keys and not animations:
        print("No se eligió ningún estudio ni la animación.")
        return
    jobs = _ask_jobs()
    if jobs is None:
        return
    run_batch(keys, root, quick=preset == "quick", animations=animations, jobs=jobs)


def _interactive_study(study: Study[Any], root: Path) -> None:
    sessions = list_sessions(root / study.folder)
    choices = [
        Choice("Sesión nueva con parámetros del enunciado", value="full"),
        Choice("Sesión nueva rápida (prueba corta)", value="quick"),
    ]
    if sessions:
        choices.append(Choice("Retomar o re-analizar una sesión existente", value="existing"))
    mode = questionary.select("Sesión", choices=choices).ask()
    if mode is None:
        return
    if mode == "existing":
        _interactive_existing(study, root, sessions)
        return
    quick = mode == "quick"
    overrides = _edit_config(study.quick() if quick else study.full())
    if overrides is None:
        return
    action = questionary.select(
        "Acción",
        choices=[
            Choice("Simular y analizar", value="both"),
            Choice("Sólo simular", value="simulate"),
        ],
    ).ask()
    jobs = _ask_jobs() if action else None
    if jobs is None:
        return
    config, context = open_session(
        study, root, session=None, quick=quick, overrides=overrides, jobs=jobs
    )
    print(f"Sesión: {context.output}")
    study.execute(config, context, simulate=True, analyze=action == "both")


def _interactive_existing(study: Study[Any], root: Path, sessions: list[Path]) -> None:
    name = questionary.select(
        "Sesión (más reciente primero)", choices=[path.name for path in reversed(sessions)]
    ).ask()
    if name is None:
        return
    action = questionary.select(
        "Acción",
        choices=[
            Choice("Sólo analizar", value="analyze"),
            Choice("Completar simulaciones faltantes y analizar", value="both"),
        ],
    ).ask()
    if action is None:
        return
    jobs = _ask_jobs() if action == "both" else 1
    if jobs is None:
        return
    config, context = open_session(study, root, session=name, quick=False, overrides={}, jobs=jobs)
    print(f"Sesión: {context.output}")
    for spec in specs(type(config)):
        print(f"  {spec.help}: {format_value(getattr(config, spec.name))}")
    study.execute(config, context, simulate=action == "both", analyze=True)


def _ask_jobs() -> int | None:
    answer = questionary.text(
        "Procesos Java en paralelo",
        default=str(default_jobs()),
        validate=lambda text: text.isdigit() and int(text) > 0,
    ).ask()
    return None if answer is None else int(answer)


def _edit_config(config: Any) -> dict[str, str] | None:
    """Overrides chosen by the user (empty to keep `config`), or None if cancelled."""
    fields = specs(type(config))
    for spec in fields:
        print(f"  {spec.help}: {format_value(getattr(config, spec.name))}")
    edit = questionary.confirm("¿Modificar parámetros?", default=False).ask()
    if edit is None:
        return None
    while edit:
        overrides = {}
        for spec in fields:
            answer = _ask_option(spec, getattr(config, spec.name))
            if answer is None:
                return None
            overrides[spec.name] = answer
        try:
            with_overrides(config, overrides)
        except ConfigError as error:
            print(f"Error: {error}")
            continue
        return overrides
    return {}


def _ask_option(spec: OptionSpec, current: Any) -> str | None:
    if spec.choices is not None and isinstance(current, tuple):
        picked = questionary.checkbox(
            spec.help,
            choices=[Choice(c, value=c, checked=c in current) for c in spec.choices],
            validate=lambda selection: bool(selection) or "Elegir al menos uno",
        ).ask()
        return None if picked is None else ", ".join(picked)
    if spec.choices is not None:
        return questionary.select(spec.help, choices=list(spec.choices), default=current).ask()
    if spec.is_bool:
        answer = questionary.confirm(spec.help, default=current).ask()
        return None if answer is None else str(answer).lower()

    def valid(text: str) -> bool | str:
        try:
            parse_value(spec, text)
        except ConfigError as error:
            return str(error)
        return True

    return questionary.text(spec.help, default=format_value(current), validate=valid).ask()


def _interactive_animation(root: Path) -> None:
    runs = sorted(path.parent for path in root.glob(f"**/run-*/{DYNAMIC_FILE}"))
    if not runs:
        raise ConfigError(f"No hay corridas en {root}")
    labels = {str(path.relative_to(root)): path for path in runs}
    picked = questionary.autocomplete(
        "Corrida (escribir para filtrar)",
        choices=list(labels),
        validate=lambda text: text in labels or "Elegir una corrida de la lista",
    ).ask()
    if picked is None:
        return
    fps = questionary.text("Cuadros por segundo", default="30", validate=str.isdigit).ask()
    every = questionary.text("Usar uno de cada n cuadros", default="1", validate=str.isdigit).ask()
    arrows = questionary.confirm("¿Dibujar velocidades?", default=True).ask()
    if fps is None or every is None or arrows is None:
        return
    _animate(labels[picked], None, fps=int(fps), every=int(every), arrows=arrows)
