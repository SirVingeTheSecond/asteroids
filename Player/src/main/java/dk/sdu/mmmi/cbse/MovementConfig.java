package dk.sdu.mmmi.cbse;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Movement configuration focusing on essential parameters.
 */
// ToDo: Not really used?
public class MovementConfig {
    private static final Logger LOGGER = Logger.getLogger(MovementConfig.class.getName());

    // === CORE MOVEMENT PARAMETERS ===

    /**
     * Force applied when player provides input (higher = more responsive)
     */
    public static final float ACCELERATION_FORCE = 800.0f;

    /**
     * Maximum player movement speed
     */
    public static final float MAX_SPEED = 160.0f;

    /**
     * Drag coefficient for natural deceleration (0.0 = no drag, 1.0 = instant stop)
     * 0.88 means velocity is reduced by 12% each frame
     */
    public static final float DRAG_COEFFICIENT = 0.88f;

    /**
     * Speed threshold below which we apply hard stop to prevent crawling
     */
    public static final float STOP_THRESHOLD = 5.0f;

    // === MOVEMENT FEEL PRESETS ===

    /**
     * Movement feel presets for different gameplay styles
     */
    public enum MovementFeel {
        RESPONSIVE(1000.0f, 180.0f, 0.85f),   // Snappy, arcade-like
        BALANCED(800.0f, 160.0f, 0.88f),      // Good balance of weight and responsiveness
        WEIGHTED(600.0f, 140.0f, 0.92f),      // More realistic, heavier feeling
        EXPERIMENTAL(1200.0f, 200.0f, 0.80f); // For testing extreme values

        public final float accelerationForce;
        public final float maxSpeed;
        public final float dragCoefficient;

        MovementFeel(float accelerationForce, float maxSpeed, float dragCoefficient) {
            this.accelerationForce = accelerationForce;
            this.maxSpeed = maxSpeed;
            this.dragCoefficient = dragCoefficient;
        }
    }

    // Current active configuration
    private static MovementFeel activeFeel = MovementFeel.BALANCED;

    /**
     * Get current movement configuration
     */
    public static MovementFeel getActiveFeel() {
        return activeFeel;
    }

    /**
     * Set movement feel
     */
    public static void setMovementFeel(MovementFeel feel) {
        activeFeel = feel;
        LOGGER.log(Level.INFO, "Movement feel changed to: {0} (Force: {1}, Speed: {2}, Drag: {3})",
                new Object[]{feel.name(), feel.accelerationForce, feel.maxSpeed, feel.dragCoefficient});
    }

    /**
     * Get acceleration force with current feel applied
     */
    public static float getAccelerationForce() {
        return activeFeel.accelerationForce;
    }

    /**
     * Get maximum speed with current feel applied
     */
    public static float getMaxSpeed() {
        return activeFeel.maxSpeed;
    }

    /**
     * Get drag coefficient with current feel applied
     */
    public static float getDragCoefficient() {
        return activeFeel.dragCoefficient;
    }

    /**
     * Calculate stopping force multiplier based on current speed
     * Provides smoother stops at higher speeds
     */
    public static float calculateStopForceMultiplier(float currentSpeed, float maxSpeed) {
        if (maxSpeed <= 0) return 0.6f;

        float speedRatio = Math.min(currentSpeed / maxSpeed, 1.0f);
        // At high speeds, apply more stopping force for quicker response
        return 0.4f + (speedRatio * 0.4f); // Range: 0.4 to 0.8
    }

    /**
     * Get debug information about current movement configuration
     */
    public static String getDebugInfo() {
        return String.format("Movement: %s | Force: %.0f | Speed: %.0f | Drag: %.2f | Stop: %.1f",
                activeFeel.name(),
                getAccelerationForce(),
                getMaxSpeed(),
                getDragCoefficient(),
                STOP_THRESHOLD);
    }

    /**
     * Validate movement configuration for potential issues
     */
    public static boolean validateConfiguration() {
        boolean valid = true;

        if (getAccelerationForce() <= 0) {
            LOGGER.log(Level.WARNING, "Invalid acceleration force: {0}", getAccelerationForce());
            valid = false;
        }

        if (getDragCoefficient() <= 0 || getDragCoefficient() >= 1.0f) {
            LOGGER.log(Level.WARNING, "Invalid drag coefficient: {0} (should be 0.0 < drag < 1.0)",
                    getDragCoefficient());
            valid = false;
        }

        if (getMaxSpeed() <= 0) {
            LOGGER.log(Level.WARNING, "Invalid max speed: {0}", getMaxSpeed());
            valid = false;
        }

        return valid;
    }
}