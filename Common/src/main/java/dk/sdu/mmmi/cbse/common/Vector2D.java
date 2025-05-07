package dk.sdu.mmmi.cbse.common;

import java.util.Objects;

/**
 * Immutable 2D vector class for geometry and physics calculations.
 */
public record Vector2D(float x, float y) {
    /**
     * Create a new vector with given x,y coordinates
     *
     * @param x X coordinate
     * @param y Y coordinate
     */
    public Vector2D {
    }

    /**
     * Get the X coordinate
     *
     * @return X value
     */
    @Override
    public float x() {
        return x;
    }

    /**
     * Get the Y coordinate
     *
     * @return Y value
     */
    @Override
    public float y() {
        return y;
    }

    /**
     * Add another vector to this one
     *
     * @param other Vector to add
     * @return A new vector representing the sum
     */
    public Vector2D add(Vector2D other) {
        Objects.requireNonNull(other, "Cannot add null vector");
        return new Vector2D(this.x + other.x, this.y + other.y);
    }

    /**
     * Subtract another vector from this one
     *
     * @param other Vector to subtract
     * @return A new vector representing the difference
     */
    public Vector2D subtract(Vector2D other) {
        Objects.requireNonNull(other, "Cannot subtract null vector");
        return new Vector2D(this.x - other.x, this.y - other.y);
    }

    /**
     * Scale this vector by a scalar value
     *
     * @param scalar Value to scale by
     * @return A new scaled vector
     */
    public Vector2D scale(float scalar) {
        return new Vector2D(this.x * scalar, this.y * scalar);
    }

    /**
     * Calculate the dot product with another vector
     *
     * @param other Vector to dot with
     * @return Dot product result
     */
    public float dot(Vector2D other) {
        Objects.requireNonNull(other, "Cannot calculate dot product with null vector");
        return this.x * other.x + this.y * other.y;
    }

    /**
     * Calculate the length of this vector
     *
     * @return Vector magnitude
     */
    public float magnitude() {
        return (float) Math.sqrt(magnitudeSquared());
    }

    /**
     * Calculate the squared magnitude (faster than magnitude)
     *
     * @return Vector magnitude squared
     */
    public float magnitudeSquared() {
        return x * x + y * y;
    }

    /**
     * Normalize this vector (make it length 1)
     *
     * @return A new normalized vector
     * @throws ArithmeticException if vector length is zero
     */
    public Vector2D normalize() {
        float mag = magnitude();
        if (mag < 0.000001f) {
            throw new ArithmeticException("Cannot normalize a zero-length vector");
        }
        return new Vector2D(x / mag, y / mag);
    }

    /**
     * Attempt to normalize, returning a zero vector if magnitude is zero
     *
     * @return Normalized vector, or zero vector if magnitude is zero
     */
    public Vector2D normalizeSafe() {
        float mag = magnitude();
        if (mag < 0.000001f) {
            return new Vector2D(0, 0);
        }
        return new Vector2D(x / mag, y / mag);
    }

    /**
     * Calculate distance to another vector
     *
     * @param other Vector to calculate distance to
     * @return Distance between the two vectors
     */
    public float distance(Vector2D other) {
        Objects.requireNonNull(other, "Cannot calculate distance to null vector");
        float dx = other.x - x;
        float dy = other.y - y;
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Calculate the angle of this vector (in radians)
     *
     * @return Angle in radians
     */
    public float angle() {
        return (float) Math.atan2(y, x);
    }

    /**
     * Create a new vector by rotating this one
     *
     * @param angleInRadians Angle to rotate by in radians
     * @return New rotated vector
     */
    public Vector2D rotate(float angleInRadians) {
        float cos = (float) Math.cos(angleInRadians);
        float sin = (float) Math.sin(angleInRadians);
        return new Vector2D(
                x * cos - y * sin,
                x * sin + y * cos
        );
    }

    /**
     * Create a vector from angle and magnitude
     *
     * @param angleInRadians Angle in radians
     * @param magnitude Length of the vector
     * @return New vector with specified angle and magnitude
     */
    public static Vector2D fromAngle(float angleInRadians, float magnitude) {
        return new Vector2D(
                (float) Math.cos(angleInRadians) * magnitude,
                (float) Math.sin(angleInRadians) * magnitude
        );
    }

    /**
     * Get a zero vector (0,0)
     *
     * @return Zero vector
     */
    public static Vector2D zero() {
        return new Vector2D(0, 0);
    }

    @Override
    public String toString() {
        return String.format("Vector2D[%.2f, %.2f]", x, y);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Vector2D other = (Vector2D) obj;
        return Float.compare(x, other.x) == 0 && Float.compare(y, other.y) == 0;
    }
}