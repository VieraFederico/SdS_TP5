package schemes;

import models.Agent;
import models.Room;
import java.util.function.DoubleSupplier;

/**
 * Zone of radius R_s around the door centre, skin δ_in around time-step agents and minimum flight
 * time t_s, with hysteresis (h, δ_out > δ_in, ε) on the way back [DESIGN.md 2.6; Teorica_3, diapo 5].
 * R_s = 0 and t_s = 0 never switch anyone: pure EDMD. R_s past the farthest corner: pure TSDMD.
 */
public final class ZoneSkinFlightCriterion implements SwitchingCriterion {
    private final double zoneRadius, flightThreshold, skinIn, skinOut, hysteresis, tolerance, desiredSpeed;

    public ZoneSkinFlightCriterion(double zoneRadius, double flightThreshold, double skinIn, double skinOut,
                                   double hysteresis, double tolerance, double desiredSpeed) {
        if (zoneRadius < 0 || flightThreshold < 0 || skinIn <= 0 || skinOut <= skinIn || hysteresis < 0
            || tolerance <= 0)
            throw new IllegalArgumentException("Need R_s, t_s, h >= 0, 0 < delta_in < delta_out and eps > 0");
        this.zoneRadius = zoneRadius;
        this.flightThreshold = flightThreshold;
        this.skinIn = skinIn;
        this.skinOut = skinOut;
        this.hysteresis = hysteresis;
        this.tolerance = tolerance;
        this.desiredSpeed = desiredSpeed;
    }

    /** Same quadratic as a collision, against the door centre with σ = R_s [Teorica_3, diapo 14]. */
    @Override
    public double zoneEntryTime(Agent agent, Room room) {
        if (zoneRadius == 0) return Double.POSITIVE_INFINITY;
        double dx = room.doorCentre() - agent.x, dy = -agent.y;
        if (Math.hypot(dx, dy) <= zoneRadius) return 0;
        return EventCore.collisionTime(dx, dy, -agent.vx, -agent.vy, zoneRadius);
    }

    @Override
    public double skinIn() {
        return skinIn;
    }

    @Override
    public boolean flightTooShort(double flight) {
        return flight < flightThreshold;
    }

    @Override
    public double skinOut() {
        return skinOut;
    }

    @Override
    public boolean toEventDriven(Agent agent, Room room, DoubleSupplier deviation, DoubleSupplier gap) {
        return Math.hypot(room.doorCentre() - agent.x, agent.y) >= zoneRadius + hysteresis
            && deviation.getAsDouble() <= tolerance * desiredSpeed && gap.getAsDouble() >= skinOut;
    }
}
