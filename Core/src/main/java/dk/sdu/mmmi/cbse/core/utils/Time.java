package dk.sdu.mmmi.cbse.core.utils;

/**
 * Global time management class.
 */
public final class Time {
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