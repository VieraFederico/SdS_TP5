package secondTry.models.events;

import secondTry.models.Particle;

public class EventGate extends Event {
    private final double desiredSpeed;
    public EventGate(double time, Particle particle, double desiredSpeed) {
        super(time, particle);
        this.desiredSpeed = desiredSpeed;
    }

    @Override
    public boolean bounce() {
        return particle.exit(desiredSpeed);
    }
}
