import models.SocialForceModel;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/** CLI and numerical/output settings; physical constants stay in the models. */
public record RunConfig(String scheme, int n, double side, double door, double dt, int dumpEvery,
                        double maxTime, long maxEvents, double kt, double flightThreshold,
                        int repeats, int warmup, long seed, Path out, Path initial, boolean help) {
    private static final Set<String> VALUES = Set.of("--scheme", "--N", "--L", "--door", "--dt", "--dump-every",
        "--max-time", "--max-events", "--kt", "--ts", "--repeats", "--warmup", "--seed", "--out", "--initial");

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
        double kt = number(flags, "--kt", 0);
        double flightThreshold = number(flags, "--ts", SocialForceModel.contactDuration(SocialForceModel.MASS / 2));
        int repeats = Integer.parseInt(flags.getOrDefault("--repeats", "1"));
        int warmup = Integer.parseInt(flags.getOrDefault("--warmup", "0"));
        long seed = Long.parseLong(flags.getOrDefault("--seed", "1"));
        if (n < 1 || dt <= 0 || maxTime <= 0 || dumpEvery < 1 || repeats < 1 || warmup < 0)
            throw new IllegalArgumentException("Positive N, dt, max-time, dump-every and repeats required");
        if (maxEvents < 1) throw new IllegalArgumentException("Positive max-events required");
        if (flightThreshold < 0) throw new IllegalArgumentException("Need ts >= 0");
        return new RunConfig(scheme, n, side, door, dt, dumpEvery, maxTime, maxEvents, kt, flightThreshold,
            repeats, warmup, seed, Path.of(flags.getOrDefault("--out", "output/experiment")),
            flags.containsKey("--initial") ? Path.of(flags.get("--initial")) : null, flags.containsKey("--help"));
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
            Event-driven and hybrid: elastic collisions of equal masses, as in the TP3 (no parameters)
            Hybrid: --ts SECONDS (two contacts closer than ts: time-step; ts without contacts: back to
                    event-driven; touching a time-step agent: time-step)
            --repeats INTEGER --warmup INTEGER --seed LONG --out PATH
            --initial PATH (one line 'x y R' per agent instead of random placement; N lines; v(0) = vd e as always)
            --help
            Defaults: time-step, N=200, L=50, door=0.8, kt=0, dt=0.001, dump-every=500, max-time=1e5 (safety
            only: complete=false in params.txt if reached), ts=pi/omega=0.0574, repeats=1, warmup=0, seed=1.
            Pure EDMD inside the hybrid: --ts 0.
            Every scheme uses cells of at least 2 r_max, so the three share one grid.
            Warmup runs are unmeasured, in the same JVM, and write no files.
            """;
    }
}
