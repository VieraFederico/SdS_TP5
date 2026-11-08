package models;

/** The physics, apart from how time advances. Forces accumulate on Agent.fx and Agent.fy. */
public interface PedestrianModel {
    void addDrivingForce(Agent agent, Room room);

    /** Adds the contact force to both agents if they overlap. */
    void addContactForce(Agent i, Agent j);

    /** Contact with a fixed point of a wall or a door edge. */
    void addWallForce(Agent agent, double x, double y);

    /** Sets the velocity at which the agent feels no force. */
    void setFreeFlight(Agent agent, Room room);

    /** Applies the driving force alone over `elapsed`, exactly. */
    void relax(Agent agent, Room room, double elapsed);

    /** True if relaxing over `elapsed` would leave the velocity practically at the desired one. */
    boolean nearlyRecovered(Agent agent, Room room, double elapsed);

    /** Time to travel one radius: the interval between phantom events. */
    double characteristicTime(Agent agent);
}
