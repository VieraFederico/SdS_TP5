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
 * then the Step, then the frame. Everybody starts event-driven. One criterion, the time between
 * contacts Δt against t_s, with Δt = 0 for a contact with an agent of the other scheme (soft, it lasts):
 *  - an event-driven agent with two contacts closer than t_s (marked by EventCore) becomes time-step;
 *  - an event-driven agent that is going to touch a time-step one before the next step becomes
 *    time-step now, before touching it, in a chain. Beeman's position r(t + dt) only needs the state at
 *    t, so each time-step agent follows a known segment until the next step and the contact is predicted
 *    with the EDMD quadratic. If an event turns the agent in the middle of the step, EventCore predicts
 *    the contact again and the agent bounces on the segment, as on a moving door edge (no overlap);
 *  - a time-step agent that has gone t_s without touching anybody (agents or walls), and that is not going
 *    to touch a time-step one before the next step, goes back.
 * No switch ever starts with an overlap, and no contact is missed. t_s = 0 never switches anyone: pure EDMD.
 */
public final class HybridScheme extends Scheme {
    private final double dt, flightThreshold;
    /** How far a time-step agent may move in one step and still have its contacts in its 3×3 cells (m). */
    private final double cellSlack;
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
        // Time-step agents stay filed where the step left them while they move along their segment.
        cellSlack = cutoff - 2 * maxRadius(agents);
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

    /** How many event-driven agents bounced on a time-step one in the middle of a step (checks). */
    public long timeStepContacts() {
        return core.timeStepContacts();
    }

    /**
     * The Step event at n·dt: Beeman on the time-step agents, their segments until (n + 1)·dt, then the
     * scheme changes (which already see the segments of the agents that are time-step from now on).
     */
    private void step(long n) {
        pendingStep = 0;
        steps = n;
        double time = n * dt;
        if (!timeStep.isEmpty()) stepTimeStep(time);
        for (Agent agent : timeStep) if (agent.active()) plan(agent);
        core.window(time, time + dt);
        switchSchemes(time);
        if (remaining > 0 && !timeStep.isEmpty()) scheduleStep(n + 1);
    }

    /**
     * Segment of a time-step agent until the next step: Beeman's position, r(t + dt) = r + v dt +
     * (2/3 a(t) - 1/6 a(t - dt)) dt², only needs the state at t. The agent stays filed in the cell of r(t),
     * so it must not move more than the slack of the cells (a speed over 20 m/s with the default ones).
     */
    private void plan(Agent agent) {
        agent.stepVx = agent.vx + (2.0 / 3 * agent.ax - 1.0 / 6 * agent.previousAx) * dt;
        agent.stepVy = agent.vy + (2.0 / 3 * agent.ay - 1.0 / 6 * agent.previousAy) * dt;
        if (Math.hypot(agent.stepVx, agent.stepVy) * dt > cellSlack)
            throw new IllegalStateException("A time-step agent moves more than the cell slack in one step");
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
            else if (time - agent.lastContact >= flightThreshold && !aboutToTouchTimeStep(agent)) leaving.add(agent);
        }
        for (Agent agent : leaving) toEventDriven(agent, time);
        if (changed || !leaving.isEmpty() || timeStep.removeIf(agent -> !agent.active())) rebuildTimeStep();
    }

    /**
     * Contact between the schemes [DESIGN.md 2.7]: every event-driven agent that is going to touch the
     * segment of a time-step agent in `fresh` before the next step becomes time-step now, before touching
     * it, and its own neighbors are checked in turn against its new segment. True if any did.
     */
    private boolean catchContacts(ArrayDeque<Agent> fresh, double time) {
        boolean switched = false;
        while (!fresh.isEmpty()) {
            Agent agent = fresh.poll();
            int count = grid.neighbors(agent, neighbors);
            for (int k = 0; k < count; k++) {
                Agent other = neighbors[k];
                if (other.scheme != Agent.Scheme.EVENT_DRIVEN || !other.active()) continue;
                if (!Double.isFinite(core.timeStepContactTime(other, agent))) continue;
                other.lastContact = time;
                toTimeStep(other, time);
                fresh.add(other);
                switched = true;
            }
        }
        return switched;
    }

    /**
     * Whether the agent, back in EDMD (straight with its velocity now), would touch the segment of a
     * time-step agent before the next step: then it stays, or it would go back only to touch it.
     */
    private boolean aboutToTouchTimeStep(Agent agent) {
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) {
            Agent other = neighbors[k];
            if (other.scheme != Agent.Scheme.TIME_STEP || !other.active()) continue;
            double time = EventCore.collisionTime(other.x - agent.x, other.y - agent.y, other.stepVx - agent.vx,
                other.stepVy - agent.vy, agent.radius + other.radius);
            if (time <= dt) return true;
        }
        return false;
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
        plan(agent);
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
