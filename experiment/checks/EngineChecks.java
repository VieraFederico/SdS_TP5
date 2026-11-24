import models.Agent;
import models.CellGrid;
import models.PedestrianModel;
import models.Room;
import models.SocialForceModel;
import schemes.EventCore;
import schemes.EventDrivenScheme;
import schemes.HybridScheme;
import schemes.TimeStepScheme;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/** The only checks program, without a framework, as in the TP4. Values are computed by hand. */
public final class EngineChecks {
    private static int passed;

    public static void main(String[] args) throws Exception {
        placement();
        placementIsReproducible();
        wallContacts();
        targets();
        cellGridMatchesBruteForce();
        outputFiles();
        overlapAndContactForce();
        wallForce();
        freeFlightFeelsNoForce();
        beemanRelaxation();
        loneAgentExitsOnTime();
        twoAgentsCollideAndSurvive();
        collisionTimeByHand();
        collisionIsTheTP3One();
        wallIsAMirror();
        touchingAndApproachingCollideNow();
        unequalMassesAreRejected();
        loneAgentInEventDriven();
        collisionThenBounceBack();
        eventDrivenWritesEveryDt2();
        loneAgentInHybrid();
        shortFlightSwitchesToTimeStep();
        isolatedCollisionStaysEventDriven();
        hybridAtZeroIsEventDriven();
        contactsWithTimeStepAreCaught();
        collisionIsDampedOnlyAfterIt();
        slidingIntoTheOpeningTurnsAtOnce();
        agentOnACellBorderMovesOn();
        cutOffsEndCleanly();
        invalidFlagsAreRejected();
        System.out.println(passed + " checks passed");
    }

    private static void check(boolean condition, String name) {
        if (!condition) throw new AssertionError("FAILED: " + name);
        passed++;
    }

    private static void near(double actual, double expected, double tolerance, String name) {
        check(Math.abs(actual - expected) <= tolerance, name + ": " + actual + " vs " + expected);
    }

    private static void placement() {
        Room room = new Room(50, .8);
        List<Agent> agents = room.placeAgents(1000, .18, .25, 80, new Random(7));
        check(agents.size() == 1000, "all agents placed");
        for (Agent a : agents) {
            check(a.radius >= .18 && a.radius <= .25, "radius in range");
            check(a.x >= a.radius && a.x <= 50 - a.radius && a.y >= a.radius && a.y <= 50 - a.radius, "inside");
            for (Agent b : agents) check(a == b || a.gap(b) > 0, "no overlap");
        }
    }

    private static void placementIsReproducible() {
        Room room = new Room(50, .8);
        Agent a = room.placeAgents(50, .18, .25, 80, new Random(3)).getLast();
        Agent b = room.placeAgents(50, .18, .25, 80, new Random(3)).getLast();
        check(a.x == b.x && a.y == b.y && a.radius == b.radius, "same seed, same agents");
    }

    private static List<double[]> contacts(Room room, double x, double y, double radius) {
        List<double[]> points = new ArrayList<>();
        room.forEachWallContact(new Agent(0, radius, 80, x, y), (px, py) -> points.add(new double[]{px, py}));
        return points;
    }

    private static void wallContacts() {
        Room room = new Room(50, .8);
        check(contacts(room, 10, 10, .2).isEmpty(), "no contact in the middle");
        List<double[]> wall = contacts(room, 10, .15, .2);
        check(wall.size() == 1, "one contact on the wall");
        near(wall.getFirst()[0], 10, 1e-12, "wall contact x");
        near(wall.getFirst()[1], 0, 1e-12, "wall contact y");
        check(contacts(room, 25, .1, .2).isEmpty(), "no wall in the middle of the door");
        List<double[]> edge = contacts(room, 24.65, .1, .2);
        check(edge.size() == 1, "door edge contact");
        near(edge.getFirst()[0], 24.6, 1e-12, "door edge is the end of the segment");
        check(contacts(room, .1, .1, .2).size() == 2, "corner touches two walls");
        check(contacts(room, 24.65, -.1, .2).size() == 1, "door edge from outside");
    }

    private static void targets() {
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 1, 2);
        near(room.targetY(agent), .2, 0, "first target one radius from the wall");
        check(!room.reachedDoor(agent), "far from the door");
        agent.x = 25.1;
        agent.y = .19;
        check(room.reachedDoor(agent), "inside the opening");
        agent.x = 24;
        check(!room.reachedDoor(agent), "against the wall beside the door");
        agent.headingOut = true;
        near(room.targetY(agent), -2, 0, "second target outside");
    }

    private static void cellGridMatchesBruteForce() {
        Room room = new Room(20, .8);
        double cutoff = .5;
        Random random = new Random(11);
        List<Agent> agents = room.placeAgents(300, .18, .25, 80, random);
        CellGrid grid = new CellGrid(room, cutoff, agents.size());
        agents.forEach(grid::insert);
        Agent[] buffer = new Agent[agents.size()];
        for (int round = 0; round < 5; round++) {
            for (Agent a : agents) {
                a.x = Math.max(0, Math.min(20, a.x + random.nextGaussian()));
                a.y = Math.max(-1, Math.min(20, a.y + random.nextGaussian()));
                grid.update(a);
            }
            for (Agent a : agents) {
                int count = grid.neighbors(a, buffer);
                Set<Integer> found = new HashSet<>();
                for (int k = 0; k < count; k++) found.add(buffer[k].id);
                check(found.size() == count, "no repeated neighbors");
                for (Agent b : agents)
                    if (a != b && Math.hypot(a.x - b.x, a.y - b.y) < cutoff)
                        check(found.contains(b.id), "close pair found");
            }
        }
        grid.remove(agents.getFirst());
        Agent other = agents.get(1);
        int count = grid.neighbors(other, buffer);
        for (int k = 0; k < count; k++) check(buffer[k] != agents.getFirst(), "removed agent is gone");
    }

    private static void outputFiles() throws Exception {
        Path directory = Files.createTempDirectory("tp5-checks");
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .25, 80, 1.5, 2);
        agent.vx = -1.25;
        agent.vy = 1e-7;
        agent.scheme = Agent.Scheme.EVENT_DRIVEN;
        agent.location = Agent.Location.PASSED_DOOR;
        agent.doorTime = 3.5;
        try (OutputWriter writer = new OutputWriter(directory, room, List.of(agent))) {
            writer.frame(.5, List.of(agent));
            writer.exits(List.of(agent));
        }
        check(Files.readAllLines(directory.resolve("static.txt")).equals(List.of("1", "50.0", "0.8", "0.25 0")), "static");
        check(Files.readAllLines(directory.resolve("dynamic.txt"))
            .equals(List.of("0.5", "0 1.500000 2.000000 -1.250000 0.000000 0 1")), "dynamic");
        check(Files.readAllLines(directory.resolve("exits.txt")).equals(List.of("0 3.5 NaN")), "exits");
    }

    private static void overlapAndContactForce() {
        // R_i = 0.25, R_j = 0.20, centres 0.44 m apart along (0.8, 0.6): ξ = 0.01 m.
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .20, 80, .44 * .8, .44 * .6);
        near(-i.gap(j), .01, 1e-12, "overlap");
        i.vx = 1;
        i.vy = .5;
        new SocialForceModel(SocialForceModel.KT).addContactForce(i, j);
        near(i.fx, -1248, 1e-6, "contact force x");
        near(i.fy, -336, 1e-6, "contact force y");
        near(j.fx, 1248, 1e-6, "action and reaction x");
        near(j.fy, 336, 1e-6, "action and reaction y");
    }

    private static void wallForce() {
        Agent agent = new Agent(0, .2, 80, 10, .19);
        agent.vx = 1;
        new SocialForceModel(SocialForceModel.KT).addWallForce(agent, 10, 0);
        near(agent.fy, 1200, 1e-6, "normal push from the wall");
        near(agent.fx, -2400, 1e-6, "tangential friction with the wall");
    }

    private static void freeFlightFeelsNoForce() {
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 3, 40);
        SocialForceModel model = new SocialForceModel(SocialForceModel.KT);
        model.setFreeFlight(agent, room);
        near(Math.hypot(agent.vx, agent.vy), 1.5, 1e-12, "desired speed");
        model.addDrivingForce(agent, room);
        near(agent.fx, 0, 1e-9, "no force x");
        near(agent.fy, 0, 1e-9, "no force y");
    }

    private static void beemanRelaxation() throws Exception {
        // Starting at rest on the door axis: v(t) = vd (1 - exp(-t/τ)) [Teorica_6, diapo 31].
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 25, 40);
        double[] speed = new double[1];
        var scheme = new TimeStepScheme(room, List.of(agent), new SocialForceModel(SocialForceModel.KT), .5, 1, 1e-3, 1);
        agent.vx = agent.vy = 0;
        scheme.run(time -> speed[0] = Math.hypot(agent.vx, agent.vy));
        near(speed[0], 1.5 * (1 - Math.exp(-1 / .5)), 1e-5, "exponential relaxation");
    }

    private static void loneAgentExitsOnTime() throws Exception {
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 25, 10);
        new TimeStepScheme(room, List.of(agent), new SocialForceModel(SocialForceModel.KT), .5, 100, 1e-3, 1000).run(time -> { });
        near(agent.doorTime, 10 / 1.5, 2e-3, "door time on the axis");
        near(agent.retireTime, 11 / 1.5, 2e-3, "retire time on the axis");
        check(agent.location == Agent.Location.RETIRED, "retired");
        Agent diagonal = new Agent(0, .2, 80, 10, 20);
        new TimeStepScheme(room, List.of(diagonal), new SocialForceModel(SocialForceModel.KT), .5, 100, 1e-3, 1000).run(time -> { });
        double path = Math.hypot(15, 19.8);
        check(diagonal.doorTime > path / 1.5 && diagonal.doorTime < path / 1.5 + .5, "off-axis door time");
    }

    private static void twoAgentsCollideAndSurvive() throws Exception {
        // Same line, the second one stopped ahead: contact, then both leave without big overlaps.
        Room room = new Room(50, .8);
        Agent a = new Agent(0, .25, 80, 25, 20), b = new Agent(1, .25, 80, 25, 15);
        PedestrianModel model = new SocialForceModel(SocialForceModel.KT);
        double[] closest = {Double.MAX_VALUE};
        var scheme = new TimeStepScheme(room, List.of(a, b), model, .5, 100, 1e-3, 1);
        b.vy = 0;
        boolean done = scheme.run(time -> {
            if (a.active() && b.active()) closest[0] = Math.min(closest[0], a.gap(b));
        });
        check(done, "both left");
        check(closest[0] > -.05, "soft contact stays small: " + closest[0]);
    }

    private static EventDrivenScheme eventDriven(Room room, List<Agent> agents, double dt2) {
        return new EventDrivenScheme(room, agents, new SocialForceModel(SocialForceModel.KT), .5, 1000, 1_000_000, dt2, 1);
    }

    private static void collisionTimeByHand() {
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .20, 80, 2, .3);
        i.vx = 1.5;
        near(EventCore.collisionTime(i, j), 1.110, 1e-3, "collision time");
        i.vx = -1.5;
        check(EventCore.collisionTime(i, j) == Double.POSITIVE_INFINITY, "moving apart never collide");
    }

    private static void collisionIsTheTP3One() {
        // Touching pairs (σ = .45, normal (.8, .6)) with arbitrary velocities, against the TP3 formula by hand:
        // k = Δv·Δr / |Δr|², vi += k Δr, vj -= k Δr. Momentum and energy kept, normal parts exchanged.
        double nx = .8, ny = .6, tx = -.6, ty = .8;
        for (double[] v : new double[][]{{1.5, .2, -.3, 0}, {1, -1, -.5, .7}, {.001, 0, 0, 0}, {0, 1.5, 0, -1.5}}) {
            Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .20, 80, .45 * nx, .45 * ny);
            i.vx = v[0];
            i.vy = v[1];
            j.vx = v[2];
            j.vy = v[3];
            double dx = j.x - i.x, dy = j.y - i.y, k = ((v[2] - v[0]) * dx + (v[3] - v[1]) * dy) / (dx * dx + dy * dy);
            double in = v[0] * nx + v[1] * ny, jn = v[2] * nx + v[3] * ny;
            double it = v[0] * tx + v[1] * ty, jt = v[2] * tx + v[3] * ty;
            double energy = v[0] * v[0] + v[1] * v[1] + v[2] * v[2] + v[3] * v[3];
            EventCore.elasticCollision(i, j);
            near(i.vx, v[0] + k * dx, 1e-12, "TP3 formula, vix");
            near(i.vy, v[1] + k * dy, 1e-12, "TP3 formula, viy");
            near(j.vx, v[2] - k * dx, 1e-12, "TP3 formula, vjx");
            near(j.vy, v[3] - k * dy, 1e-12, "TP3 formula, vjy");
            near(i.vx + j.vx, v[0] + v[2], 1e-12, "momentum x");
            near(i.vy + j.vy, v[1] + v[3], 1e-12, "momentum y");
            near(i.vx * i.vx + i.vy * i.vy + j.vx * j.vx + j.vy * j.vy, energy, 1e-12, "kinetic energy");
            near(i.vx * nx + i.vy * ny, jn, 1e-12, "normal parts exchanged (i)");
            near(j.vx * nx + j.vy * ny, in, 1e-12, "normal parts exchanged (j)");
            near(i.vx * tx + i.vy * ty, it, 1e-12, "tangential kept (i)");
            near(j.vx * tx + j.vy * ty, jt, 1e-12, "tangential kept (j)");
        }
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .25, 80, .5, 0);
        i.vx = -1;
        EventCore.elasticCollision(i, j);
        check(i.vx == -1 && j.vx == 0, "moving apart: nothing happens");
    }

    private static void wallIsAMirror() throws Exception {
        // Wall x = 0 at 1 m/s inwards (it arrives at t = 0.19 s): only the normal component flips.
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, .2 + .19, 30);
        var scheme = new EventDrivenScheme(room, List.of(agent), new SocialForceModel(0), CUTOFF, 1, 1000, 1e-3, 1);
        agent.vx = -1;
        agent.vy = .3;
        double[] after = new double[2];
        scheme.run(time -> {
            if (Math.abs(time - .195) < 1e-9) { after[0] = agent.vx; after[1] = agent.vy; }
        });
        near(after[0], 1, 1e-12, "wall: normal component flipped");
        near(after[1], .3, 1e-12, "wall: tangential component kept");
        // Door edge (x = 24.6, y = 0) hit head-on along the diagonal from inside the opening (at t ≈ 7 ms,
        // before the first phantom): the velocity comes back reversed, as off a fixed disc of radius 0.
        double edge = room.doorLeft(), r = .2, start = (r + .01) / Math.sqrt(2);
        Agent corner = new Agent(0, r, 80, edge + start, start);
        var cornerScheme = new EventDrivenScheme(room, List.of(corner), new SocialForceModel(0), CUTOFF, 1, 1000, 1e-3, 1);
        corner.vx = -1;
        corner.vy = -1;
        double[] back = new double[2];
        cornerScheme.run(time -> {
            if (Math.abs(time - .015) < 1e-9) { back[0] = corner.vx; back[1] = corner.vy; }
        });
        near(back[0], 1, 1e-9, "door edge: head-on, x reversed");
        near(back[1], 1, 1e-9, "door edge: head-on, y reversed");
    }

    private static void touchingAndApproachingCollideNow() {
        // Overlap by rounding (1e-12 m) and approaching at 1e-9 m/s: collide now, no error.
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .25, 80, .5 - 1e-12, 0);
        i.vx = 1e-9;
        check(EventCore.collisionTime(i, j) == 0, "touching and approaching: collision now");
        // A real overlap is still a bug.
        Agent k = new Agent(2, .25, 80, .4, 0);
        boolean thrown = false;
        try {
            EventCore.collisionTime(i, k);
        } catch (IllegalStateException expected) {
            thrown = true;
        }
        check(thrown, "a real overlap is reported");
    }

    private static void unequalMassesAreRejected() throws Exception {
        boolean rejected = false;
        try {
            new EventDrivenScheme(new Room(50, .8), List.of(new Agent(0, .2, 80, 25, 10), new Agent(1, .2, 60, 25, 20)),
                new SocialForceModel(0), CUTOFF, 10, 1000, 1e-3, 1).run(time -> { });
        } catch (IllegalArgumentException expected) {
            rejected = true;
        }
        check(rejected, "the TP3 collision needs equal masses");
    }

    private static void loneAgentInEventDriven() throws Exception {
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 25, 10);
        check(eventDriven(room, List.of(agent), 1).run(time -> { }), "lone agent leaves");
        near(agent.doorTime, 10 / 1.5, 1e-9, "exact door time");
        near(agent.retireTime, 11 / 1.5, 1e-9, "exact retire time");
        Agent diagonal = new Agent(0, .2, 80, 10, 20);
        eventDriven(room, List.of(diagonal), 1).run(time -> { });
        double path = Math.hypot(15, 19.8);
        check(diagonal.doorTime > path / 1.5 && diagonal.doorTime < path / 1.5 + .5, "off-axis door time in EDMD");
    }

    private static void collisionThenBounceBack() throws Exception {
        // One agent stopped in the way: they collide, relax with phantom events and both leave.
        Room room = new Room(50, .8);
        Agent a = new Agent(0, .25, 80, 25, 20), b = new Agent(1, .25, 80, 25, 15);
        var scheme = eventDriven(room, List.of(a, b), 1);
        b.vx = b.vy = 0;
        double[] speed = {0};
        check(scheme.run(time -> {
            if (a.active() && a.y < 5) speed[0] = Math.hypot(a.vx, a.vy);
        }), "both leave after colliding");
        near(speed[0], 1.5, 1e-9, "desired speed recovered, exactly");
        check(scheme.events() > 40, "collision, phantoms and crossings happened");
        check(b.retireTime > a.doorTime - 8, "the stopped agent was pushed and recovered");
    }

    private static void eventDrivenWritesEveryDt2() throws Exception {
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 25, 10);
        List<Double> frames = new ArrayList<>();
        eventDriven(room, List.of(agent), .5).run(frames::add);
        near(frames.get(3), 1.5, 1e-12, "frames at multiples of dt2");
        near(frames.get(1) - frames.get(0), .5, 1e-12, "first interval");
    }

    // ---- Hybrid [DESIGN.md 2.6, 2.7 and 6] ----

    private static final double CUTOFF = 2 * SocialForceModel.MAX_RADIUS,
        T_S = SocialForceModel.contactDuration(40);

    private static HybridScheme hybrid(Room room, List<Agent> agents, double flight, int dumpEvery) {
        return hybrid(room, agents, flight, dumpEvery, 1e5);
    }

    private static HybridScheme hybrid(Room room, List<Agent> agents, double flight, int dumpEvery, double maxTime) {
        return new HybridScheme(room, agents, new SocialForceModel(0), CUTOFF, maxTime, 2_000_000_000L,
            1e-3, dumpEvery, flight);
    }

    private static List<Agent> crowd(long seed) {
        return new Room(50, .8).placeAgents(200, SocialForceModel.MIN_RADIUS, SocialForceModel.MAX_RADIUS,
            SocialForceModel.MASS, new Random(seed));
    }

    /** Every agent's full state at each frame, to compare two runs number by number. */
    private static List<double[]> recorder(List<Double> times, List<Agent> agents, double time) {
        times.add(time);
        List<double[]> frame = new ArrayList<>();
        for (Agent a : agents) frame.add(new double[]{a.x, a.y, a.vx, a.vy, a.scheme.ordinal(), a.location.ordinal()});
        return frame;
    }

    private static void loneAgentInHybrid() throws Exception {
        // On the door axis 10 m away it never touches anything: EDMD all the way, with its exact times.
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 25, 10);
        boolean[] timeStep = {false};
        check(hybrid(room, List.of(agent), T_S, 1).run(time -> timeStep[0] |= agent.scheme == Agent.Scheme.TIME_STEP),
            "lone agent leaves in the hybrid");
        check(!timeStep[0], "without contacts nobody leaves EDMD");
        near(agent.doorTime, 10 / 1.5, 1e-9, "exact door time in the hybrid");
        near(agent.retireTime, 11 / 1.5, 1e-9, "exact retire time in the hybrid");
    }

    private static void shortFlightSwitchesToTimeStep() throws Exception {
        // b stopped 1 cm from the wall x = 0, a hits it: b bounces on the wall about 7 ms later, less than t_s.
        Room room = new Room(50, .8);
        Agent a = new Agent(0, .25, 80, 1.5, 30), b = new Agent(1, .25, 80, .26, 30);
        var scheme = hybrid(room, List.of(a, b), T_S, 1);
        a.vx = -1.5;
        a.vy = b.vx = b.vy = 0;
        double[] switched = {Double.NaN};
        check(scheme.run(time -> {
            if (Double.isNaN(switched[0]) && b.scheme == Agent.Scheme.TIME_STEP) switched[0] = time;
        }), "both leave after the double collision");
        double hit = (1.5 - .76) / 1.5;
        check(switched[0] > hit && switched[0] < hit + .02, "two collisions within t_s: TSDMD, at " + switched[0]);
    }

    private static void isolatedCollisionStaysEventDriven() throws Exception {
        // The scenario of collisionThenBounceBack, in the hybrid: one collision, nobody switches.
        Room room = new Room(50, .8);
        Agent a = new Agent(0, .25, 80, 25, 20), b = new Agent(1, .25, 80, 25, 15);
        var scheme = hybrid(room, List.of(a, b), T_S, 1);
        b.vx = b.vy = 0;
        boolean[] timeStep = {false};
        check(scheme.run(time -> timeStep[0] |= a.scheme == Agent.Scheme.TIME_STEP || b.scheme == Agent.Scheme.TIME_STEP),
            "both leave");
        check(!timeStep[0], "an isolated collision stays in EDMD");
        check(scheme.events() > 40, "the collision and the phantoms did happen");
    }

    private static void hybridAtZeroIsEventDriven() throws Exception {
        // t_s = 0: same events in the same order as pure EDMD, so the same numbers, bit by bit.
        Room room = new Room(50, .8);
        List<Agent> pure = crowd(1), mixed = crowd(1);
        List<Double> pureTimes = new ArrayList<>(), mixedTimes = new ArrayList<>();
        List<List<double[]>> pureFrames = new ArrayList<>(), mixedFrames = new ArrayList<>();
        var reference = new EventDrivenScheme(room, pure, new SocialForceModel(0), CUTOFF, 1e5, 2_000_000_000L,
            1e-3, 500);
        check(reference.run(time -> pureFrames.add(recorder(pureTimes, pure, time))), "pure EDMD, N = 200, finishes");
        check(hybrid(room, mixed, 0, 500).run(time -> mixedFrames.add(recorder(mixedTimes, mixed, time))),
            "hybrid at t_s = 0 finishes");
        sameRuns(pure, mixed, pureTimes, mixedTimes, pureFrames, mixedFrames, "EDMD");
        for (List<double[]> frame : mixedFrames) for (double[] row : frame) check(row[4] == 0, "nobody in TSDMD");
    }

    /** Exit records identical; frames identical at every common instant (the last one may differ in time). */
    private static void sameRuns(List<Agent> pure, List<Agent> mixed, List<Double> pureTimes, List<Double> mixedTimes,
                                 List<List<double[]>> pureFrames, List<List<double[]>> mixedFrames, String name) {
        boolean exits = true;
        for (int k = 0; k < pure.size(); k++)
            exits &= Double.compare(pure.get(k).doorTime, mixed.get(k).doorTime) == 0
                && Double.compare(pure.get(k).retireTime, mixed.get(k).retireTime) == 0;
        check(exits, "same exit times as pure " + name);
        int common = Math.min(pureTimes.size(), mixedTimes.size()) - 1;
        check(common > 50, "enough frames to compare with pure " + name);
        boolean frames = true;
        for (int f = 0; f < common; f++) {
            frames &= pureTimes.get(f).equals(mixedTimes.get(f));
            for (int k = 0; k < pure.size(); k++) {
                double[] p = pureFrames.get(f).get(k), m = mixedFrames.get(f).get(k);
                for (int c = 0; c < 4; c++) frames &= Double.compare(p[c], m[c]) == 0;
                frames &= p[5] == m[5];
            }
        }
        check(frames, "same states at every frame as pure " + name);
    }

    private static void contactsWithTimeStepAreCaught() throws Exception {
        // DESIGN.md 2.7, with a frame every step (written after the Step of that instant). An event-driven
        // agent may overlap a time-step one between two steps, but the step catches it: no such overlap
        // at any frame, and the ones that just switched overlap at most (|v_e| + |v_t|)·dt, under 1 cm.
        // The ones that just went back touch nobody. A jam is a valid result (TP5_Consideraciones iii),
        // so the run may stop at the 300 s cut-off.
        for (double flight : new double[]{T_S, 10 * T_S}) {
            List<Agent> agents = crowd(3);
            List<Agent> eventDriven = new ArrayList<>(), timeStep = new ArrayList<>();
            int[] previous = new int[agents.size()];
            double[] closest = {Double.POSITIVE_INFINITY}, entering = {Double.POSITIVE_INFINITY},
                leaving = {Double.POSITIVE_INFINITY};
            long[] mixedSteps = {0}, back = {0};
            var scheme = hybrid(new Room(50, .8), agents, flight, 1, 300);
            boolean done = scheme.run(time -> {
                eventDriven.clear();
                timeStep.clear();
                for (Agent a : agents) if (a.active()) (a.scheme == Agent.Scheme.TIME_STEP ? timeStep : eventDriven).add(a);
                for (Agent a : agents) {
                    if (!a.active()) continue;
                    int now = a.scheme.ordinal();
                    if (now != previous[a.id]) {
                        double gap = Double.POSITIVE_INFINITY;
                        for (Agent b : agents) if (b != a && b.active()) gap = Math.min(gap, a.gap(b));
                        if (now == 1) entering[0] = Math.min(entering[0], gap);
                        else {
                            leaving[0] = Math.min(leaving[0], gap);
                            back[0]++;
                        }
                    }
                    previous[a.id] = now;
                }
                if (!eventDriven.isEmpty() && !timeStep.isEmpty()) mixedSteps[0]++;
                for (Agent e : eventDriven)
                    for (Agent t : timeStep)
                        if (Math.abs(e.x - t.x) < 1 && Math.abs(e.y - t.y) < 1) closest[0] = Math.min(closest[0], e.gap(t));
            });
            String name = " (t_s = " + flight + ")";
            check(mixedSteps[0] > 1000, "both schemes coexisted" + name + ": " + mixedSteps[0] + " steps");
            check(closest[0] >= -1e-9, "no event-driven–time-step overlap at any step" + name + ": " + closest[0]);
            check(entering[0] > -.01, "contacts caught within one step" + name + ": overlap " + -entering[0]);
            check(back[0] > 0 && leaving[0] >= -1e-9, "back to EDMD touching nobody" + name + ": " + leaving[0]);
            System.out.printf("  hybrid t_s=%.4f: closest ED-TS gap %.4f m, overlap on switching %.4f m, %d mixed steps,"
                + " %d TS->ED, %d events, complete %b%n", flight, closest[0], Math.max(0, -entering[0]), mixedSteps[0],
                back[0], scheme.events(), done);
        }
    }

    // ---- Fixes of the engine review (2026-11-08) ----

    private static void collisionIsDampedOnlyAfterIt() throws Exception {
        // Phantom due at 0.2 s (relaxed at 0), wall hit at 0.19 s, velocity held in between (★10). At the
        // phantom, the velocity before the hit decays over 0.2 s and the rebound kick Δ only over 0.01 s:
        // v = vd ê + (v_held - Δ + Δ e^{0.19/τ} - vd ê) e^{-0.2/τ}. Before the fix Δ decayed over 0.2 s.
        Room room = new Room(50, .8);
        SocialForceModel model = new SocialForceModel(0);
        Agent agent = new Agent(0, .2, 80, .2 + .19, 30);
        var scheme = new EventDrivenScheme(room, List.of(agent), model, CUTOFF, 1, 1000, 1e-3, 1);
        agent.vx = -1;
        agent.vy = 0;
        agent.phantomTime = .2;
        double[] seen = new double[2];
        scheme.run(time -> {
            if (Math.abs(time - .205) < 1e-9) { seen[0] = agent.vx; seen[1] = agent.vy; }
        });
        Agent twin = new Agent(1, .2, 80, .2, 30);
        twin.vx = -1;
        double approach = -twin.vx, change = 2 * approach;
        twin.vx += change;
        twin.x += twin.vx * .01;
        twin.vx += change * (Math.exp(.19 / SocialForceModel.TAU) - 1);
        model.relax(twin, room, .2);
        near(seen[0], twin.vx, 1e-12, "rebound damped from the collision on (x)");
        near(seen[1], twin.vy, 1e-12, "rebound damped from the collision on (y)");
    }

    private static void slidingIntoTheOpeningTurnsAtOnce() throws Exception {
        // Free flight along y = r towards the opening: the target changes when it enters (t = 2.6/1.5 s),
        // not at whatever event happens to come next.
        Room room = new Room(50, .8);
        Agent agent = new Agent(0, .2, 80, 22, .2);
        var scheme = new EventDrivenScheme(room, List.of(agent), new SocialForceModel(0), CUTOFF, 100, 100_000,
            1e-3, 1);
        agent.vx = 1.5;
        agent.vy = 0;
        double[] turned = {Double.NaN};
        check(scheme.run(time -> {
            if (Double.isNaN(turned[0]) && agent.headingOut) turned[0] = time;
        }), "the sliding agent leaves");
        near(turned[0], 2.6 / 1.5, 1.5e-3, "heads out on entering the opening");
    }

    private static void agentOnACellBorderMovesOn() throws Exception {
        // L = 10 m, cells of 0.5 m: x = 5 (the door axis) is a border. With vx = -1e-17 the agent would
        // need 1e8 s to be 1e-9 m past it, so it is never refiled sideways; it walks down and leaves.
        // With the margin in time instead of distance it was refiled in its own cell every 1e-9 s.
        Room room = new Room(10, .8);
        Agent agent = new Agent(0, .2, 80, 5, 3);
        var scheme = new EventDrivenScheme(room, List.of(agent), new SocialForceModel(0), CUTOFF, 100, 10_000,
            1e-3, 1000);
        agent.vx = -1e-17;
        check(scheme.run(time -> { }), "an agent on a cell border with a negligible speed leaves");
        check(scheme.events() < 100, "without refiling it over and over: " + scheme.events() + " events");
    }

    private static void cutOffsEndCleanly() throws Exception {
        // maxEvents inside a hybrid step: complete = false, no crash. EDMD cut on a frame: no repeated frame.
        check(!new HybridScheme(new Room(50, .8), crowd(4), new SocialForceModel(0), CUTOFF, 1e5, 3000,
            1e-3, 500, T_S).run(time -> { }),
            "hybrid cut by max-events reports an incomplete run");
        Agent agent = new Agent(0, .2, 80, 25, 10);
        List<Double> frames = new ArrayList<>();
        check(!new EventDrivenScheme(new Room(50, .8), List.of(agent), new SocialForceModel(0), CUTOFF, 1, 1000,
            1e-3, 500).run(frames::add), "EDMD cut by max-time reports an incomplete run");
        boolean increasing = true;
        for (int k = 1; k < frames.size(); k++) increasing &= frames.get(k) > frames.get(k - 1);
        check(increasing, "frame times strictly increase: " + frames);
    }

    private static void invalidFlagsAreRejected() {
        // --e-pair and --v-sep (the collision is the TP3 one), --Rs and --v-max (only the time criterion is
        // left) no longer exist: rejected as unknown flags.
        for (String[] flags : new String[][]{{"--ts", "-1"}, {"--e-pair", "1.5"}, {"--max-events", "0"},
            {"--v-sep", "-1"}, {"--Rs", "5"}, {"--v-max", "5"}}) {
            boolean rejected = false;
            try {
                RunConfig.parse(flags);
            } catch (IllegalArgumentException expected) {
                rejected = true;
            }
            check(rejected, "rejects " + String.join(" ", flags));
        }
    }
}
