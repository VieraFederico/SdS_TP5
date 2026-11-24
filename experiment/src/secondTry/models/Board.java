package secondTry.models;

import secondTry.models.events.*;

import java.io.IOException;
import java.util.*;

/** Binary hard-disc events with lazy invalidation. No fixed time steps. */
public class Board {
    public static final double L = 50;
    public static final double GATE = 0.8;
    public static final double MIN_RADIUS = 0.18;
    public static final double MAX_RADIUS = 0.25;
    public static final double DESIRED_SPEED = 1.5;
    public static final double RELAX_TIME = 0.5;
    public static final double MASS = 80;
    // Larger than 2*MAX_RADIUS and divides L and L with an integer result.
    private static final double GRID_CELL = 0.5;

    private final Queue<Event> events = new PriorityQueue<>();
    private final List<Particle> particles = new ArrayList<>();
    private int usedCount = 0;
    private long eventCount;
    private double time;

    @FunctionalInterface
    public interface SnapshotSink {
        void write(long index, double time, List<Particle> particles, int usedCount) throws IOException;
    }

    public Board(int n) {
        this(n, 1);
    }

    public Board(int n, long seed) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        Random random = new Random(seed);
        Map<Integer, Map<Integer, Set<Particle>>> grid = new HashMap<>();
        for (int i = 0; i < n; i++) {
            boolean placed = false;
            for (int attempt = 0; attempt < 100_000; attempt++) {
                Particle particle = generateParticle(grid, random);
                if (particle == null) {
                    continue;
                }
                particles.add(particle);
                placed = true;
                break;
            }
            if (!placed) {
                throw new IllegalArgumentException("Unable to place exactly N=" + n + " particles");
            }
        }
        initializeEvents();
    }

    /** Explicit initial states for deterministic diagnostic scenarios. */
    public Board(List<Particle> initial) {
        if (initial.isEmpty()) {
            throw new IllegalArgumentException("Empty particle list");
        }
        particles.addAll(initial);
        for (int i = 0; i < particles.size(); i++) {
            Particle particle = particles.get(i);
            if (outside(particle)) {
                throw new IllegalArgumentException("Particle outside table");
            }
            for (int j = 0; j < i; j++) {
                if (particle.overlaps(particles.get(j))) {
                    throw new IllegalArgumentException("Particles overlap");
                }
            }
        }
        initializeEvents();
    }

    private static boolean outside(Particle one) {
        return one.getX() - one.getR() < 0 || one.getX() + one.getR() > L
                || one.getY() - one.getR() < 0 || one.getY() + one.getR() > L;
    }

    private Particle generateParticle(Map<Integer, Map<Integer, Set<Particle>>> grid, Random random) {
        double radius = random.nextDouble(MIN_RADIUS, MAX_RADIUS);
        double intendedX = random.nextDouble(radius, L - radius);
        double intendedY = random.nextDouble(radius, L - radius);
        int indexX = (int) (intendedX / GRID_CELL);
        int indexY = (int) (intendedY / GRID_CELL);
        if (neighborhoodOverlaps(grid.getOrDefault(indexX - 1, new HashMap<>()), intendedX, intendedY, indexY, radius)) {
            return null;
        }
        if (neighborhoodOverlaps(grid.getOrDefault(indexX, new HashMap<>()), intendedX, intendedY, indexY, radius)) {
            return null;
        }
        if (neighborhoodOverlaps(grid.getOrDefault(indexX + 1, new HashMap<>()), intendedX, intendedY, indexY, radius)) {
            return null;
        }
        grid.putIfAbsent(indexX, new HashMap<>());
        grid.get(indexX).putIfAbsent(indexY, new HashSet<>());
        Particle particle = new Particle(intendedX, intendedY, radius, random.nextDouble(Math.PI * 2), DESIRED_SPEED);
        grid.get(indexX).get(indexY).add(particle);
        return particle;
    }

    private boolean neighborhoodOverlaps(Map<Integer, Set<Particle>> column, double intendedX, double intendedY, int indexY, double radius) {
        if (boxOverlaps(column.getOrDefault(indexY - 1, new HashSet<>()), intendedX, intendedY, radius)) {
            return true;
        }
        if (boxOverlaps(column.getOrDefault(indexY + 1, new HashSet<>()), intendedX, intendedY, radius)) {
            return true;
        }
        return boxOverlaps(column.getOrDefault(indexY, new HashSet<>()), intendedX, intendedY, radius);
    }

    private boolean boxOverlaps(Set<Particle> occupants, double intendedX, double intendedY, double radius) {
        for (Particle particle : occupants) {
            if (particle.overlaps(intendedX, intendedY, radius)) {
                return true;
            }
        }
        return false;
    }

    private void initializeEvents() {
        for (Particle particle : particles) {
            loadEvents(particle);
        }
    }

    public List<Particle> getParticles() {
        return Collections.unmodifiableList(particles);
    }

    public void simulate(double limit) {
        try {
            simulate(limit,1000, (_, _, _, _) -> { });
        } catch (IOException impossible) {
            throw new IllegalStateException(impossible);
        }
    }

    public void simulate(double limit, int dumpEvery, SnapshotSink sink) throws IOException {
        Objects.requireNonNull(sink, "snapshot sink");
        if (!Double.isFinite(limit) || limit <= 0) {
            throw new IllegalArgumentException("tmax must be positive");
        }
        if (dumpEvery < 1) {
            throw new IllegalArgumentException("dumpEvery must be an integer >= 1");
        }
        sink.write(0, 0, getParticles(),usedCount);
        long lastDumpedIndex = 0;
        while (!events.isEmpty()) {
            Event event = events.poll();
            if (event.wasInvalidated()) {
                continue;
            }
            double next = event.getTime();
            if (next > limit) {
                break;
            }
            if (next < time) {
                throw new IllegalStateException("Event in the past");
            }
            time = next;
            for (Particle particle : particles) {
                particle.update(time);
            }
            boolean goal = event.bounce();
            eventCount++;
            if (goal) {
                usedCount++;
            }
            if (goal || eventCount % dumpEvery == 0) {
                sink.write(eventCount, time, getParticles(), usedCount);
                lastDumpedIndex = eventCount;
            }
            for (Particle particle : event.getParticles()) {
                loadEvents(particle);
            }
        }
        if (eventCount != lastDumpedIndex) {
            sink.write(eventCount, time, getParticles(), usedCount);
        }
    }

    private double scheduled(double dt) {
        if (dt == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        if (!Double.isFinite(dt)) {
            throw new IllegalStateException("Collision prediction is NaN or -Infinity");
        }
        if (dt < -1e-10) {
            throw new IllegalStateException("Collision prediction in the past");
        }
        double eventTime = time + Math.max(0, dt);
        if (eventTime == Double.POSITIVE_INFINITY) {
            return Double.POSITIVE_INFINITY;
        }
        if (!Double.isFinite(eventTime)) {
            throw new IllegalStateException("Collision prediction is NaN or -Infinity");
        }
        return eventTime;
    }

    private void loadEvents(Particle particle) {
        double timeY = scheduled(particle.collidesY(L));
        double timeX = scheduled(particle.collidesX(L));
        if (Double.isFinite(timeY)) {
            events.add(new EventHorizontal(timeY, particle));
        }
        if (Double.isFinite(timeX)) {
            events.add(new EventVertical(timeX, particle, L, GATE));
        }
        for (Particle other : particles) {
            if (particle == other) {
                continue;
            }
            double timeP = scheduled(particle.collides(other));
            if (Double.isFinite(timeP)) {
                events.add(new EventParticles(timeP, particle, other));
            }
        }
    }
}
