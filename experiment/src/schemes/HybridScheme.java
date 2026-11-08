package schemes;

import models.Agent;
import models.PedestrianModel;
import models.Room;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;

/**
 * Hybrid EDMD + TSDMD [DESIGN.md 2.6, 2.7 and 5]. A global clock of step dt: in each interval the
 * events of the event-driven agents come first, then one Beeman step of the time-step agents, whose
 * forces only involve time-step agents. Scheme changes happen at the end of the step, with every agent
 * at the same instant:
 *  - marked agents (E1 zone, E2 skin, E3 short flight) become time-step, and so does, in a chain,
 *    every event-driven agent within δ_in of a time-step one (E2 at the boundary);
 *  - event-driven agents near a time-step agent that just appeared in their 3×3 cells get a skin check;
 *  - time-step agents that meet S1, S2 and S3 go back to event-driven.
 * No event-driven agent ever touches a time-step one: the skin check marks it at δ_in, and it switches
 * at most one step later, after moving (|v| + maxSpeed)·dt, far less than δ_in.
 */
public final class HybridScheme extends Scheme {
    private final double dt, maxSpeed;
    private final int dumpEvery;
    private final long maxEvents;
    private final SwitchingCriterion criterion;
    private final ForceStepper stepper;
    private final EventCore core;
    private final List<Agent> timeStep = new ArrayList<>();
    private final int[] cells;
    private final Agent[] neighbors;
    private int remaining;

    public HybridScheme(Room room, List<Agent> agents, PedestrianModel model, double cutoff, double maxTime,
                        long maxEvents, double pairRestitution, double wallRestitution, double separationSpeed,
                        double dt, int dumpEvery, SwitchingCriterion criterion, double maxSpeed) {
        super(room, agents, model, cutoff, maxTime);
        // Beyond the 3×3 cells the gap is at least one cell minus two radii: S2 must see that far.
        if (cutoff < 2 * maxRadius(agents) + criterion.skinOut())
            throw new IllegalArgumentException("Cells narrower than 2 r_max + delta_out");
        if (2 * maxSpeed * dt >= criterion.skinIn())
            throw new IllegalArgumentException("Need 2 v_max dt < delta_in");
        this.dt = dt;
        this.dumpEvery = dumpEvery;
        this.maxEvents = maxEvents;
        this.criterion = criterion;
        this.maxSpeed = maxSpeed;
        stepper = new ForceStepper(room, grid, model, dt, agents.size());
        cells = new int[agents.size()];
        neighbors = new Agent[agents.size()];
        for (Agent agent : agents)
            agent.scheme = criterion.zoneEntryTime(agent, room) == 0 ? Agent.Scheme.TIME_STEP : Agent.Scheme.EVENT_DRIVEN;
        core = new EventCore(room, grid, model, agents, (agent, time) -> {
            updateProgress(agent, time);
            if (!agent.active()) remaining--;
        }, null, pairRestitution, wallRestitution, separationSpeed, dt, dumpEvery);
        core.enableSwitching(criterion, maxSpeed);
    }

    private static double maxRadius(List<Agent> agents) {
        return agents.stream().mapToDouble(agent -> agent.radius).max().orElse(0);
    }

    @Override
    public boolean run(FrameSink sink) throws IOException {
        remaining = agents.size();
        ArrayDeque<Agent> fresh = new ArrayDeque<>();
        for (Agent agent : agents) if (agent.scheme == Agent.Scheme.TIME_STEP) fresh.add(agent);
        spread(fresh, 0);
        rebuildTimeStep();
        stepper.prime(timeStep);
        core.start();
        sink.write(0);
        double time = 0;
        while (remaining > 0 && time < maxTime && core.processed() < maxEvents) {
            steps++;
            time = steps * dt;
            core.processUntil(time, maxEvents);
            // Cut by maxEvents inside the step: events before `time` are left, so nothing may advance past them.
            if (core.processed() >= maxEvents) break;
            if (!timeStep.isEmpty()) stepTimeStep(time);
            core.settle(time);
            switchSchemes(time);
            events = core.processed();
            if (steps % dumpEvery == 0 || remaining == 0) {
                for (Agent agent : agents)
                    if (agent.active() && agent.scheme == Agent.Scheme.EVENT_DRIVEN) agent.advanceTo(time);
                sink.write(time);
            }
        }
        return remaining == 0;
    }

    private void stepTimeStep(double time) {
        for (Agent agent : timeStep) cells[agent.id] = grid.cellOf(agent);
        stepper.step(timeStep);
        for (Agent agent : timeStep) {
            updateProgress(agent, time);
            if (!agent.active()) remaining--;
            else if (Math.hypot(agent.vx, agent.vy) > maxSpeed)
                throw new IllegalStateException("Time-step agent " + agent.id + " at " + Math.hypot(agent.vx, agent.vy)
                    + " m/s > v_max at t=" + time + ": the skin guarantee does not hold");
        }
    }

    private void switchSchemes(double time) {
        boolean changed = false;
        ArrayDeque<Agent> fresh = new ArrayDeque<>();
        for (Agent agent : core.drainMarked()) {
            if (!agent.active() || agent.scheme == Agent.Scheme.TIME_STEP) continue;
            requireApart(agent, time);
            toTimeStep(agent, time);
            fresh.add(agent);
            changed = true;
        }
        // A time-step agent that entered a new cell may now be in the 3×3 cells of event-driven ones.
        for (Agent agent : timeStep) if (agent.active() && grid.cellOf(agent) != cells[agent.id]) fresh.add(agent);
        changed |= spread(fresh, time);
        List<Agent> leaving = new ArrayList<>();
        for (Agent agent : timeStep)
            if (agent.active() && criterion.toEventDriven(agent, room, () -> model.velocityDeviation(agent, room),
                () -> smallestGap(agent, time))) leaving.add(agent);
        for (Agent agent : leaving) toEventDriven(agent, time);
        if (changed || !leaving.isEmpty() || timeStep.removeIf(agent -> !agent.active())) rebuildTimeStep();
    }

    /**
     * Pasaje en cadena [DESIGN.md 2.7]: every event-driven neighbor of a time-step agent in `fresh`
     * within δ_in becomes time-step too, and the rest get a new skin check. True if anyone switched.
     */
    private boolean spread(ArrayDeque<Agent> fresh, double time) {
        boolean switched = false;
        while (!fresh.isEmpty()) {
            Agent agent = fresh.poll();
            int count = grid.neighbors(agent, neighbors);
            for (int k = 0; k < count; k++) {
                Agent other = neighbors[k];
                if (other.scheme != Agent.Scheme.EVENT_DRIVEN) continue;
                double gap = agent.gapAt(other, time);
                if (gap <= 0) throw contactLost(other, agent, time);
                if (gap <= criterion.skinIn()) {
                    toTimeStep(other, time);
                    fresh.add(other);
                    switched = true;
                } else {
                    core.watch(other);
                }
            }
        }
        return switched;
    }

    /** The guarantee of DESIGN.md 2.7, checked: a marked agent reaches the end of the step untouched. */
    private void requireApart(Agent agent, double time) {
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) {
            Agent other = neighbors[k];
            if (other.scheme == Agent.Scheme.TIME_STEP && agent.gapAt(other, time) <= 0)
                throw contactLost(agent, other, time);
        }
    }

    private static IllegalStateException contactLost(Agent eventDriven, Agent timeStep, double time) {
        return new IllegalStateException("Event-driven agent " + eventDriven.id + " touches time-step agent "
            + timeStep.id + " at t=" + time);
    }

    /** Enters TSDMD with the relaxation owed so far applied: a(t) = a(t - dt) = driving force alone (decision ★3). */
    private void toTimeStep(Agent agent, double time) {
        agent.advanceTo(time);
        core.settleRelaxation(agent);
        grid.update(agent);
        agent.scheme = Agent.Scheme.TIME_STEP;
        agent.switchPending = false;
        agent.phantomTime = Double.POSITIVE_INFINITY;
        agent.skinStamp++;
        agent.revision++;
        agent.fx = agent.fy = 0;
        model.addDrivingForce(agent, room);
        agent.ax = agent.previousAx = agent.fx / agent.mass;
        agent.ay = agent.previousAy = agent.fy / agent.mass;
    }

    /** Back to EDMD: free flight from its velocity now, relaxing with phantom events; E3 starts over. */
    private void toEventDriven(Agent agent, double time) {
        agent.time = time;
        agent.scheme = Agent.Scheme.EVENT_DRIVEN;
        agent.lastCollision = Double.NEGATIVE_INFINITY;
        agent.relaxedAt = time;
        agent.phantomTime = time + model.characteristicTime(agent);
        EventCore.clearKicks(agent);
        agent.revision++;
        core.predict(agent);
    }

    /** S2: smallest gap to any agent; a wall contact counts as 0. Beyond the 3×3 cells it is ≥ δ_out. */
    private double smallestGap(Agent agent, double time) {
        boolean[] wall = {false};
        room.forEachWallContact(agent, (x, y) -> wall[0] = true);
        if (wall[0]) return 0;
        double gap = Double.POSITIVE_INFINITY;
        int count = grid.neighbors(agent, neighbors);
        for (int k = 0; k < count; k++) gap = Math.min(gap, agent.gapAt(neighbors[k], time));
        return gap;
    }

    /** By id, so a hybrid with every agent in TSDMD sums forces in the same order as pure TSDMD. */
    private void rebuildTimeStep() {
        timeStep.clear();
        for (Agent agent : agents)
            if (agent.active() && agent.scheme == Agent.Scheme.TIME_STEP) timeStep.add(agent);
    }
}
