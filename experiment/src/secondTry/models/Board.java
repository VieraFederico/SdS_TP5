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
    public static final double TIME_STEP = 1e-3;
    private static final double K_N = 1.2e5;
    private static final double PHANTOM_TIME = MAX_RADIUS / DESIRED_SPEED;

    private final Queue<Event> events = new PriorityQueue<>();
    private final List<Particle> particles = new ArrayList<>();
    private final List<Particle> particlesTSDM = new ArrayList<>();
    private final List<Particle> particlesEDM = new ArrayList<>();
    private final Map<Integer, Map<Integer, Set<Particle>>> grid;
    private final Scheme scheme;
    private int usedCount = 0;
    private long eventCount;
    private double time;

    @FunctionalInterface
    public interface SnapshotSink {
        void write(long index, double time, List<Particle> particles, int usedCount) throws IOException;
    }

    public Board(int n, Scheme s) throws IOException {
        this(n, 1, s);
    }

    public Board(int n, long seed, Scheme scheme) {
        if (n <= 0) {
            throw new IllegalArgumentException("N must be positive");
        }
        this.scheme = scheme;
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
        if (scheme == Scheme.TSDM) {
            particlesTSDM.addAll(particles);
            this.grid = grid;
        }
        else {
            particlesEDM.addAll(particles);
            initializeEvents();
            this.grid = new HashMap<>();
        }
    }

    /** Explicit initial states for deterministic diagnostic scenarios. */
    public Board(List<Particle> initial, Scheme scheme) {
        if (initial.isEmpty()) {
            throw new IllegalArgumentException("Empty particle list");
        }
        for (int i = 0; i < initial.size(); i++) {
            Particle particle = initial.get(i);
            if (outside(particle)) {
                throw new IllegalArgumentException("Particle outside table");
            }
            for (int j = 0; j < i; j++) {
                if (particle.overlaps(initial.get(j))) {
                    throw new IllegalArgumentException("Particles overlap");
                }
            }
        }
        this.scheme = scheme;
        particles.addAll(initial);
        this.grid = new HashMap<>();
        if (scheme == Scheme.TSDM) {
            particlesTSDM.addAll(initial);
            for (Particle particle : initial) {
                int indexX = (int) (particle.getPosition().x() / GRID_CELL);
                int indexY = (int) (particle.getPosition().y() / GRID_CELL);
                Map<Integer, Set<Particle>> column = grid.getOrDefault(indexX, new HashMap<>());
                Set<Particle> set = column.getOrDefault(indexY, new HashSet<>());
                set.add(particle);
                column.put(indexX, set);
                grid.put(indexX, column);
            }
        } else
            particlesEDM.addAll(initial);
        initializeEvents();
    }

    private static boolean outside(Particle one) {
        double x = one.getPosition().x(), y = one.getPosition().y();
        return x - one.getR() < 0 || x + one.getR() > L
                || y - one.getR() < 0 || y + one.getR() > L;
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
        Vector2D position = new Vector2D(intendedX, intendedY);
        Vector2D velocity = position.pointer(new Vector2D(L/2, 0)).scale(DESIRED_SPEED);
        Particle particle = new Particle(position, radius, velocity, MASS);
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
        for (Particle particle : particlesEDM) {
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
        double lastTimeStep = 0;
        while (!events.isEmpty()) {
            Event event = events.poll();
            if (event.wasInvalidated()) {
                continue;
            }
            double next = event.getTime();
            if (next > lastTimeStep) {
                if(scheme == Scheme.EDM || particlesTSDM.isEmpty()) {
                    lastTimeStep = next;
                }else{
                    while(lastTimeStep < next) {
                        lastTimeStep+=TIME_STEP;
                        advanceBeeman(particlesTSDM);
                    }
                }
            }
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
            event.bounce();
            for (Particle particle : event.getParticles()) {
                loadEvents(particle);
                events.add(new EventPhantom(time+PHANTOM_TIME,particle,L,GATE,DESIRED_SPEED,PHANTOM_TIME/RELAX_TIME));
            }
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
            events.add(new EventVertical(timeX, particle));
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


    public void advanceBeeman(List<Particle> particles) {
        Map<Particle, Vector2D> previousVelocities = new IdentityHashMap<>(particles.size());

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle particle = it.next();
            previousVelocities.put(particle, particle.getVelocity());
            Vector2D previousPosition = particle.getPosition();
            int previousIndexX = (int) Math.floor(previousPosition.x() / GRID_CELL);
            int previousIndexY = (int) Math.floor(previousPosition.y() / GRID_CELL);
            particle.predictState(TIME_STEP);
            Vector2D position = particle.getPosition();
            if(position.y()<-1) {
                particles.remove(particle);
                this.particles.remove(particle);
                grid.get(previousIndexX).get(previousIndexY).remove(particle);
                continue;
            }
            int indexX = (int) Math.floor(position.x() / GRID_CELL);
            int indexY = (int) Math.floor(position.y() / GRID_CELL);
            if(previousIndexX != indexX || previousIndexY != indexY) {
                updateGrid(particle,previousIndexX,previousIndexY,indexX,indexY);
            }
            particle.clearForce();
        }

        Set<Particle> checked = new HashSet<>();
        for(Particle particle : particles) {
            drivingForce(particle);
            wallForce(particle);
            checked.add(particle);
            particleForce(particle,checked);
        }

        for (Particle particle : particles) {
            Vector2D originalVelocity = previousVelocities.get(particle);
            particle.correctVelocity(originalVelocity, TIME_STEP);
        }
    }

    public void updateGrid(Particle particle, int previousIndexX, int previousIndexY, int indexX, int indexY) {
        grid.get(previousIndexX).get(previousIndexY).remove(particle);
        grid.computeIfAbsent(indexX, k -> new HashMap<>())
                .computeIfAbsent(indexY, k -> new HashSet<>())
                .add(particle);
    }

    public void drivingForce(Particle particle) {
        Vector2D position = particle.getPosition();
        Vector2D desiredVelocity;
        double r = particle.getR();
        if(position.y()<r && position.x() < L/2+GATE/2 -r && position.x() > L/2-GATE/2+r)
            desiredVelocity = new Vector2D(0,-DESIRED_SPEED);
        else
            desiredVelocity = position.pointer(new Vector2D(L/2, 0)).scale(DESIRED_SPEED);
        particle.addForce(desiredVelocity.subtract(particle.getVelocity()).scale(1/RELAX_TIME));
    }

    public void wallForce(Particle particle) {
        Vector2D position = particle.getPosition();
        double r = particle.getR();
        if(position.x() < r)
            particle.addForce(new Vector2D((r-position.x())*K_N/MASS,0));
        else if (position.x() + r > L)
            particle.addForce(new Vector2D((L-r-position.x())*K_N/MASS,0));
        if(position.y() < r) {
            double sqrt = Math.sqrt(r * r - position.y() * position.y());
            if (position.x() > L/2 + GATE / 2 - sqrt || position.x() < L/2 - GATE / 2 + sqrt)
                particle.addForce(new Vector2D(0, (position.y()>0?1:-1)*(r - position.y()) * K_N / MASS));
        }else if (position.y() + r > L)
            particle.addForce(new Vector2D(0,(L-r-position.y())*K_N/MASS));
    }

    public void particleForce(Particle particle, Set<Particle> checked) {
        Vector2D position = particle.getPosition();
        int indexX = (int) Math.floor(position.x() / GRID_CELL);
        int indexY = (int) Math.floor(position.y() / GRID_CELL);
        for (int dx = -1; dx <= 1; dx++) {
            Map<Integer, Set<Particle>> column = grid.getOrDefault(indexX + dx, Collections.emptyMap());
            for (int dy = -1; dy <= 1; dy++) {
                Set<Particle> cell = column.getOrDefault(indexY + dy, Collections.emptySet());
                for (Particle other : cell) {
                    if (other != particle && !checked.contains(other)) {
                        Vector2D delta = other.getPosition().subtract(position);
                        double distSq = delta.dotProduct(delta);
                        double contact = particle.getR() + other.getR();
                        if (distSq > 0 && distSq < contact * contact) {
                            double distance = Math.sqrt(distSq);
                            Vector2D normalForce = delta.scale((distance - contact) * K_N / distance / MASS);
                            particle.addForce(normalForce);
                            other.addForce(normalForce.scale(-1.0));
                        }
                    }
                }
            }
        }
    }
}
