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
        Vector2D position = particle.getPosition();
        Vector2D desiredVelocity;
        double r = particle.getR();
        if(position.y()<r && position.x() < width/2+gate/2 -r && position.x() > width/2-gate/2+r)
            desiredVelocity = new Vector2D(0,-desiredSpeed);
        else
            desiredVelocity = position.pointer(new Vector2D(width/2, 0)).scale(desiredSpeed);
        particle.setVelocity(desiredVelocity.subtract(particle.getVelocity().subtract(desiredVelocity).scale(Math.exp(-dtOverRelaxTime))));
    }
}
