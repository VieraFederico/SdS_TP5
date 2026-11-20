package schemes;

import models.Agent;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.util.List;

/**
 * Pure EDMD. Hard discs in free flight between events, with the elastic collisions of the TP3. The
 * cut-off (maxTime, maxEvents) is part of the scheme: not finishing is a valid result, reported as
 * complete = false. dt only sets the frame interval dt2 = dumpEvery·dt, as in the other two schemes.
 */
public final class EventDrivenScheme extends Scheme {
    private final double dt;
    private final int dumpEvery;
    private final long maxEvents;

    public EventDrivenScheme(Room room, List<Agent> agents, PedestrianModel model, double cutoff, double maxTime,
                             long maxEvents, double dt, int dumpEvery) {
        super(room, agents, model, cutoff, maxTime);
        this.maxEvents = maxEvents;
        this.dt = dt;
        this.dumpEvery = dumpEvery;
        agents.forEach(agent -> agent.scheme = Agent.Scheme.EVENT_DRIVEN);
    }

    @Override
    public boolean run(FrameSink sink) throws IOException {
        int[] remaining = {agents.size()};
        EventCore core = new EventCore(room, grid, model, agents, (agent, time) -> {
            updateProgress(agent, time);
            if (!agent.active()) remaining[0]--;
        }, sink, dt, dumpEvery);
        sink.write(0);
        core.start();
        while (remaining[0] > 0 && core.processed() < maxEvents && core.processNext(maxTime)) events = core.processed();
        for (Agent agent : agents) if (agent.active()) agent.advanceTo(core.now());
        if (core.now() > core.lastFrame()) sink.write(core.now());
        return remaining[0] == 0;
    }
}
