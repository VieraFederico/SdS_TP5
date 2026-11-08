package models;

/**
 * Full state of one pedestrian. Plain public fields: schemes, events and the integrator all read
 * and write them on the hot path, and getters would only add noise to the formulas.
 * Position and velocity of an event-driven agent are valid at `time`: see advanceTo.
 */
public final class Agent {
    public enum Scheme { EVENT_DRIVEN, TIME_STEP }

    public enum Location { INSIDE, PASSED_DOOR, RETIRED }

    public final int id;
    public final double radius, mass;
    public double x, y, vx, vy;
    public double time;
    /** Forces of the current evaluation and accelerations a(t), a(t-dt) of the integrator. */
    public double fx, fy, ax, ay, previousAx, previousAy;
    public Scheme scheme = Scheme.TIME_STEP;
    public Location location = Location.INSIDE;
    public boolean headingOut;
    public double doorTime = Double.NaN, retireTime = Double.NaN;
    /** Grows with every velocity change, so events predicted before it are stale. */
    public int revision;
    /** Next phantom event (infinite in free flight) and last instant the velocity was relaxed. */
    public double phantomTime = Double.POSITIVE_INFINITY, relaxedAt;

    public Agent(int id, double radius, double mass, double x, double y) {
        if (!Double.isFinite(x) || !Double.isFinite(y) || radius <= 0 || mass <= 0)
            throw new IllegalArgumentException("Invalid agent state");
        this.id = id;
        this.radius = radius;
        this.mass = mass;
        this.x = x;
        this.y = y;
    }

    /** Free flight to `target`: the only way an event-driven agent moves [Teorica_3, diapo 8]. */
    public void advanceTo(double target) {
        double elapsed = target - time;
        if (elapsed < 0) throw new IllegalArgumentException("Time moved backwards");
        x += vx * elapsed;
        y += vy * elapsed;
        time = target;
    }

    public boolean active() {
        return location != Location.RETIRED;
    }

    /** Separation between edges: negative means overlap [Teorica_5, diapo 8]. */
    public double gap(Agent other) {
        return Math.hypot(other.x - x, other.y - y) - radius - other.radius;
    }
}
