package secondTry.models.events;

import secondTry.models.Particle;

public class EventLeave extends Event {
    public EventLeave(double time, Particle particle) {
        super(time, particle);
    }

    @Override
    public boolean bounce() {
        return false;
    }
}
