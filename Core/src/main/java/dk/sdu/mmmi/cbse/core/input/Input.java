package dk.sdu.mmmi.cbse.core.input;

import dk.sdu.sem.commonsystem.Vector2D;

import java.util.EnumMap;

public class Input {
	private static final EnumMap<Key, Boolean> currentKeys = new EnumMap<>(Key.class);
	private static final EnumMap<Key, Boolean> prevKeys = new EnumMap<>(Key.class);

	private static Vector2D mousePosition = new Vector2D(0,0);

	static {
		for (Key key : Key.values()) {
			currentKeys.put(key, false);
			prevKeys.put(key, false);
		}
	}

	/**
	 * Returns true while the user holds down the key.
	 */
	public static boolean getKey(Key key) {
		return currentKeys.get(key);
	}

	/**
	 * Returns true during the frame the user starts pressing down the key.
	 */
	public static boolean getKeyDown(Key key) {
		return currentKeys.get(key) && !prevKeys.get(key);
	}

	/**
	 * Returns true during the frame the user stops pressing down the key.
	 */
	public static boolean getKeyUp(Key key) {
		return !currentKeys.get(key) && prevKeys.get(key);
	}

	public static void update() {
		prevKeys.putAll(currentKeys);
	}

	public static void setKeyPressed(Key key, boolean pressed) {
		currentKeys.put(key, pressed);
	}

	public static Vector2D getMousePosition() {
		return mousePosition;
	}

	public static void setMousePosition(Vector2D mousePosition) {
		Input.mousePosition = mousePosition;
	}

	public static Vector2D getMove() {
		return new Vector2D(
				(currentKeys.get(Key.LEFT) ? -1 : 0) + (currentKeys.get(Key.RIGHT) ? 1 : 0),
				(currentKeys.get(Key.UP) ? -1 : 0) + (currentKeys.get(Key.DOWN) ? 1 : 0)
		).normalize();
	}
}
