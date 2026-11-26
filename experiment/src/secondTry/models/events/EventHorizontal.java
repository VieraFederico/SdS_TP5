package secondTry.models.events;

import secondTry.models.Particle;

public class EventHorizontal extends Event {
    private final double width, gate;
    public EventHorizontal(double time, Particle particle, double width, double gate) {
        super(time, particle);
        this.width = width;
        this.gate = gate;
    }

    @Override
    public boolean bounce() {
        return particle.bounceY(width,gate);
    }
}
