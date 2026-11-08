package schemes;

import models.Agent;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Pure TSDMD: every agent, every dt. */
public final class TimeStepScheme extends Scheme {
    private final double dt;
    private final int dumpEvery;
    private final ForceStepper stepper;

    public TimeStepScheme(Room room, List<Agent> agents, PedestrianModel model, double cutoff, double maxTime,
                          double dt, int dumpEvery) {
        super(room, agents, model, cutoff, maxTime);
        this.dt = dt;
        this.dumpEvery = dumpEvery;
        stepper = new ForceStepper(room, grid, model, dt, agents.size());
        agents.forEach(agent -> agent.scheme = Agent.Scheme.TIME_STEP);
    }

    @Override
    public boolean run(FrameSink sink) throws IOException {
        List<Agent> active = new ArrayList<>(agents);
        stepper.prime(active);
        sink.write(0);
        double time = 0;
        while (!active.isEmpty() && time < maxTime) {
            steps++;
            time = steps * dt;
            stepper.step(active);
            for (Agent agent : active) updateProgress(agent, time);
            active.removeIf(agent -> !agent.active());
            if (steps % dumpEvery == 0 || active.isEmpty()) sink.write(time);
        }
        return active.isEmpty();
    }
}
