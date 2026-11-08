import models.Agent;
import models.Room;
import models.SocialForceModel;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;

/**
 * static.txt and dynamic.txt follow Teorica_1, diapos 36-37, and dynamic.txt carries the whole state
 * of every agent at every dt2 [Correcciones_TP4, Código]. exits.txt is the record of the dynamics with
 * maximum precision [Teorica_5, diapo 21]. No observable is computed here: N(t), the evacuation time
 * and the fraction per scheme come from these files in post-processing [Teorica_0, diapo 20].
 */
public final class OutputWriter implements AutoCloseable {
    private static final double DECIMALS = 1e6;
    private final Path directory;
    private final BufferedWriter dynamic;
    private final StringBuilder line = new StringBuilder(128);

    public OutputWriter(Path directory, Room room, List<Agent> agents) throws IOException {
        this.directory = directory;
        StringBuilder text = new StringBuilder();
        text.append(agents.size()).append('\n').append(room.side).append('\n').append(room.door).append('\n');
        for (Agent agent : agents)
            text.append(agent.radius).append(' ').append(agent.scheme.ordinal()).append('\n');
        Files.writeString(directory.resolve("static.txt"), text.toString(), StandardOpenOption.CREATE_NEW);
        dynamic = Files.newBufferedWriter(directory.resolve("dynamic.txt"), StandardOpenOption.CREATE_NEW);
    }

    public void frame(double time, List<Agent> agents) throws IOException {
        dynamic.write(time + "\n");
        for (Agent agent : agents) {
            line.setLength(0);
            line.append(agent.id).append(' ');
            fixed(agent.x);
            fixed(agent.y);
            fixed(agent.vx);
            fixed(agent.vy);
            line.append(agent.scheme.ordinal()).append(' ').append(agent.location.ordinal()).append('\n');
            dynamic.append(line);
        }
    }

    /** Six decimals without String.format, which would dominate the writing cost. */
    private void fixed(double value) {
        long scaled = Math.round(Math.abs(value) * DECIMALS);
        if (value < 0 && scaled != 0) line.append('-');
        long whole = scaled / (long) DECIMALS, fraction = scaled % (long) DECIMALS;
        line.append(whole).append('.');
        String digits = Long.toString(fraction);
        line.repeat("0", 6 - digits.length()).append(digits).append(' ');
    }

    public void exits(List<Agent> agents) throws IOException {
        StringBuilder text = new StringBuilder();
        for (Agent agent : agents)
            text.append(agent.id).append(' ').append(agent.doorTime).append(' ').append(agent.retireTime).append('\n');
        Files.writeString(directory.resolve("exits.txt"), text.toString(), StandardOpenOption.CREATE_NEW);
    }

    public static void parameters(Path directory, RunConfig c, long seed, boolean complete, long steps, long events,
                                  long elapsed) throws IOException {
        String text = "scheme " + c.scheme() + "\nN " + c.n() + "\nL " + c.side() + "\ndoor " + c.door()
            + "\ndt " + c.dt() + "\ndt2 " + c.dt() * c.dumpEvery() + "\ndump_every " + c.dumpEvery()
            + "\nmax_time " + c.maxTime() + "\nseed " + seed + "\nm " + SocialForceModel.MASS
            + "\ntau " + SocialForceModel.TAU + "\nkn " + SocialForceModel.KN + "\nkt " + c.kt()
            + "\nvd " + SocialForceModel.DESIRED_SPEED + "\nr_min " + SocialForceModel.MIN_RADIUS
            + "\nr_max " + SocialForceModel.MAX_RADIUS + "\ncomplete " + complete + "\nsteps " + steps
            + "\ne_pair " + c.pairRestitution() + "\ne_wall " + c.wallRestitution() + "\nv_sep " + c.separationSpeed()
            + "\nmax_events " + c.maxEvents() + "\nevents " + events + "\nelapsed_ns " + elapsed + "\n";
        Files.writeString(directory.resolve("params.txt"), text, StandardOpenOption.CREATE_NEW);
    }

    @Override
    public void close() throws IOException {
        dynamic.close();
    }
}
