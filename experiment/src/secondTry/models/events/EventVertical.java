package secondTry.models.events;

import secondTry.models.Particle;
import secondTry.models.events.Event;

public class EventVertical extends Event {
    private final double gate;
    private final double height;
    public EventVertical(double time, Particle particle, double height, double gate) {
        super(time, particle);
        this.height = height;
        this.gate = gate;
    }

    @Override
    public boolean bounce() {
        return particle.bounceX(height, gate);
    }
}
