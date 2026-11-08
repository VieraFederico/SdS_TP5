package schemes;

import models.Agent;
import models.CellGrid;
import models.PedestrianModel;
import models.Room;
import java.util.List;

/** One TSDMD step of the agents it is given. Forces only act between time-step agents. */
final class ForceStepper {
    private final Room room;
    private final CellGrid grid;
    private final PedestrianModel model;
    private final BeemanIntegrator integrator;
    private final Agent[] neighbors;

    ForceStepper(Room room, CellGrid grid, PedestrianModel model, double dt, int n) {
        this.room = room;
        this.grid = grid;
        this.model = model;
        integrator = new BeemanIntegrator(dt, n);
        neighbors = new Agent[n];
    }

    void step(List<Agent> agents) {
        integrator.advance(agents, this::evaluateForces);
    }

    /** a(t) and, as a start, a(t-Δt) = a(t): exact in free flight, where both are zero. */
    void prime(List<Agent> agents) {
        evaluateForces(agents);
        for (Agent a : agents) {
            a.ax = a.previousAx = a.fx / a.mass;
            a.ay = a.previousAy = a.fy / a.mass;
        }
    }

    private void evaluateForces(List<Agent> agents) {
        for (Agent a : agents) {
            a.fx = a.fy = 0;
            grid.update(a);
        }
        for (Agent a : agents) {
            model.addDrivingForce(a, room);
            room.forEachWallContact(a, (x, y) -> model.addWallForce(a, x, y));
            int count = grid.neighbors(a, neighbors);
            for (int k = 0; k < count; k++) {
                Agent other = neighbors[k];
                if (other.id > a.id && other.scheme == Agent.Scheme.TIME_STEP) model.addContactForce(a, other);
            }
        }
    }
}
