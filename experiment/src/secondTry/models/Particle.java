package secondTry.models;

public class Particle{
    private double x;
    private double y;
    private final double r;
    private double vx;
    private double vy;
    private int collisionCount;
    private boolean scored;
    private double lastTime = 0;

    public Particle(double x, double y, double r, double alpha, double speed){
        if (!Double.isFinite(x) || !Double.isFinite(y) || !Double.isFinite(r) || r <= 0 ||
                !Double.isFinite(alpha) || !Double.isFinite(speed) || speed < 0)
            throw new IllegalArgumentException("Invalid circle geometry");
        this.x = x;
        this.y = y;
        this.r = r;
        this.vx = speed * Math.cos(alpha);
        this.vy = speed * Math.sin(alpha);
        collisionCount = 0;
        scored = false;
    }

    public double getX() {
        return x;
    }
    public double getY() {
        return y;
    }
    public double getR() {
        return r;
    }
    protected void setX(double x) {
        this.x = x;
    }
    protected void setY(double y) {
        this.y = y;
    }

    public double getVx() {
        return vx;
    }

    public double getVy() {
        return vy;
    }

    public void update(double time) {
        double dt = time - lastTime;
        if (!Double.isFinite(time) || dt < 0) throw new IllegalArgumentException("Time moved backwards");
        setX(getX() + vx * dt);
        setY(getY() + vy * dt);
        lastTime = time;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public boolean hasScored(){
        return scored;
    }

    public double collidesX(double width){
        if(vx == 0)
            return Double.POSITIVE_INFINITY;
        if(vx<0)
            return (getR() - getX())/vx;
        return (width - getR() - getX())/vx;
    }

    public double collidesY(double height){
        if(vy == 0)
            return Double.POSITIVE_INFINITY;
        if(vy<0)
            return (getR() - getY())/vy;
        return (height - getR() -getY())/vy;
    }

    public double collides(Particle other) {
        if (other == this) return Double.POSITIVE_INFINITY;
        double deltaVR = getDeltaVelocityDotPosition(other);
        if (deltaVR >= 0)
            return Double.POSITIVE_INFINITY;
        double deltaRR = getDeltaPositionDotPosition(other);
        double deltaVV = getDeltaVelocityDotVelocity(other);
        double d = Math.pow(deltaVR, 2) - deltaVV * (deltaRR - Math.pow(getR() + other.getR(), 2));
        if (d < 0)
            return Double.POSITIVE_INFINITY;
        double dt = -(deltaVR + Math.sqrt(d)) / deltaVV;
        if (dt < -1e-10) throw new IllegalStateException("Overlapping approaching particles");
        return Math.max(0, dt);
    }

    public boolean bounceX(double height, double gate){
        vx *= -1;
        collisionCount++;
        if(!scored){
            scored = Math.abs(getY() - height/2) <= gate/2;
            return scored;
        }
        return false;
    }

    public boolean bounceY(){
        vy *= -1;
        collisionCount++;
        return false;
    }

    public boolean bounce(double impulseOverMassTimesRadius, Particle other){
        vx += impulseOverMassTimesRadius*(other.getX()-getX());
        vy += impulseOverMassTimesRadius*(other.getY()-getY());
        collisionCount++;
        return false;
    }

    protected double getDeltaVelocityDotPosition(Particle other){
        return (other.getX() - getX()) * (other.getVx() - getVx())
                + (other.getY() - getY()) * (other.getVy() - getVy());
    }

    protected double getDeltaVelocityDotVelocity(Particle other){
        return Math.pow(getVx() - other.getVx(), 2) + Math.pow(getVy() - other.getVy(), 2);
    }

    protected double getDeltaPositionDotPosition(Particle other){
        return Math.pow(other.getX() - getX(), 2) + Math.pow(other.getY() - getY(), 2);
    }

    public double impulseOverMassTimesRadius(Particle other){
        return getDeltaVelocityDotPosition(other)/getDeltaPositionDotPosition(other);
    }

    public boolean overlaps(Particle other){
        return Math.pow(getX()-other.getX(),2)+Math.pow(getY()-other.getY(),2)<Math.pow(getR()+other.getR(),2);
    }

    public boolean overlaps(double x, double y, double r){
        return Math.pow(getX()-x,2)+Math.pow(getY()-y,2)<Math.pow(getR()+r,2);
    }
}
