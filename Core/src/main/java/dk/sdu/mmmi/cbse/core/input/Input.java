package dk.sdu.mmmi.cbse.core.input;

import dk.sdu.mmmi.cbse.common.Vector2D;

import java.util.EnumMap;

/**
 * Core input handling system.
 * Maintains button states and axis values.
 */
public class Input {
	// Current and previous frame button states
	private static final EnumMap<Button, Boolean> currentButtons = new EnumMap<>(Button.class);
	private static final EnumMap<Button, Boolean> previousButtons = new EnumMap<>(Button.class);

	// Axis values
	private static final EnumMap<Axis, Float> axes = new EnumMap<>(Axis.class);

	// Mouse position
	private static Vector2D mousePosition = new Vector2D(0, 0);

	// Initialize maps
	static {
		for (Button button : Button.values()) {
			currentButtons.put(button, false);
			previousButtons.put(button, false);
		}

		for (Axis axis : Axis.values()) {
			axes.put(axis, 0.0f);
		}
	}

	/**
	 * Check if a button is currently pressed
	 *
	 * @param button Button to check
	 * @return true if pressed
	 */
	public static boolean isButtonPressed(Button button) {
		return currentButtons.get(button);
	}

	/**
	 * Check if a button was just pressed this frame
	 *
	 * @param button Button to check
	 * @return true if just pressed
	 */
	public static boolean isButtonDown(Button button) {
		return currentButtons.get(button) && !previousButtons.get(button);
	}

	/**
	 * Check if a button was just released this frame
	 *
	 * @param button Button to check
	 * @return true if just released
	 */
	public static boolean isButtonUp(Button button) {
		return !currentButtons.get(button) && previousButtons.get(button);
	}

	/**
	 * Get the value of an axis
	 *
	 * @param axis Axis to get
	 * @return Value between -1.0 and 1.0
	 */
	public static float getAxis(Axis axis) {
		return axes.get(axis);
	}

	/**
	 * Get mouse position as a Vector2D
	 *
	 * @return Mouse position
	 */
	public static Vector2D getMousePosition() {
		return mousePosition;
	}

	/**
	 * Set button pressed state
	 *
	 * @param button Button to set
	 * @param pressed Pressed state
	 */
	public static void setButton(Button button, boolean pressed) {
		currentButtons.put(button, pressed);
	}

	/**
	 * Set axis value
	 *
	 * @param axis Axis to set
	 * @param value Value between -1.0 and 1.0
	 */
	public static void setAxis(Axis axis, float value) {
		// Clamp value to [-1, 1]
		value = Math.max(-1.0f, Math.min(1.0f, value));
		axes.put(axis, value);
	}

	/**
	 * Set mouse position
	 *
	 * @param position Mouse position
	 */
	public static void setMousePosition(Vector2D position) {
		mousePosition = position;
	}

	/**
	 * Update input state for next frame.
	 * Called at the end of each frame.
	 */
	public static void update() {
		// Copy current to previous for next frame
		previousButtons.putAll(currentButtons);
	}
}