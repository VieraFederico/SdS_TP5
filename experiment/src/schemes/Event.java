package schemes;

import models.Agent;

/**
 * Something that happens at an instant, from the TP3 Event: ordered by time, ties by creation order,
 * and stale when an agent changed since it was predicted [Teorica_3, diapos 6-9]. Every event that
 * touches an agent grows its revision, so all its other predictions die at once.
 */
abstract class Event implements Comparable<Event> {
    private static long created;
    final double time;
    private final long order = created++;
    final Agent first, second;
    private final int firstRevision, secondRevision;

    Event(double time, Agent first, Agent second) {
        this.time = time;
        this.first = first;
        this.second = second;
        firstRevision = first == null ? 0 : first.revision;
        secondRevision = second == null ? 0 : second.revision;
    }

    abstract void apply(EventCore core);

    boolean stale() {
        return (first != null && first.revision != firstRevision)
            || (second != null && second.revision != secondRevision);
    }

    @Override
    public int compareTo(Event other) {
        int byTime = Double.compare(time, other.time);
        return byTime != 0 ? byTime : Long.compare(order, other.order);
    }

    /** Agent-agent collision. */
    static final class Collision extends Event {
        Collision(double time, Agent i, Agent j) {
            super(time, i, j);
        }

        @Override
        void apply(EventCore core) {
            core.collide(first, second);
        }
    }

    /** Agent against a wall with outward normal (nx, ny) from the agent. */
    static final class Wall extends Event {
        private final double nx, ny;

        Wall(double time, Agent agent, double nx, double ny) {
            super(time, agent, null);
            this.nx = nx;
            this.ny = ny;
        }

        @Override
        void apply(EventCore core) {
            core.bounce(first, nx, ny);
        }
    }

    /** Agent against a door edge: a fixed disc of radius 0. */
    static final class Edge extends Event {
        private final double x, y;

        Edge(double time, Agent agent, double x, double y) {
            super(time, agent, null);
            this.x = x;
            this.y = y;
        }

        @Override
        void apply(EventCore core) {
            core.bounceOnPoint(first, x, y);
        }
    }

    /** No collision: wakes the agent up to relax its velocity towards vd ê (the "phantom" event). */
    static final class Phantom extends Event {
        Phantom(double time, Agent agent) {
            super(time, agent, null);
        }

        @Override
        void apply(EventCore core) {
            core.relax(first);
        }
    }

    /** The velocity is back at vd ê: the agent returns to free flight. */
    static final class BounceBack extends Event {
        BounceBack(double time, Agent agent) {
            super(time, agent, null);
        }

        @Override
        void apply(EventCore core) {
            core.recover(first);
        }
    }

    /** Change of target, door crossing or retirement: the agent reaches a line y = constant. */
    static final class Progress extends Event {
        private final double y;

        Progress(double time, Agent agent, double y) {
            super(time, agent, null);
            this.y = y;
        }

        @Override
        void apply(EventCore core) {
            core.progress(first, y);
        }
    }

    /** The agent enters another cell of the grid. */
    static final class CellCross extends Event {
        CellCross(double time, Agent agent) {
            super(time, agent, null);
        }

        @Override
        void apply(EventCore core) {
            core.crossCell(first);
        }
    }

    /** Hybrid, criterion E1: the agent enters the zone around the door. */
    static final class Zone extends Event {
        Zone(double time, Agent agent) {
            super(time, agent, null);
        }

        @Override
        void apply(EventCore core) {
            core.mark(first);
        }
    }

    /**
     * Hybrid, criterion E2: until now no time-step agent could have reached the skin of this one
     * (the safe horizon of DESIGN.md 2.7). It changes no velocity, so it has its own stamp: a new
     * skin check kills the previous one without touching the agent's other predictions.
     */
    static final class Skin extends Event {
        private final int stamp;

        Skin(double time, Agent agent) {
            super(time, agent, null);
            stamp = agent.skinStamp;
        }

        @Override
        boolean stale() {
            return super.stale() || first.skinStamp != stamp;
        }

        @Override
        void apply(EventCore core) {
            core.watch(first);
        }
    }

    /** Writes the state every dt2: the only event that moves all the agents. */
    static final class Write extends Event {
        Write(double time) {
            super(time, null, null);
        }

        @Override
        void apply(EventCore core) {
            core.write(time);
        }
    }
}
