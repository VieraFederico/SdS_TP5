package secondTry.models.events;

import secondTry.models.Particle;

public abstract class Event implements Comparable<Event> {
    private final double time;
    private static long nextOrder;
    private final long order = nextOrder++;
    protected final Particle particle;
    protected final int collisions;

    public Event(double time, Particle particle) {
        this.particle = particle;
        this.time = time;
        collisions = particle.getCollisionCount();
    }

    public abstract boolean bounce();

    public double getTime() {
        return time;
    }

    public Particle[] getParticles() {
        return new Particle[]{particle};
    }

    /**
     * Checks if any of the particles has collided since the event was predicted
     * @return true if outdated, false otherwise
     */
    public boolean wasInvalidated() {
        return particle.getCollisionCount() > collisions;
    }

    @Override
    public int compareTo(Event o) {
        int byTime = Double.compare(this.time, o.getTime());
        return byTime != 0 ? byTime : Long.compare(order, o.order);
    }
}
