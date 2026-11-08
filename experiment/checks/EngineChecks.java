import models.Agent;
import models.CellGrid;
import models.PedestrianModel;
import models.Room;
import models.SocialForceModel;
import schemes.EventCore;
import schemes.EventDrivenScheme;
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
        impulses();
        restitutionFromTheModel();
        loneAgentInEventDriven();
        collisionThenBounceBack();
        eventDrivenWritesEveryDt2();
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
        return new EventDrivenScheme(room, agents, new SocialForceModel(SocialForceModel.KT), .5, 1000, 1_000_000, .94, .92, .02, dt2);
    }

    private static void collisionTimeByHand() {
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .20, 80, 2, .3);
        i.vx = 1.5;
        near(EventCore.collisionTime(i, j), 1.110, 1e-3, "collision time");
        i.vx = -1.5;
        check(EventCore.collisionTime(i, j) == Double.POSITIVE_INFINITY, "moving apart never collide");
    }

    private static void impulses() {
        for (double e : new double[]{0, .5, .94, 1}) {
            Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .20, 60, .45 * .8, .45 * .6);
            i.vx = 1.5;
            i.vy = .2;
            j.vx = -.3;
            double px = 80 * i.vx + 60 * j.vx, py = 80 * i.vy + 60 * j.vy;
            double approach = (i.vx - j.vx) * .8 + (i.vy - j.vy) * .6;
            EventCore.applyImpulse(i, j, e, 0);
            near(80 * i.vx + 60 * j.vx, px, 1e-9, "momentum x");
            near(80 * i.vy + 60 * j.vy, py, 1e-9, "momentum y");
            near((j.vx - i.vx) * .8 + (j.vy - i.vy) * .6, e * approach, 1e-9, "normal relative velocity");
        }
        Agent i = new Agent(0, .25, 80, 0, 0), j = new Agent(1, .25, 80, .5, 0);
        i.vx = .001;
        EventCore.applyImpulse(i, j, 0, .02);
        near(j.vx - i.vx, .02, 1e-12, "minimum separation speed");
    }

    private static void restitutionFromTheModel() {
        near(SocialForceModel.restitution(40), .944, 1e-3, "pair restitution");
        near(SocialForceModel.restitution(80), .922, 1e-3, "wall restitution");
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
}
