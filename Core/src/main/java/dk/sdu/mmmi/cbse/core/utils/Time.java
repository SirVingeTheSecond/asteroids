package dk.sdu.mmmi.cbse.core.utils;

/**
 * Global time management class.
 * Centralized time handling for consistent behavior across all systems.
 *
 * Uses temporal anti-aliasing for smooth bullet movement: smaller delta time
 * steps than actual update frequency to reduce visual stepping artifacts.
 */
public final class Time {
	// Fixed update rate configuration
	public static final int FIXED_UPDATE_RATE = 60; // Hz
	public static final float FIXED_DELTA_TIME = 1.0f / FIXED_UPDATE_RATE; // 0.01666 seconds
	public static final int FIXED_UPDATE_INTERVAL_MS = 1000 / FIXED_UPDATE_RATE; // 16 milliseconds

	// Delta time for bullet movement (should explain how this is temporal anti-aliasing)
	public static final int BULLET_UPDATE_RATE = 120; // Hz - for smoother visual movement
	public static final float BULLET_DELTA_TIME = 1.0f / BULLET_UPDATE_RATE; // 0.0083 seconds

	// Total elapsed game time in seconds
	private static double time = 0.0;

	// Time elapsed since last frame in seconds
	private static double deltaTime = 0.0;

	// Time scale (1.0 = normal speed)
	private static double timeScale = 1.0;

	// Frame counter
	private static long frameCount = 0;

	/**
	 * Private constructor to prevent instantiation
	 */
	private Time() {

	}

	/**
	 * Get total elapsed game time
	 *
	 * @return Total time in seconds
	 */
	public static double getTime() {
		return time;
	}

	/**
	 * Get time elapsed since last frame
	 *
	 * @return Delta time in seconds
	 */
	public static double getDeltaTime() {
		return deltaTime;
	}

	/**
	 * Get time elapsed since last frame as float (for convenience)
	 *
	 * @return Delta time in seconds as float
	 */
	public static float getDeltaTimeF() {
		return (float) deltaTime;
	}

	/**
	 * Get fixed delta time for fixed update systems
	 *
	 * @return Fixed delta time in seconds
	 */
	public static float getFixedDeltaTime() {
		return FIXED_DELTA_TIME;
	}

	/**
	 * Get high-frequency delta time for smooth bullet movement
	 * Uses smaller time steps for visual smoothness (temporal anti-aliasing)
	 *
	 * @return Bullet delta time in seconds
	 */
	public static float getBulletDeltaTime() {
		return BULLET_DELTA_TIME;
	}

	/**
	 * Get current time scale
	 *
	 * @return Time scale (1.0 = normal)
	 */
	public static double getTimeScale() {
		return timeScale;
	}

	/**
	 * Set time scale
	 *
	 * @param scale New time scale (1.0 = normal)
	 */
	public static void setTimeScale(double scale) {
		if (scale >= 0) {
			timeScale = scale;
		}
	}

	/**
	 * Get current frame count
	 *
	 * @return Total frames since start
	 */
	public static long getFrameCount() {
		return frameCount;
	}

	/**
	 * Check if the game is paused (time scale is 0)
	 *
	 * @return true if game is paused
	 */
	public static boolean isPaused() {
		return timeScale == 0.0;
	}

	/**
	 * Update time values
	 * Called once per frame
	 *
	 * @param dt Raw time elapsed since last frame
	 */
	public static void update(double dt) {
		frameCount++;
		deltaTime = dt * timeScale;
		time += deltaTime;
	}
}