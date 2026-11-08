package models;

/**
 * SFM without social force [Teorica_6, diapo 28]: contact N.2 + T.3 [Teorica_5, diapo 15;
 * Teorica_6, diapo 29] and driving force m(vd ê - v)/τ [Teorica_6, diapo 31].
 * m, τ, kn and kt from Helbing et al. (2000), as the enunciado asks; radii and vd from the enunciado.
 */
public final class SocialForceModel implements PedestrianModel {
    public static final double MASS = 80, TAU = .5, KN = 1.2e5, KT = 2.4e5;
    /** T.3 coefficient: KT, or 0 without tangential friction, as the cátedra allows (TP5_Consideraciones iii). */
    private final double kt;

    public SocialForceModel(double kt) {
        if (kt < 0) throw new IllegalArgumentException("kt must be nonnegative");
        this.kt = kt;
    }
    public static final double DESIRED_SPEED = 1.5, MIN_RADIUS = .18, MAX_RADIUS = .25, RECOVERED = .01;

    @Override
    public void addDrivingForce(Agent agent, Room room) {
        double dx = room.targetX() - agent.x, dy = room.targetY(agent) - agent.y;
        double distance = Math.hypot(dx, dy);
        if (distance == 0) return;
        double scale = DESIRED_SPEED / distance;
        agent.fx += agent.mass * (scale * dx - agent.vx) / TAU;
        agent.fy += agent.mass * (scale * dy - agent.vy) / TAU;
    }

    @Override
    public void addContactForce(Agent i, Agent j) {
        double dx = j.x - i.x, dy = j.y - i.y, contact = i.radius + j.radius;
        double squared = dx * dx + dy * dy;
        if (squared >= contact * contact) return;
        double distance = requireNormal(Math.sqrt(squared));
        double nx = dx / distance, ny = dy / distance;
        double fx = force(contact - distance, nx, ny, i.vx - j.vx, i.vy - j.vy, true);
        double fy = force(contact - distance, nx, ny, i.vx - j.vx, i.vy - j.vy, false);
        i.fx += fx;
        i.fy += fy;
        j.fx -= fx;
        j.fy -= fy;
    }

    @Override
    public void addWallForce(Agent agent, double x, double y) {
        double dx = x - agent.x, dy = y - agent.y;
        double distance = requireNormal(Math.hypot(dx, dy));
        double nx = dx / distance, ny = dy / distance;
        agent.fx += force(agent.radius - distance, nx, ny, agent.vx, agent.vy, true);
        agent.fy += force(agent.radius - distance, nx, ny, agent.vx, agent.vy, false);
    }

    /** Projection of F_N ê^n + F_T ê^t with ê^t = (-ny, nx) [Teorica_5, diapos 11, 15 y 17]. */
    private double force(double overlap, double nx, double ny, double vx, double vy, boolean xComponent) {
        double normal = -KN * overlap;
        double tangential = -kt * overlap * (-ny * vx + nx * vy);
        return xComponent ? normal * nx - tangential * ny : normal * ny + tangential * nx;
    }

    @Override
    public void setFreeFlight(Agent agent, Room room) {
        double dx = room.targetX() - agent.x, dy = room.targetY(agent) - agent.y;
        double scale = DESIRED_SPEED / Math.hypot(dx, dy);
        agent.vx = scale * dx;
        agent.vy = scale * dy;
    }

    /** v(t) = vd ê + (v0 - vd ê) exp(-t/τ), solution of m dv/dt = m(vd ê - v)/τ [Teorica_6, diapo 31]. */
    @Override
    public void relax(Agent agent, Room room, double elapsed) {
        double vx = agent.vx, vy = agent.vy, decay = Math.exp(-elapsed / TAU);
        setFreeFlight(agent, room);
        agent.vx += (vx - agent.vx) * decay;
        agent.vy += (vy - agent.vy) * decay;
    }

    @Override
    public double velocityDeviation(Agent agent, Room room) {
        double dx = room.targetX() - agent.x, dy = room.targetY(agent) - agent.y;
        double scale = DESIRED_SPEED / Math.hypot(dx, dy);
        return Math.hypot(agent.vx - scale * dx, agent.vy - scale * dy);
    }

    @Override
    public double desiredSpeed() {
        return DESIRED_SPEED;
    }

    @Override
    public boolean nearlyRecovered(Agent agent, Room room, double elapsed) {
        return velocityDeviation(agent, room) * Math.exp(-elapsed / TAU) < RECOVERED * DESIRED_SPEED;
    }

    /** Note of the professor: how long the agent takes to travel its radius. The max keeps it finite. */
    @Override
    public double characteristicTime(Agent agent) {
        return agent.radius / Math.max(Math.hypot(agent.vx, agent.vy), DESIRED_SPEED);
    }

    /**
     * Soft contact as a hard-disc restitution, decision ★9 of DECISIONS.md: with
     * ξ̈ + ξ̇/τ + ω²ξ = 0, a rebound takes π/ω and e = exp(-π/(2τω)).
     */
    public static double restitution(double reducedMass) {
        return Math.exp(-Math.PI / (2 * TAU * Math.sqrt(KN / reducedMass)));
    }

    /** π/ω: how long a soft rebound lasts. The threshold t_s of criterion E3 (decision ★21b). */
    public static double contactDuration(double reducedMass) {
        return Math.PI / Math.sqrt(KN / reducedMass);
    }

    private static double requireNormal(double distance) {
        if (distance == 0) throw new IllegalStateException("Coincident contact centers: undefined normal");
        return distance;
    }
}
