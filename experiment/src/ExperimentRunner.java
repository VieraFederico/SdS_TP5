import models.Agent;
import models.PedestrianModel;
import models.Room;
import models.SocialForceModel;
import schemes.EventDrivenScheme;
import schemes.HybridScheme;
import schemes.Scheme;
import schemes.TimeStepScheme;
import schemes.ZoneSkinFlightCriterion;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ExperimentRunner {
    private ExperimentRunner() {
    }

    public static void run(RunConfig config) throws IOException {
        for (int i = 0; i < config.repeats(); i++) {
            if (Files.exists(config.runDirectory(i), LinkOption.NOFOLLOW_LINKS))
                throw new IllegalArgumentException("Refusing existing run directory: " + config.runDirectory(i));
        }
        Files.createDirectories(config.out());
        for (int i = 0; i < config.warmup(); i++) {
            long seed = Math.addExact(config.seed(), (long) config.repeats() + i);
            scheme(config, agents(config, seed)).run(time -> { });
        }
        for (int i = 0; i < config.repeats(); i++) {
            var directory = config.runDirectory(i);
            long seed = Math.addExact(config.seed(), i);
            List<Agent> agents = agents(config, seed);
            Scheme scheme = scheme(config, agents); // validates everything before any file is created
            Files.createDirectory(directory);
            boolean complete;
            long elapsed;
            try (OutputWriter output = new OutputWriter(directory, room(config), agents)) {
                // Wall-clock around the loop: the one measure only the engine can take (ítem c).
                long started = System.nanoTime();
                complete = scheme.run(time -> output.frame(time, agents));
                elapsed = System.nanoTime() - started;
                output.exits(agents);
            }
            OutputWriter.parameters(directory, config, seed, complete, scheme.steps(), scheme.events(), elapsed);
            System.out.println("Completed " + directory + " (seed=" + seed + ", complete=" + complete + ")");
        }
    }

    private static Room room(RunConfig config) {
        return new Room(config.side(), config.door());
    }

    private static List<Agent> agents(RunConfig config, long seed) throws IOException {
        if (config.initial() != null) return initialAgents(config);
        return room(config).placeAgents(config.n(), SocialForceModel.MIN_RADIUS, SocialForceModel.MAX_RADIUS,
            SocialForceModel.MASS, new Random(seed));
    }

    /**
     * --initial: agents placed by hand, for the demonstration scenes of the switching criteria (note of the
     * professor). Same checks as the random placement: inside the room, no overlaps, radii of the model.
     */
    private static List<Agent> initialAgents(RunConfig config) throws IOException {
        Room room = room(config);
        List<Agent> agents = new ArrayList<>();
        for (String line : Files.readAllLines(config.initial())) {
            if (line.isBlank() || line.startsWith("#")) continue;
            String[] parts = line.trim().split("\\s+");
            if (parts.length != 3) throw new IllegalArgumentException("--initial: need 'x y R' per line: " + line);
            double x = Double.parseDouble(parts[0]), y = Double.parseDouble(parts[1]), r = Double.parseDouble(parts[2]);
            if (r < SocialForceModel.MIN_RADIUS || r > SocialForceModel.MAX_RADIUS)
                throw new IllegalArgumentException("--initial: radius outside the model range: " + line);
            if (x < r || x > room.side - r || y < r || y > room.side - r)
                throw new IllegalArgumentException("--initial: agent outside the room: " + line);
            Agent agent = new Agent(agents.size(), r, SocialForceModel.MASS, x, y);
            for (Agent other : agents)
                if (agent.gap(other) <= 0) throw new IllegalArgumentException("--initial: overlapping agents: " + line);
            agents.add(agent);
        }
        if (agents.size() != config.n())
            throw new IllegalArgumentException("--initial: " + agents.size() + " agents, but --N " + config.n());
        return agents;
    }

    /** One grid for the three schemes: cells of 2 r_max + δ_out, which S2 of the hybrid needs. */
    private static Scheme scheme(RunConfig config, List<Agent> agents) {
        PedestrianModel model = new SocialForceModel(config.kt());
        double cutoff = 2 * SocialForceModel.MAX_RADIUS + config.skinOut();
        return switch (config.scheme()) {
            case "event-driven" -> new EventDrivenScheme(room(config), agents, model, cutoff, config.maxTime(),
                config.maxEvents(), config.pairRestitution(), config.wallRestitution(), config.separationSpeed(),
                config.dt(), config.dumpEvery());
            case "hybrid" -> new HybridScheme(room(config), agents, model, cutoff, config.maxTime(), config.maxEvents(),
                config.pairRestitution(), config.wallRestitution(), config.separationSpeed(), config.dt(),
                config.dumpEvery(), new ZoneSkinFlightCriterion(config.zoneRadius(), config.flightThreshold(),
                config.skinIn(), config.skinOut(), config.hysteresis(), config.tolerance(),
                model.desiredSpeed()), config.maxSpeed());
            default -> new TimeStepScheme(room(config), agents, model, cutoff, config.maxTime(), config.dt(),
                config.dumpEvery());
        };
    }
}
