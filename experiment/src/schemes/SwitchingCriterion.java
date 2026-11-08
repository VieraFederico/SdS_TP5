package schemes;

import models.Agent;
import models.Room;
import java.util.function.DoubleSupplier;

/**
 * When an agent of the hybrid changes scheme (decisions ★21 and ★21b of DECISIONS.md, DESIGN.md 2.6).
 * To time-step if any of E1 (zone), E2 (skin) or E3 (short flight); back to event-driven only if
 * S1, S2 and S3 all hold. The scheme evaluates them; this says what the thresholds are.
 */
public interface SwitchingCriterion {
    /** E1: time until the event-driven agent enters the zone, 0 if it is inside, infinite if never. */
    double zoneEntryTime(Agent agent, Room room);

    /** E2: an event-driven agent this close to a time-step one becomes time-step. */
    double skinIn();

    /** E3: true if the flight between two collisions of an agent is too short for EDMD. */
    boolean flightTooShort(double flight);

    /** S2 looks this far: the cell grid must see every agent within it. */
    double skinOut();

    /** S1, S2 and S3, cheapest first. `deviation`: |v - vd ê|; `gap`: smallest edge separation to any agent. */
    boolean toEventDriven(Agent agent, Room room, DoubleSupplier deviation, DoubleSupplier gap);
}
