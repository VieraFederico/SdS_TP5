"""Expected failures, reported to the CLI user without a traceback."""


class SimulationError(Exception):
    """Base for every failure the user can act on."""


class ConfigError(SimulationError):
    """A study or engine parameter is invalid."""


class EngineError(SimulationError):
    """The Java engine could not be compiled or a simulation failed."""


class OutputFormatError(SimulationError):
    """A simulation output file does not follow the expected format."""


class MissingResultsError(SimulationError):
    """An analysis needs simulation results that do not exist yet."""
