import models.SocialForceModel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** CLI and numerical/output settings; physical constants stay in the models. */
public record RunConfig(String scheme, int n, double side, double door, double dt, int dumpEvery,
                        double maxTime, long maxEvents, double pairRestitution, double wallRestitution,
                        double separationSpeed, double kt, double zoneRadius, double flightThreshold,
                        double skinIn, double skinOut, double hysteresis, double tolerance, double maxSpeed,
                        int repeats, int warmup, long seed, Path out, boolean help) {
    private static final Set<String> VALUES = Set.of("--scheme", "--N", "--L", "--door", "--dt", "--dump-every",
        "--max-time", "--max-events", "--e-pair", "--e-wall", "--v-sep", "--kt", "--Rs", "--ts", "--delta-in",
        "--delta-out", "--h", "--eps", "--v-max", "--repeats", "--warmup", "--seed", "--out");

    public static RunConfig parse(String[] args) {
        Map<String, String> flags = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            String key = args[i];
            if (!VALUES.contains(key) && !key.equals("--help"))
                throw new IllegalArgumentException("Unknown flag: " + key);
            if (flags.containsKey(key)) throw new IllegalArgumentException("Duplicate flag: " + key);
            if (VALUES.contains(key)) {
                if (++i == args.length || args[i].startsWith("--"))
                    throw new IllegalArgumentException("Missing value for " + key);
                flags.put(key, args[i]);
            } else {
                flags.put(key, "true");
            }
        }
        String scheme = flags.getOrDefault("--scheme", "time-step");
        if (!Set.of("time-step", "event-driven", "hybrid").contains(scheme))
            throw new IllegalArgumentException("Unknown scheme: " + scheme);
        int n = Integer.parseInt(flags.getOrDefault("--N", "200"));
        double side = number(flags, "--L", 50), door = number(flags, "--door", .8);
        double dt = number(flags, "--dt", 1e-3), maxTime = number(flags, "--max-time", 1e5);
        int dumpEvery = Integer.parseInt(flags.getOrDefault("--dump-every", "500"));
        long maxEvents = Long.parseLong(flags.getOrDefault("--max-events", "20000000000"));
        double pairRestitution = number(flags, "--e-pair", SocialForceModel.restitution(SocialForceModel.MASS / 2));
        double wallRestitution = number(flags, "--e-wall", SocialForceModel.restitution(SocialForceModel.MASS));
        double separationSpeed = number(flags, "--v-sep", .02);
        double kt = number(flags, "--kt", 0);
        double zoneRadius = number(flags, "--Rs", 5);
        double flightThreshold = number(flags, "--ts", SocialForceModel.contactDuration(SocialForceModel.MASS / 2));
        double skinIn = number(flags, "--delta-in", .05), skinOut = number(flags, "--delta-out", .25);
        double hysteresis = number(flags, "--h", 1), tolerance = number(flags, "--eps", .05);
        double maxSpeed = number(flags, "--v-max", 5);
        int repeats = Integer.parseInt(flags.getOrDefault("--repeats", "1"));
        int warmup = Integer.parseInt(flags.getOrDefault("--warmup", "0"));
        long seed = Long.parseLong(flags.getOrDefault("--seed", "1"));
        if (n < 1 || dt <= 0 || maxTime <= 0 || dumpEvery < 1 || repeats < 1 || warmup < 0)
            throw new IllegalArgumentException("Positive N, dt, max-time, dump-every and repeats required");
        if (maxSpeed <= 0 || maxEvents < 1 || separationSpeed < 0)
            throw new IllegalArgumentException("Positive v-max and max-events, nonnegative v-sep required");
        if (pairRestitution < 0 || pairRestitution > 1 || wallRestitution < 0 || wallRestitution > 1)
            throw new IllegalArgumentException("Restitution must be in [0, 1]");
        // delta-out sets the cells of every scheme (2 r_max + delta-out), so it is checked for all of them.
        if (skinIn <= 0 || skinOut <= skinIn) throw new IllegalArgumentException("Need 0 < delta-in < delta-out");
        return new RunConfig(scheme, n, side, door, dt, dumpEvery, maxTime, maxEvents, pairRestitution, wallRestitution,
            separationSpeed, kt, zoneRadius, flightThreshold, skinIn, skinOut, hysteresis, tolerance, maxSpeed,
            repeats, warmup, seed, Path.of(flags.getOrDefault("--out", "output/experiment")), flags.containsKey("--help"));
    }

    private static double number(Map<String, String> flags, String key, double fallback) {
        double value = Double.parseDouble(flags.getOrDefault(key, Double.toString(fallback)));
        if (!Double.isFinite(value)) throw new IllegalArgumentException("Nonfinite " + key);
        return value;
    }

    public Path runDirectory(int index) {
        return out.resolve(String.format(Locale.ROOT, "run-%03d", index + 1));
    }

    public static String usage() {
        return """
            TP5 engine (static.txt, dynamic.txt, exits.txt, params.txt per realization)
            --scheme time-step|event-driven|hybrid
            --N INTEGER --L METERS --door METERS --kt NUMBER (tangential friction; 0 = none)
            --dt SECONDS --dump-every INTEGER (dt2 = dump-every * dt) --max-time SECONDS --max-events LONG
            Event-driven and hybrid: --e-pair NUMBER --e-wall NUMBER --v-sep METERS_PER_SECOND
            Hybrid: --Rs METERS (zone) --ts SECONDS (minimum flight) --delta-in METERS --delta-out METERS
                    --h METERS (zone hysteresis) --eps NUMBER (relaxed velocity) --v-max METERS_PER_SECOND
            --repeats INTEGER --warmup INTEGER --seed LONG --out PATH
            --help
            Defaults: time-step, N=200, L=50, door=0.8, kt=0, dt=0.001, dump-every=500, max-time=1e5 (safety
            only: complete=false in params.txt if reached), Rs=5, ts=pi/omega=0.0574, delta-in=0.05,
            delta-out=0.25, h=1, eps=0.05, v-max=5, repeats=1, warmup=0, seed=1.
            Pure EDMD inside the hybrid: --Rs 0 --ts 0. Pure TSDMD inside the hybrid: --Rs 56 (L = 50).
            Every scheme uses cells of at least 2 r_max + delta-out, so the three share one grid.
            Warmup runs are unmeasured, in the same JVM, and write no files.
            """;
    }
}
