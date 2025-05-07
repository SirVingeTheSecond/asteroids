package dk.sdu.mmmi.cbse.common.components;

import dk.sdu.mmmi.cbse.common.Vector2D;

/**
 * Component that handles entity position, rotation, and shape.
 * Uses Vector2D for positions and implements Unity-like transform behavior.
 */
public class TransformComponent implements IComponent {
    private Vector2D position;
    private Vector2D scale;
    private float rotation; // in degrees
    private float radius;
    private double[] polygonCoordinates;
    private Vector2D forward; // Calculated based on rotation
    private Vector2D right; // Perpendicular to forward

    /**
     * Create a new transform at position (0,0) with scale (1,1)
     */
    public TransformComponent() {
        this.position = new Vector2D(0, 0);
        this.scale = new Vector2D(1, 1);
        this.rotation = 0;
        this.radius = 1;
        this.updateDirectionVectors();
    }

    /**
     * Updates cached direction vectors based on current rotation
     */
    private void updateDirectionVectors() {
        float radians = (float) Math.toRadians(rotation);
        forward = new Vector2D((float) Math.cos(radians), (float) Math.sin(radians));
        right = new Vector2D((float) -Math.sin(radians), (float) Math.cos(radians));
    }

    // Position getters and setters
    public Vector2D getPosition() {
        return position;
    }

    public void setPosition(Vector2D position) {
        this.position = position;
    }

    // For backward compatibility
    public float getX() {
        return position.x();
    }

    public void setX(double x) {
        this.position = new Vector2D((float) x, position.y());
    }

    public float getY() {
        return position.y();
    }

    public void setY(double y) {
        this.position = new Vector2D(position.x(), (float) y);
    }

    // Scale getters and setters
    public Vector2D getScale() {
        return scale;
    }

    public void setScale(Vector2D scale) {
        this.scale = scale;
    }

    // Rotation getter and setter
    public float getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = (float) rotation;
        updateDirectionVectors();
    }

    // Direction vectors
    public Vector2D getForward() {
        return forward;
    }

    public Vector2D getRight() {
        return right;
    }

    // Movement helpers - similar to Unity's Transform methods
    public void translate(Vector2D translation) {
        this.position = position.add(translation);
    }

    public void rotate(float degrees) {
        this.rotation = (this.rotation + degrees) % 360;
        updateDirectionVectors();
    }

    // Radius getter and setter for collision detection
    public float getRadius() {
        return radius;
    }

    public void setRadius(float radius) {
        this.radius = radius;
    }

    // Shape data getter and setter
    public double[] getPolygonCoordinates() {
        return polygonCoordinates;
    }

    public void setPolygonCoordinates(double... coordinates) {
        this.polygonCoordinates = coordinates;
    }

    /**
     * Move forward based on current rotation
     * @param amount Amount to move
     */
    public void moveForward(float amount) {
        position = position.add(forward.scale(amount));
    }

    /**
     * Move right based on current rotation
     * @param amount Amount to move
     */
    public void moveRight(float amount) {
        position = position.add(right.scale(amount));
    }
}