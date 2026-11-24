package schemes;

import models.Agent;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Hybrid EDMD + TSDMD [DESIGN.md 2.6, 2.7 and 5]. The global clock is the event queue, as in pure EDMD.
 * The time-step agents advance in Step events at n·dt (one Beeman step; their forces only involve
 * time-step agents), which exist only while some agent is time-step or marked to become one: with
 * nobody in TSDMD the hybrid goes from event to event. At the same instant, agent events come first,
 * then the Step, then the frame. Everybody starts event-driven. Scheme changes happen in the Step,
 * with every agent at its instant, and they all measure the time between contacts against t_s:
 *  - an event-driven agent with two contacts closer than t_s (marked by EventCore) becomes time-step;
 *  - an event-driven agent that overlaps a time-step one becomes time-step too, in a chain: a contact
 *    with a soft agent lasts, so the next contact is 0 < t_s away. The overlap is not predicted, it is
 *    seen at the step, as TSDMD sees its own contacts: at most (|v_e| + |v_t|)·dt, a few millimetres;
 *  - a time-step agent that has gone t_s without touching anybody (agents or walls) goes back.
 * t_s = 0 never switches anyone: pure EDMD.
 */
public final class HybridScheme extends Scheme {
    private final double dt, flightThreshold;
    private final int dumpEvery;
    private final long maxEvents;
    private final ForceStepper stepper;
    private final EventCore core;
    private final List<Agent> timeStep = new ArrayList<>();
    private final Agent[] neighbors;
    private int remaining;
    /** Index n of the pending Step at n·dt; 0 if none is pending. */
    private long pendingStep;

    public HybridScheme(Room room, List<Agent> agents, PedestrianModel model, double cutoff, double maxTime,
                        long maxEvents, double dt, int dumpEvery, double flightThreshold) {
        super(room, agents, model, cutoff, maxTime);
        // Every contact must be in the 3×3 cells of the agent.
        if (cutoff < 2 * maxRadius(agents)) throw new IllegalArgumentException("Cells narrower than 2 r_max");
        if (flightThreshold < 0) throw new IllegalArgumentException("Need t_s >= 0");
        this.dt = dt;
        this.dumpEvery = dumpEvery;
        this.maxEvents = maxEvents;
        this.flightThreshold = flightThreshold;
        stepper = new ForceStepper(room, grid, model, dt, agents.size());
        neighbors = new Agent[agents.size()];
        agents.forEach(agent -> agent.scheme = Agent.Scheme.EVENT_DRIVEN);
        core = new EventCore(room, grid, model, agents, (agent, time) -> {
            updateProgress(agent, time);
            if (!agent.active()) remaining--;
        }, null, dt, dumpEvery);
        core.enableSwitching(flightThreshold, this::requestStep);
    }

    private static double maxRadius(List<Agent> agents) {
        return agents.stream().mapToDouble(agent -> agent.radius).max().orElse(0);
    }

    @Override
    public boolean run(FrameSink sink) throws IOException {
        remaining = agents.size();
        core.output(sink);
        sink.write(0);
        core.start();
        while (remaining > 0 && core.processed() < maxEvents && core.processNext(maxTime)) events = core.processed();
        // Final frame, only with every agent at the same instant: a cut by maxEvents between Steps leaves
        // the time-step agents behind the event-driven ones, and that frame is not written.
        double end = core.now();
        boolean together = timeStep.stream().noneMatch(Agent::active) || end == steps * dt;
        if (together && end > core.lastFrame()) {
            for (Agent agent : agents)
                if (agent.active() && agent.scheme == Agent.Scheme.EVENT_DRIVEN) agent.advanceTo(end);
            sink.write(end);
        }
        return remaining == 0;
    }

    /**
     * A mark with no Step pending (nobody in TSDMD): the next one on the grid n·dt, the first instant of
     * it not before now. On the grid, so frames (k·dump·dt) always find the time-step agents there.
     */
    private void requestStep() {
        if (pendingStep != 0) return;
        double now = core.now();
        long n = Math.max(steps + 1, (long) Math.ceil(now / dt));
        while (n * dt < now) n++;
        scheduleStep(n);
    }

    private void scheduleStep(long n) {
        pendingStep = n;
        core.schedule(new Event.Step(n * dt, () -> step(n)));
    }

    /** The Step event at n·dt: Beeman on the time-step agents, then the scheme changes. */
    private void step(long n) {
        pendingStep = 0;
        steps = n;
        double time = n * dt;
        if (!timeStep.isEmpty()) stepTimeStep(time);
        switchSchemes(time);
        if (remaining > 0 && !timeStep.isEmpty()) scheduleStep(n + 1);
    }

    private void stepTimeStep(double time) {
        stepper.step(timeStep);
        for (Agent agent : timeStep) {
            updateProgress(agent, time);
            if (!agent.active()) remaining--;
        }
    }

    private void switchSchemes(double time) {
        boolean changed = false;
        ArrayDeque<Agent> fresh = new ArrayDeque<>();
        for (Agent agent : core.drainMarked()) {
            if (!agent.active() || agent.scheme == Agent.Scheme.TIME_STEP) continue;
            toTimeStep(agent, time);
            fresh.add(agent);
            changed = true;
        }
        for (Agent agent : timeStep) if (agent.active()) fresh.add(agent);
        changed |= catchContacts(fresh, time);
        // Only the agents that were already time-step: the ones that just switched stay at least one step.
        List<Agent> leaving = new ArrayList<>();
        for (Agent agent : timeStep) {
            if (!agent.active()) continue;
            if (touching(agent, time)) agent.lastContact = time;
            else if (time - agent.lastContact >= flightThreshold) leaving.add(agent);
        }
        for (Agent agent : leaving) toEventDriven(agent, time);
        if (changed || !leaving.isEmpty() || timeStep.removeIf(agent -> !agent.active())) rebuildTimeStep();
    }

    /**
     * Contact between the schemes [DESIGN.md 2.7]: every event-driven agent that overlaps a time-step
     * agent in `fresh` becomes time-step too, and its own neighbors are checked in turn. True if any did.
     */
    private boolean catchContacts(ArrayDeque<Agent> fresh, double time) {
        boolean switched = false;
        while (!fresh.isEmpty()) {
            Agent agent = fresh.poll();
            int count = grid.neighbors(agent, neighbors);
            for (int k = 0; k < count; k++) {
                Agent other = neighbors[k];
                if (other.scheme != Agent.Scheme.EVENT_DRIVEN || agent.gapAt(other, time) >= 0) continue;
                other.lastContact = time;
                toTimeStep(other, time);
                fresh.add(other);
                switched = true;
            }
        }
        return switched;
    }

    /** Whether the agent overlaps any other agent or a wall; all contacts are in its 3×3 cells. */
    private boolean touching(Agent agent, double time) {
        boolean[] wall = {false};
        room.forEachWallContact(agent, (x, y) -> wall[0] = true);
        if (wall[0]) return true;
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) if (agent.gapAt(neighbors[k], time) < 0) return true;
        return false;
    }

    /** Enters TSDMD with the relaxation owed so far applied: a(t) = a(t - dt) = driving force alone (decision ★3). */
    private void toTimeStep(Agent agent, double time) {
        agent.advanceTo(time);
        core.settleRelaxation(agent);
        grid.update(agent);
        agent.scheme = Agent.Scheme.TIME_STEP;
        agent.switchPending = false;
        agent.phantomTime = Double.POSITIVE_INFINITY;
        agent.revision++;
        agent.fx = agent.fy = 0;
        model.addDrivingForce(agent, room);
        agent.ax = agent.previousAx = agent.fx / agent.mass;
        agent.ay = agent.previousAy = agent.fy / agent.mass;
    }

    /**
     * Back to EDMD, touching nobody: free flight from its velocity now, relaxing with phantom events. Its
     * last contact was t_s ago or more, so its next collision alone cannot send it back.
     */
    private void toEventDriven(Agent agent, double time) {
        agent.time = time;
        agent.scheme = Agent.Scheme.EVENT_DRIVEN;
        agent.relaxedAt = time;
        agent.phantomTime = time + model.characteristicTime(agent);
        EventCore.clearKicks(agent);
        agent.revision++;
        core.predict(agent);
    }

    /** By id, so the time-step agents sum their forces in the same order as in pure TSDMD. */
    private void rebuildTimeStep() {
        timeStep.clear();
        for (Agent agent : agents)
            if (agent.active() && agent.scheme == Agent.Scheme.TIME_STEP) timeStep.add(agent);
    }
}
