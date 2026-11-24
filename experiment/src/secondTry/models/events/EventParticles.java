package secondTry.models.events;

import secondTry.models.Particle;

public class EventParticles extends Event{
    private final int collisionsOther;
    protected final Particle other;

    public EventParticles(double time, Particle particle, Particle other) {
        super(time, particle);
        this.other = other;
        collisionsOther = other.getCollisionCount();
    }

    @Override
    public boolean bounce(){
        double impulseOverMassTimesRadius = particle.impulseOverMassTimesRadius(other);
        particle.bounce(impulseOverMassTimesRadius, other);
        return other.bounce(impulseOverMassTimesRadius, particle);
    }

    @Override
    public Particle[] getParticles() {
        return new Particle[]{particle, other};
    }

    @Override
    public boolean wasInvalidated() {
        return super.wasInvalidated() || other.getCollisionCount() > collisionsOther;
    }
}
