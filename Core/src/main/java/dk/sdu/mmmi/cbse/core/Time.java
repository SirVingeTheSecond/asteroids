package dk.sdu.mmmi.cbse.core;

import java.util.ArrayList;
import java.util.List;

public final class Time {
	// Total elapsed simulation time (in seconds).
	private static double time = 0.0;

	// Delta time (time elapsed since last frame) (in seconds).
	private static double deltaTime = 0.0;

	// Fixed delta time for fixed updates (in seconds). Default for 60Hz.
	private static double fixedDeltaTime = 0.016667;

	// Global time scale (1.0 = normal speed).
	private static double timeScale = 1.0;

	// Total frames since game start
	private static long frameCount = 0;

	private Time() {
		// Prevent instantiation.
	}

	private static List<ScheduledAction> schedule = new ArrayList<ScheduledAction>();

	public static double getTime() {
		return time;
	}

	public static double getDeltaTime() {
		return deltaTime;
	}

	public static double getFixedDeltaTime() {
		return fixedDeltaTime;
	}

	public static double getTimeScale() {
		return timeScale;
	}

	public static void setTimeScale(double newTimeScale) {
		if (newTimeScale >= 0) {
			timeScale = newTimeScale;
		}
	}

	/**
	 * Call this during each variable-rate update (from the UI thread).
	 * @param dt Time in seconds since the last frame.
	 */
	public static void update(double dt) {
		frameCount += 1;
		deltaTime = dt * timeScale;
		time += deltaTime;

		schedule.removeIf(action -> {
			action.elapsed += deltaTime;
			if (action.elapsed >= action.duration) {
				action.action.run();
				return true; // Remove the action from the schedule
			}
			return false; // Keep the action in the schedule
		});
	}

    public static void after(float duration, Runnable action) {
		schedule.add(new ScheduledAction(duration, action));
    }

	public static long getFrameCount() {
		return frameCount;
	}
}