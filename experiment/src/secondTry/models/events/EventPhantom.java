package secondTry.models.events;

import secondTry.models.Particle;
import secondTry.models.Vector2D;

public class EventPhantom extends Event{
    private final double width;
    private final double gate;
    private final double desiredSpeed;
    private final double dtOverRelaxTime;
    public EventPhantom(double time, Particle particle, double width, double gate, double desiredSpeed, double dtOverRelaxTime) {
        super(time, particle);
        this.width = width;
        this.gate = gate;
        this.desiredSpeed = desiredSpeed;
        this.dtOverRelaxTime = dtOverRelaxTime;
    }

    @Override
    public boolean bounce() {
        Vector2D desiredVelocity = particle.getDesiredVelocity(width,gate, desiredSpeed);
        particle.setVelocity(desiredVelocity.subtract(particle.getVelocity().subtract(desiredVelocity).scale(Math.exp(-dtOverRelaxTime))));
        return false;
    }
}
