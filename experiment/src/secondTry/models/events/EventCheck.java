package secondTry.models.events;

import secondTry.models.Particle;

public class EventCheck extends Event{
    public EventCheck(double time, Particle particle) {
        super(time, particle);
    }

    @Override
    public boolean bounce() {
        return false;
    }
}
