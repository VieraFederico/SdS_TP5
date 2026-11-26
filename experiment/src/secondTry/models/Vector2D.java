package secondTry.models;

public record Vector2D(double x, double y) {
    public static final Vector2D ZERO = new Vector2D(0.0, 0.0);
    public static final Vector2D ONE = new Vector2D(1, 1);

    public Vector2D add(Vector2D other) {
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    public Vector2D subtract(Vector2D other) {
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    public Vector2D scalarProduct(Vector2D other) {
        return new Vector2D(this.x * other.x, this.y * other.y);
    }

    public double dotProduct(Vector2D other) {
        return this.x * other.x + this.y * other.y;
    }

    public Vector2D scale(double scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    public Vector2D pointer(Vector2D other) {
        Vector2D delta = subtract(other);
        double mod = Math.sqrt(delta.dotProduct(delta));
        return delta.scale(1/mod);
    }
}
