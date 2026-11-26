package secondTry.models.events;

import secondTry.models.Particle;

public class EventVertical extends Event {
    public EventVertical(double time, Particle particle) {
        super(time, particle);
    }

    @Override
    public boolean bounce() {
        return particle.bounceX();
    }
}
