package secondTry.models.events;

import secondTry.models.Particle;
import secondTry.models.events.Event;

public class EventHorizontal extends Event {
    public EventHorizontal(double time, Particle particle) {
        super(time, particle);
    }

    @Override
    public boolean bounce() {
        return particle.bounceY();
    }
}
