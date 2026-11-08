package schemes;

import models.Agent;
import models.CellGrid;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.util.List;

/** What the three schemes share: the state, the geometry and the agents' progress to the exit. */
public abstract class Scheme {
    @FunctionalInterface
    public interface FrameSink {
        void write(double time) throws IOException;
    }

    protected final Room room;
    protected final List<Agent> agents;
    protected final PedestrianModel model;
    protected final CellGrid grid;
    protected final double maxTime;
    protected long steps, events;

    protected Scheme(Room room, List<Agent> agents, PedestrianModel model, double cutoff, double maxTime) {
        this.room = room;
        this.agents = agents;
        this.model = model;
        this.maxTime = maxTime;
        grid = new CellGrid(room, cutoff, agents.size());
        for (Agent agent : agents) {
            model.setFreeFlight(agent, room);
            grid.insert(agent);
        }
    }

    /** Simulates until the last agent retires [Correcciones_TP4, D30]; false if maxTime stopped it. */
    public abstract boolean run(FrameSink sink) throws IOException;

    public long steps() {
        return steps;
    }

    public long events() {
        return events;
    }

    /** Change of target, door crossing and retirement. Exit times are records, not observables. */
    protected void updateProgress(Agent agent, double time) {
        if (!Double.isFinite(agent.x + agent.y + agent.vx + agent.vy))
            throw new IllegalStateException("Nonfinite state of agent " + agent.id + " at t=" + time);
        if (room.reachedDoor(agent)) agent.headingOut = true;
        if (agent.y <= 0 && agent.location == Agent.Location.INSIDE) {
            agent.location = Agent.Location.PASSED_DOOR;
            agent.doorTime = time;
        }
        if (agent.y <= -Room.EXIT_DISTANCE) {
            agent.location = Agent.Location.RETIRED;
            agent.retireTime = time;
            agent.vx = agent.vy = 0;
            grid.remove(agent);
        }
    }
}
