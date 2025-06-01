package dk.sdu.mmmi.cbse.common.components;

import dk.sdu.mmmi.cbse.common.Vector2D;

/**
 * Component that handles entity position, rotation, and shape described by Vector2D.
 * Uses mathematical coordinate system where:
 * - 0° rotation points to the right (+X direction)
 * - 90° rotation points up (+Y direction)
 * - 180° rotation points left (-X direction)
 * - 270° rotation points down (-Y direction)
 */
public class TransformComponent implements IComponent {
    private Vector2D position;
    private Vector2D scale;
    private float rotation;
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
     * Create a transform with specified position
     * @param x Initial X position
     * @param y Initial Y position
     */
    public TransformComponent(float x, float y) {
        this.position = new Vector2D(x, y);
        this.scale = new Vector2D(1, 1);
        this.rotation = 0;
        this.radius = 1;
        this.updateDirectionVectors();
    }

    /**
     * Updates direction vectors based on current rotation.
     * Uses mathematical coordinate system where +Y is up.
     * Forward points in the direction of rotation.
     * Right points 90 degrees clockwise from forward.
     */
    private void updateDirectionVectors() {
        float radians = (float) Math.toRadians(rotation);

        // Forward vector points in the direction of rotation
        // 0° = (1,0), 90° = (0,1), 180° = (-1,0), 270° = (0,-1)
        forward = new Vector2D((float) Math.cos(radians), (float) Math.sin(radians));

        // Right vector is 90° clockwise from forward
        // If forward is (cos θ, sin θ), then right is (sin θ, -cos θ)
        // 0° forward (1,0) -> right (0,-1)
        // 90° forward (0,1) -> right (1,0)
        right = new Vector2D((float) Math.sin(radians), (float) -Math.cos(radians));
    }

    // === Position Methods ===

    /**
     * Get the position vector
     * @return Current position
     */
    public Vector2D getPosition() {
        return position;
    }

    /**
     * Set the position vector
     * @param position New position
     */
    public void setPosition(Vector2D position) {
        this.position = position != null ? position : new Vector2D(0, 0);
    }

    /**
     * Get the X coordinate
     * @return X position
     */
    public float getX() {
        return position.x();
    }

    /**
     * Set the X coordinate
     * @param x New X position
     */
    public void setX(double x) {
        this.position = new Vector2D((float) x, position.y());
    }

    /**
     * Get the Y coordinate
     * @return Y position
     */
    public float getY() {
        return position.y();
    }

    /**
     * Set the Y coordinate
     * @param y New Y position
     */
    public void setY(double y) {
        this.position = new Vector2D(position.x(), (float) y);
    }

    /**
     * Translate the position by a vector
     * @param translation Vector to add to current position
     */
    public void translate(Vector2D translation) {
        if (translation != null) {
            this.position = position.add(translation);
        }
    }

    /**
     * Translate the position by x,y amounts
     * @param deltaX Amount to move in X direction
     * @param deltaY Amount to move in Y direction
     */
    public void translate(float deltaX, float deltaY) {
        this.position = position.add(new Vector2D(deltaX, deltaY));
    }

    // === Scale Methods ===

    /**
     * Get the scale vector
     * @return Current scale
     */
    public Vector2D getScale() {
        return scale;
    }

    /**
     * Set the scale vector
     * @param scale New scale
     */
    public void setScale(Vector2D scale) {
        this.scale = scale != null ? scale : new Vector2D(1, 1);
    }

    /**
     * Set uniform scale
     * @param uniformScale Scale factor for both X and Y
     */
    public void setScale(float uniformScale) {
        this.scale = new Vector2D(uniformScale, uniformScale);
    }

    /**
     * Get the rotation in degrees
     * @return Current rotation in degrees
     */
    public float getRotation() {
        return rotation;
    }

    /**
     * Set the rotation in degrees
     * @param rotation New rotation in degrees
     */
    public void setRotation(double rotation) {
        this.rotation = normalizeAngle((float) rotation);
        updateDirectionVectors();
    }

    /**
     * Add to the current rotation
     * @param degrees Degrees to add to current rotation
     */
    public void rotate(float degrees) {
        this.rotation = normalizeAngle(this.rotation + degrees);
        updateDirectionVectors();
    }

    /**
     * Normalize angle to 0-360 range
     * @param angle Angle in degrees
     * @return Normalized angle
     */
    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle < 0) {
            angle += 360;
        }
        return angle;
    }

    /**
     * Get the forward direction vector
     * @return Unit vector pointing in the direction of rotation
     */
    public Vector2D getForward() {
        return forward;
    }

    /**
     * Get the right direction vector
     * @return Unit vector pointing 90° clockwise from forward
     */
    public Vector2D getRight() {
        return right;
    }

    /**
     * Get the up direction vector (opposite of right)
     * @return Unit vector pointing 90° counter-clockwise from forward
     */
    public Vector2D getUp() {
        return new Vector2D(-right.y(), right.x());
    }

    /**
     * Get the backward direction vector (opposite of forward)
     * @return Unit vector pointing opposite to forward
     */
    public Vector2D getBackward() {
        return new Vector2D(-forward.x(), -forward.y());
    }

    /**
     * Move forward based on current rotation
     * @param amount Distance to move forward
     */
    public void moveForward(float amount) {
        position = position.add(forward.scale(amount));
    }

    /**
     * Move backward based on current rotation
     * @param amount Distance to move backward
     */
    public void moveBackward(float amount) {
        position = position.add(forward.scale(-amount));
    }

    /**
     * Move right based on current rotation (90 degrees clockwise from forward)
     * @param amount Distance to move right
     */
    public void moveRight(float amount) {
        position = position.add(right.scale(amount));
    }

    /**
     * Move left based on current rotation (90 degrees counter-clockwise from forward)
     * @param amount Distance to move left
     */
    public void moveLeft(float amount) {
        position = position.add(right.scale(-amount));
    }

    /**
     * Move in a specific direction
     * @param direction Direction vector (will be normalized)
     * @param amount Distance to move
     */
    public void moveInDirection(Vector2D direction, float amount) {
        if (direction != null && direction.magnitude() > 0.001f) {
            Vector2D normalizedDirection = direction.normalize();
            position = position.add(normalizedDirection.scale(amount));
        }
    }

    /**
     * Get the collision radius
     * @return Collision radius
     */
    public float getRadius() {
        return radius;
    }

    /**
     * Set the collision radius
     * @param radius New collision radius
     */
    public void setRadius(float radius) {
        this.radius = Math.max(0, radius);
    }

    /**
     * Get polygon coordinates for complex shapes
     * @return Array of polygon coordinates as [x1,y1,x2,y2,...]
     */
    public double[] getPolygonCoordinates() {
        return polygonCoordinates;
    }

    /**
     * Set polygon coordinates for complex shapes
     * @param coordinates Array of coordinates as [x1,y1,x2,y2,...]
     */
    public void setPolygonCoordinates(double... coordinates) {
        this.polygonCoordinates = coordinates;
    }

    /**
     * Calculate distance to another transform
     * @param other Other transform
     * @return Distance between the two transforms
     */
    public float distanceTo(TransformComponent other) {
        if (other == null) {
            return Float.MAX_VALUE;
        }
        return position.distance(other.position);
    }

    /**
     * Calculate angle to another transform
     * @param other Other transform
     * @return Angle in degrees from this transform to the other
     */
    public float angleTo(TransformComponent other) {
        if (other == null) {
            return 0;
        }
        Vector2D direction = other.position.subtract(position);
        return (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
    }

    // === COULD HAVE BEEN USEFUL ===

    /**
     * Get direction vector to another transform
     * @param other Other transform
     * @return Normalized direction vector, or zero vector if other is null or at same position
     */
    public Vector2D directionTo(TransformComponent other) {
        if (other == null) {
            return Vector2D.zero();
        }
        Vector2D direction = other.position.subtract(position);
        return direction.magnitude() > 0.001f ? direction.normalize() : Vector2D.zero();
    }

    /**
     * Look at another transform (set rotation to face the target)
     * @param target Target transform to look at
     */
    public void lookAt(TransformComponent target) {
        if (target != null) {
            float angle = angleTo(target);
            setRotation(angle);
        }
    }

    /**
     * Look at a specific position (set rotation to face the position)
     * @param targetPosition Target position to look at
     */
    public void lookAt(Vector2D targetPosition) {
        if (targetPosition != null) {
            Vector2D direction = targetPosition.subtract(position);
            float angle = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
            setRotation(angle);
        }
    }

    @Override
    public String toString() {
        return String.format("Transform[pos=%.2f,%.2f rot=%.1f° scale=%.2f,%.2f radius=%.2f]",
                position.x(), position.y(), rotation, scale.x(), scale.y(), radius);
    }
}