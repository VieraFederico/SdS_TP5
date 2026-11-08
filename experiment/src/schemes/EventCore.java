package schemes;

import models.Agent;
import models.CellGrid;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Event machinery shared by EDMD and the hybrid, from the TP3 Board. Differences with the TP3:
 * agents move only when an event touches them, predictions go against the neighbors in the grid, and
 * collisions lose energy and are followed by phantom events until vd is recovered.
 *
 * In the hybrid (enableSwitching) it only moves the event-driven agents: it never predicts against a
 * time-step agent, it marks the agents that meet E1, E2 or E3 [DESIGN.md 2.6], and HybridScheme
 * switches them at the end of the step.
 */
public final class EventCore {
    interface Progress {
        void update(Agent agent, double time);
    }

    private static final double PAST_TOLERANCE = 1e-6, CROSSING_MARGIN = 1e-9, SKIN_TOLERANCE = 1e-9;
    private final Room room;
    private final CellGrid grid;
    private final PedestrianModel model;
    private final List<Agent> agents;
    private final Progress progress;
    private final Scheme.FrameSink sink;
    private final double pairRestitution, wallRestitution, separationSpeed, dt;
    private final int dumpEvery;
    private final PriorityQueue<Event> queue = new PriorityQueue<>();
    private final Agent[] neighbors;
    private double now;
    private long processed, frame;
    /** Hybrid only: null in pure EDMD. */
    private SwitchingCriterion criterion;
    private double maxSpeed, timeStepClock;
    private final List<Agent> marked = new ArrayList<>();

    /** `sink` null: the scheme writes the frames itself (the hybrid, at the end of a step). */
    EventCore(Room room, CellGrid grid, PedestrianModel model, List<Agent> agents, Progress progress,
              Scheme.FrameSink sink, double pairRestitution, double wallRestitution, double separationSpeed,
              double dt, int dumpEvery) {
        this.room = room;
        this.grid = grid;
        this.model = model;
        this.agents = agents;
        this.progress = progress;
        this.sink = sink;
        this.pairRestitution = pairRestitution;
        this.wallRestitution = wallRestitution;
        this.separationSpeed = separationSpeed;
        this.dt = dt;
        this.dumpEvery = dumpEvery;
        neighbors = new Agent[agents.size()];
    }

    /** `maxSpeed`: bound of the time-step speeds, which the safe horizon relies on. */
    void enableSwitching(SwitchingCriterion criterion, double maxSpeed) {
        this.criterion = criterion;
        this.maxSpeed = maxSpeed;
    }

    double now() {
        return now;
    }

    long processed() {
        return processed;
    }

    /** Frame k at k·dump·dt, the same expression as the step count of the other schemes. */
    private double frameTime(long k) {
        return (double) (k * dumpEvery) * dt;
    }

    void start() {
        if (sink != null) queue.add(new Event.Write(frameTime(++frame)));
        for (Agent agent : agents) predict(agent);
    }

    /** Applies the next valid event; false if there is none up to `limit`. */
    boolean processNext(double limit) {
        while (!queue.isEmpty() && queue.peek().stale()) queue.poll();
        if (queue.isEmpty() || queue.peek().time > limit) return false;
        Event event = queue.poll();
        now = Math.max(now, event.time);
        event.apply(this);
        processed++;
        return true;
    }

    /** Hybrid: every event up to the end of the step, time-step agents still at the start of it. */
    void processUntil(double limit, long maxEvents) {
        while (processed < maxEvents && processNext(limit)) {
        }
    }

    /** Hybrid: the step ended; every agent, of either scheme, is now at `time`. */
    void settle(double time) {
        now = time;
        timeStepClock = time;
    }

    /** Hybrid: the agents marked since the last call. */
    List<Agent> drainMarked() {
        List<Agent> drained = new ArrayList<>(marked);
        marked.clear();
        return drained;
    }

    /** Every prediction of the agent, from its state now [Teorica_3, diapos 12-14 y 28]. */
    void predict(Agent agent) {
        if (!agent.active() || agent.scheme == Agent.Scheme.TIME_STEP) return;
        predictWalls(agent);
        predictProgress(agent);
        double crossing = grid.exitTime(agent);
        if (Double.isFinite(crossing)) queue.add(new Event.CellCross(now + crossing + CROSSING_MARGIN, agent));
        if (Double.isFinite(agent.phantomTime)) {
            double phantom = Math.max(now, agent.phantomTime);
            queue.add(model.nearlyRecovered(agent, room, phantom - now)
                ? new Event.BounceBack(phantom, agent) : new Event.Phantom(phantom, agent));
        }
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) {
            Agent other = neighbors[k];
            if (other.scheme == Agent.Scheme.TIME_STEP) continue;
            other.advanceTo(now);
            double dt = collisionTime(agent, other);
            if (Double.isFinite(dt)) queue.add(new Event.Collision(now + dt, agent, other));
        }
        if (criterion != null && !agent.switchPending) {
            double entry = criterion.zoneEntryTime(agent, room);
            if (Double.isFinite(entry)) queue.add(new Event.Zone(now + entry, agent));
            watch(agent);
        }
    }

    /** Standard EDMD quadratic with σ = Ri + Rj [Teorica_3, diapo 14]; infinite if they never touch. */
    public static double collisionTime(Agent i, Agent j) {
        return collisionTime(j.x - i.x, j.y - i.y, j.vx - i.vx, j.vy - i.vy, i.radius + j.radius);
    }

    static double collisionTime(double dx, double dy, double dvx, double dvy, double sigma) {
        double dvdr = dvx * dx + dvy * dy;
        if (dvdr >= 0) return Double.POSITIVE_INFINITY;
        double dvdv = dvx * dvx + dvy * dvy;
        double d = dvdr * dvdr - dvdv * (dx * dx + dy * dy - sigma * sigma);
        if (d < 0) return Double.POSITIVE_INFINITY;
        double time = -(dvdr + Math.sqrt(d)) / dvdv;
        if (time < -PAST_TOLERANCE) throw new IllegalStateException("Overlapping agents approaching");
        return Math.max(0, time);
    }

    private void predictWalls(Agent agent) {
        double r = agent.radius;
        if (agent.y >= 0) {
            if (agent.vx < 0) addWall(agent, (r - agent.x) / agent.vx, -1, 0);
            if (agent.vx > 0) addWall(agent, (room.side - r - agent.x) / agent.vx, 1, 0);
            if (agent.vy > 0) addWall(agent, (room.side - r - agent.y) / agent.vy, 0, 1);
        }
        if (agent.y > 0 && agent.vy < 0) addDoorWall(agent, (r - agent.y) / agent.vy, 0, -1);
        if (agent.y < 0 && agent.vy > 0) addDoorWall(agent, (-r - agent.y) / agent.vy, 0, 1);
        addEdge(agent, room.doorLeft());
        addEdge(agent, room.doorRight());
    }

    private void addWall(Agent agent, double time, double nx, double ny) {
        queue.add(new Event.Wall(now + Math.max(0, time), agent, nx, ny));
    }

    /** The wall y = 0 only exists outside the opening; there, the door edges stop the agent. */
    private void addDoorWall(Agent agent, double time, double nx, double ny) {
        double x = agent.x + agent.vx * Math.max(0, time);
        if (Math.abs(x - room.doorCentre()) >= room.door / 2) addWall(agent, time, nx, ny);
    }

    private void addEdge(Agent agent, double x) {
        double time = collisionTime(x - agent.x, -agent.y, -agent.vx, -agent.vy, agent.radius);
        if (Double.isFinite(time)) queue.add(new Event.Edge(now + time, agent, x, 0));
    }

    private void predictProgress(Agent agent) {
        if (room.reachedDoor(agent)) {
            queue.add(new Event.Progress(now, agent, agent.y));
            return;
        }
        double line;
        if (!agent.headingOut) line = agent.radius;
        else if (agent.location == Agent.Location.INSIDE) line = 0;
        else line = -Room.EXIT_DISTANCE;
        if (agent.vy >= 0 || agent.y <= line) {
            if (agent.headingOut && agent.y <= line) queue.add(new Event.Progress(now, agent, agent.y));
            return;
        }
        double time = (line - agent.y) / agent.vy;
        double x = agent.x + agent.vx * time;
        if (agent.headingOut || Math.abs(x - room.doorCentre()) < room.door / 2)
            queue.add(new Event.Progress(now + time, agent, line));
    }

    /** Impulse with restitution [Teorica_3, diapo 20], with a minimum separation speed (decision 11). */
    void collide(Agent i, Agent j) {
        i.advanceTo(now);
        j.advanceTo(now);
        applyImpulse(i, j, pairRestitution, separationSpeed);
        perturbed(i);
        perturbed(j);
        collided(i);
        collided(j);
        touched(i);
        touched(j);
        predict(i);
        predict(j);
    }

    public static void applyImpulse(Agent i, Agent j, double restitution, double separationSpeed) {
        double dx = j.x - i.x, dy = j.y - i.y, distance = Math.hypot(dx, dy);
        double nx = dx / distance, ny = dy / distance;
        double approach = (i.vx - j.vx) * nx + (i.vy - j.vy) * ny;
        if (approach <= 0) return;
        double change = approach + Math.max(restitution * approach, separationSpeed);
        double impulse = change * i.mass * j.mass / (i.mass + j.mass);
        i.vx -= impulse * nx / i.mass;
        i.vy -= impulse * ny / i.mass;
        j.vx += impulse * nx / j.mass;
        j.vy += impulse * ny / j.mass;
    }

    /** (nx, ny): unit normal from the agent to the wall. */
    void bounce(Agent agent, double nx, double ny) {
        agent.advanceTo(now);
        double approach = agent.vx * nx + agent.vy * ny;
        if (approach > 0) {
            double change = approach + Math.max(wallRestitution * approach, separationSpeed);
            agent.vx -= change * nx;
            agent.vy -= change * ny;
        }
        perturbed(agent);
        collided(agent);
        touched(agent);
        predict(agent);
    }

    void bounceOnPoint(Agent agent, double x, double y) {
        agent.advanceTo(now);
        double dx = x - agent.x, dy = y - agent.y, distance = Math.hypot(dx, dy);
        bounce(agent, dx / distance, dy / distance);
    }

    /** Phantom event: exact solution of the driving force over the elapsed time [Teorica_6, diapo 31]. */
    void relax(Agent agent) {
        agent.advanceTo(now);
        model.relax(agent, room, now - agent.relaxedAt);
        agent.relaxedAt = now;
        agent.phantomTime = now + model.characteristicTime(agent);
        touched(agent);
        predict(agent);
    }

    void recover(Agent agent) {
        agent.advanceTo(now);
        model.setFreeFlight(agent, room);
        agent.phantomTime = Double.POSITIVE_INFINITY;
        touched(agent);
        predict(agent);
    }

    /** The line is exact: floating-point drift cannot leave the agent on the wrong side of it. */
    void progress(Agent agent, double line) {
        agent.advanceTo(now);
        agent.y = line;
        boolean wasHeadingOut = agent.headingOut;
        progress.update(agent, now);
        if (agent.headingOut != wasHeadingOut) perturbed(agent);
        touched(agent);
        predict(agent);
    }

    void crossCell(Agent agent) {
        agent.advanceTo(now);
        grid.update(agent);
        touched(agent);
        predict(agent);
    }

    void write(double time) {
        for (Agent agent : agents) if (agent.active()) agent.advanceTo(time);
        try {
            sink.write(time);
        } catch (IOException error) {
            throw new UncheckedIOException(error);
        }
        queue.add(new Event.Write(frameTime(++frame)));
    }

    /** Hybrid: the agent becomes time-step at the end of the step; until then it stays event-driven. */
    void mark(Agent agent) {
        if (agent.switchPending || !agent.active()) return;
        agent.switchPending = true;
        agent.skinStamp++;
        marked.add(agent);
    }

    /**
     * Hybrid, criterion E2 and safe horizon [DESIGN.md 2.7]. Time-step agents are where the last step
     * left them, at most maxSpeed·(now - timeStepClock) away from where they really are; beyond the 3×3
     * cells there is none closer than one cell minus two radii, and HybridScheme calls this again when
     * one enters them. So, with g the smallest gap that can be guaranteed now, the skin cannot be
     * reached before (g - δ_in)/(|v| + maxSpeed): that is when the next check goes.
     */
    void watch(Agent agent) {
        agent.skinStamp++;
        if (criterion == null || agent.switchPending || !agent.active()
            || agent.scheme == Agent.Scheme.TIME_STEP) return;
        double x = agent.xAt(now), y = agent.yAt(now), gap = Double.POSITIVE_INFINITY;
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) {
            Agent other = neighbors[k];
            if (other.scheme == Agent.Scheme.TIME_STEP)
                gap = Math.min(gap, Math.hypot(other.x - x, other.y - y) - agent.radius - other.radius);
        }
        if (gap == Double.POSITIVE_INFINITY) return;
        gap -= maxSpeed * (now - timeStepClock);
        double margin = gap - criterion.skinIn();
        if (margin <= SKIN_TOLERANCE) {
            mark(agent);
            return;
        }
        queue.add(new Event.Skin(now + margin / (Math.hypot(agent.vx, agent.vy) + maxSpeed), agent));
    }

    /** Criterion E3 [DESIGN.md 2.6]: walls and door edges count as collisions too. */
    private void collided(Agent agent) {
        if (criterion != null && criterion.flightTooShort(now - agent.lastCollision)) mark(agent);
        agent.lastCollision = now;
    }

    /** Starts the phantom events; a pending one keeps its time, as a collision does not reset it. */
    private void perturbed(Agent agent) {
        if (Double.isFinite(agent.phantomTime)) return;
        agent.relaxedAt = now;
        agent.phantomTime = now + model.characteristicTime(agent);
    }

    private void touched(Agent agent) {
        agent.revision++;
    }
}
