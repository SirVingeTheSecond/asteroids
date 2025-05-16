package dk.sdu.mmmi.cbse.core.input;

import dk.sdu.mmmi.cbse.common.Vector2D;

/**
 * Utility class for accessing input states.
 */
public class InputController {
    /**
     * Get axis value
     *
     * @param axis Axis to query
     * @return Value between -1 and 1
     */
    public static float getAxis(Axis axis) {
        return Input.getAxis(axis);
    }

    /**
     * Get mouse position
     *
     * @return Mouse position vector
     */
    public static Vector2D getMousePosition() {
        return Input.getMousePosition();
    }

    /**
     * Check if button is currently pressed
     *
     * @param button Button to check
     * @return true if pressed
     */
    public static boolean isButtonPressed(Button button) {
        return Input.isButtonPressed(button);
    }

    /**
     * Check if button was just pressed this frame
     *
     * @param button Button to check
     * @return true if just pressed
     */
    public static boolean isButtonDown(Button button) {
        return Input.isButtonDown(button);
    }

    /**
     * Check if button was just released this frame
     *
     * @param button Button to check
     * @return true if just released
     */
    public static boolean isButtonUp(Button button) {
        return Input.isButtonUp(button);
    }

    /**
     * Get a movement vector based on UP, DOWN, LEFT, RIGHT inputs
     *
     * @return Normalized movement vector
     */
    public static Vector2D getMovementVector() {
        float x = 0;
        float y = 0;

        if (Input.isButtonPressed(Button.LEFT)) x -= 1;
        if (Input.isButtonPressed(Button.RIGHT)) x += 1;
        if (Input.isButtonPressed(Button.UP)) y -= 1;
        if (Input.isButtonPressed(Button.DOWN)) y += 1;

        // Normalize if not zero
        Vector2D movement = new Vector2D(x, y);
        if (movement.magnitudeSquared() > 0.001f) {
            return movement.normalize();
        }
        return movement;
    }
}