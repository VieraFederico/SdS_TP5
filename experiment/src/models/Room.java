package models;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Square room [0, L]² with a door of width d centred on the wall y = 0; outside (y < 0) is open.
 * Walls are segments, so the closest point of a segment gives the wall contact and, at its end,
 * the door edge as a point obstacle [Teorica_5, diapos 9 y 12].
 *
 * Targets (decision 6 of DECISIONS.md): first (L/2, R_i), so a straight path never touches the wall
 * y = 0, then (L/2, -2 m) [Teorica_6, diapo 57]. Agents retire at y = -1 m [enunciado].
 */
public final class Room {
    public static final double EXIT_DISTANCE = 1, OUTER_TARGET_DEPTH = 2;
    private static final int PLACEMENT_ATTEMPTS = 100_000;

    public interface WallContact {
        void at(double x, double y);
    }

    private record Segment(double x1, double y1, double x2, double y2) {
    }

    public final double side, door;
    private final List<Segment> walls;

    public Room(double side, double door) {
        if (side <= 0 || door <= 0 || door >= side) throw new IllegalArgumentException("Invalid room geometry");
        this.side = side;
        this.door = door;
        double left = doorLeft(), right = doorRight();
        walls = List.of(new Segment(0, 0, 0, side), new Segment(side, 0, side, side),
            new Segment(0, side, side, side), new Segment(0, 0, left, 0), new Segment(right, 0, side, 0));
    }

    public double doorCentre() {
        return side / 2;
    }

    public double doorLeft() {
        return (side - door) / 2;
    }

    public double doorRight() {
        return (side + door) / 2;
    }

    public double targetX() {
        return doorCentre();
    }

    public double targetY(Agent agent) {
        return agent.headingOut ? -OUTER_TARGET_DEPTH : agent.radius;
    }

    /** Agents against the wall beside the door never qualify: their x is outside the opening and y > 0. */
    public boolean reachedDoor(Agent agent) {
        if (agent.headingOut) return false;
        return agent.y <= 0 || (Math.abs(agent.x - doorCentre()) < door / 2 && agent.y <= agent.radius);
    }

    /** Calls `contact` with the closest point of each wall the agent overlaps. */
    public void forEachWallContact(Agent agent, WallContact contact) {
        double r = agent.radius, x = agent.x, y = agent.y;
        if (x > r && x < side - r && y > r && y < side - r) return;
        for (Segment wall : walls) {
            double dx = wall.x2 - wall.x1, dy = wall.y2 - wall.y1;
            double along = ((x - wall.x1) * dx + (y - wall.y1) * dy) / (dx * dx + dy * dy);
            along = Math.max(0, Math.min(1, along));
            double px = wall.x1 + along * dx, py = wall.y1 + along * dy;
            if (Math.hypot(px - x, py - y) < r) contact.at(px, py);
        }
    }

    /** One agent at a time, at random, rejecting overlaps [Teorica_3, diapo 10; enunciado]. */
    public List<Agent> placeAgents(int n, double minRadius, double maxRadius, double mass, Random random) {
        List<Agent> agents = new ArrayList<>(n);
        for (int id = 0; id < n; id++) {
            double radius = minRadius + (maxRadius - minRadius) * random.nextDouble();
            agents.add(place(id, radius, mass, agents, random));
        }
        return agents;
    }

    private Agent place(int id, double radius, double mass, List<Agent> placed, Random random) {
        for (int attempt = 0; attempt < PLACEMENT_ATTEMPTS; attempt++) {
            double x = radius + (side - 2 * radius) * random.nextDouble();
            double y = radius + (side - 2 * radius) * random.nextDouble();
            Agent candidate = new Agent(id, radius, mass, x, y);
            if (placed.stream().allMatch(other -> candidate.gap(other) > 0)) return candidate;
        }
        throw new IllegalArgumentException("Unable to place agent " + id);
    }
}
