package secondTry.models.events;

import secondTry.models.Particle;

public class EventHorizontal extends Event {
    public EventHorizontal(double time, Particle particle) {
        super(time, particle);
    }

    @Override
    public boolean bounce() {
        return particle.bounceY();
    }
}
