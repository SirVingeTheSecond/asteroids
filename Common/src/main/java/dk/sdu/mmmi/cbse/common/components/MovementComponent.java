package dk.sdu.mmmi.cbse.common.components;

/**
 * Component for entity movement properties.
 */
public class MovementComponent implements IComponent {
    private MovementPattern pattern = MovementPattern.LINEAR;
    private float speed = 1.0f;
    private float rotationSpeed = 0.0f;
    private long lastDirectionChange = 0;
    private boolean accelerating = false;

    /**
     * Types of movement patterns for entities
     */
    public enum MovementPattern {
        LINEAR,   // Moves in a straight line
        RANDOM,   // Changes direction randomly
        PLAYER    // Controlled by player input
    }

    /**
     * Create a movement components with default values
     */
    public MovementComponent() {

    }

    /**
     * Create a movement components with a specific pattern
     *
     * @param pattern Movement pattern
     */
    public MovementComponent(MovementPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Get movement pattern
     *
     * @return Movement pattern
     */
    public MovementPattern getPattern() {
        return pattern;
    }

    /**
     * Set movement pattern
     *
     * @param pattern Movement pattern
     */
    public void setPattern(MovementPattern pattern) {
        this.pattern = pattern;
    }

    /**
     * Get movement speed
     *
     * @return Speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set movement speed
     *
     * @param speed Speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Get rotation speed in degrees per second
     *
     * @return Rotation speed
     */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Set rotation speed in degrees per second
     *
     * @param rotationSpeed Rotation speed
     */
    public void setRotationSpeed(float rotationSpeed) {
        this.rotationSpeed = rotationSpeed;
    }

    /**
     * Get timestamp of last direction change
     *
     * @return Timestamp in milliseconds
     */
    public long getLastDirectionChange() {
        return lastDirectionChange;
    }

    /**
     * Set timestamp of last direction change
     *
     * @param lastDirectionChange Timestamp in milliseconds
     */
    public void setLastDirectionChange(long lastDirectionChange) {
        this.lastDirectionChange = lastDirectionChange;
    }

    /**
     * Check if entity is accelerating
     *
     * @return true if accelerating
     */
    public boolean isAccelerating() {
        return accelerating;
    }

    /**
     * Set if entity is accelerating
     *
     * @param accelerating true to accelerate, false to stop
     */
    public void setAccelerating(boolean accelerating) {
        this.accelerating = accelerating;
    }
}