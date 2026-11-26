package secondTry.models;

public class Particle{
    private Vector2D position;
    private final double r;
    private Vector2D velocity;
    private int collisionCount;
    private double lastTime = 0;
    private final double mass;
    private Vector2D acceleration = Vector2D.ZERO;
    private Vector2D previousAcceleration = Vector2D.ZERO;
    private Vector2D netForce = Vector2D.ZERO;

    public Particle(Vector2D position, double r, Vector2D velocity, double mass){
        this.position = position;
        this.r = r;
        this.mass = mass;
        this.velocity = velocity;
        collisionCount = 0;
    }

    public Vector2D getPosition() {
        return position;
    }
    public double getR() {
        return r;
    }
    public void setPosition(Vector2D position) {
        this.position = position;
    }

    public Vector2D getVelocity(){
        return velocity;
    }
    public void setVelocity(Vector2D velocity){
        this.velocity = velocity;
    }
    public Vector2D getAcceleration(){
        return acceleration;
    }
    public void setAcceleration(Vector2D acceleration){
        this.previousAcceleration = this.acceleration;
        this.acceleration = acceleration;
    }
    public Vector2D getPreviousAcceleration(){
        return previousAcceleration;
    }
    public void clearForce() {
        this.netForce = Vector2D.ZERO;
    }

    public void addForce(Vector2D force) {
        this.netForce = this.netForce.add(force);
    }
    public Vector2D getNetForce() {
        return netForce;
    }

    public void predictState(double dt) {
        Vector2D accelTerm = acceleration.scale(2.0 / 3.0)
                .subtract(previousAcceleration.scale(1.0 / 6.0));
        this.position = position.add(velocity.scale(dt)).add(accelTerm.scale(dt * dt));

        Vector2D velTerm = acceleration.scale(3.0 / 2.0)
                .subtract(previousAcceleration.scale(1.0 / 2.0));
        this.velocity = velocity.add(velTerm.scale(dt));
    }

    public void correctVelocity(Vector2D originalVelocity, double dt) {
        Vector2D nextAcceleration = netForce;

        Vector2D accelTerm = nextAcceleration.scale(1.0 / 3.0)
                .add(acceleration.scale(5.0 / 6.0))
                .subtract(previousAcceleration.scale(1.0 / 6.0));

        this.velocity = originalVelocity.add(accelTerm.scale(dt));
        this.previousAcceleration = this.acceleration;
        this.acceleration = nextAcceleration;
    }

    public void update(double time) {
        double dt = time - lastTime;
        if (!Double.isFinite(time) || dt < 0) throw new IllegalArgumentException("Time moved backwards");
        setPosition(position.add(velocity.scale(dt)));
        lastTime = time;
    }

    public int getCollisionCount() {
        return collisionCount;
    }

    public double collidesX(double width){
        double vx = velocity.x();
        double x = position.x();
        if(vx == 0)
            return Double.POSITIVE_INFINITY;
        if(vx<0)
            return (getR() - x)/vx;
        return (width - getR() - x)/vx;
    }

    public double collidesY(double height){
        double vy = velocity.y();
        double y = position.y();
        if(vy == 0)
            return Double.POSITIVE_INFINITY;
        if(vy<0)
            return (getR() - y)/vy;
        return (height - getR() - y)/vy;
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

    public boolean bounceX(){
        setVelocity(velocity.scalarProduct(new Vector2D(-1,1)));
        collisionCount++;
        return false;
    }

    public boolean bounceY(){
        setVelocity(velocity.scalarProduct(new Vector2D(1,-1)));
        collisionCount++;
        return false;
    }

    public boolean bounce(double impulseOverMassTimesRadius, Particle other){
        setVelocity(velocity.add(other.getPosition().subtract(position).scale(impulseOverMassTimesRadius)));
        collisionCount++;
        return false;
    }

    protected double getDeltaVelocityDotPosition(Particle other){
        return other.getPosition().subtract(position).dotProduct(other.getVelocity().subtract(velocity));
    }

    protected double getDeltaVelocityDotVelocity(Particle other){
        return other.getVelocity().subtract(velocity).dotProduct(other.getVelocity().subtract(velocity));
    }

    protected double getDeltaPositionDotPosition(Particle other){
        return other.getPosition().subtract(position).dotProduct(other.getPosition().subtract(position));
    }

    public double impulseOverMassTimesRadius(Particle other){
        return getDeltaVelocityDotPosition(other)/getDeltaPositionDotPosition(other);
    }

    public boolean overlaps(Particle other){
        return getDeltaPositionDotPosition(other)<Math.pow(getR()+other.getR(),2);
    }

    public boolean overlaps(double x, double y, double r){
        return Math.pow(getPosition().x()-x,2)+Math.pow(getPosition().y()-y,2)<Math.pow(getR()+r,2);
    }
}
