package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.core.input.Input;
import dk.sdu.mmmi.cbse.core.utils.Time;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles the main processing cycle of the game.
 */
public class GameLoop extends AnimationTimer {
	private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getName());

	private final GameData gameData;
	private final World world;
	private final GraphicsContext context;

	// for fixed-interval processing
	private final ScheduledExecutorService fixedProcessorScheduler;

	private long lastTime = 0;

	/**
	 * Create a new game loop.
	 *
	 * @param gameData Game state data
	 * @param world Game world with entities
	 * @param context Graphics context for rendering
	 */
	public GameLoop(GameData gameData, World world, GraphicsContext context) {
		this.gameData = gameData;
		this.world = world;
		this.context = context;

		// Initialize fixed update thread
		fixedProcessorScheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});

		LOGGER.log(Level.INFO, "GameLoop initialized with fixed update rate: {0}Hz ({1}ms intervals)",
				new Object[]{Time.FIXED_UPDATE_RATE, Time.FIXED_UPDATE_INTERVAL_MS});
	}

	/**
	 * Start both the animation timer and fixed update scheduler
	 */
	@Override
	public void start() {
		super.start();

		fixedProcessorScheduler.scheduleAtFixedRate(() -> {
			try {
				fixedUpdate();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error in fixed update", e);
			}
		}, 0, Time.FIXED_UPDATE_INTERVAL_MS, TimeUnit.MILLISECONDS);

		LOGGER.log(Level.INFO, "Game loop started");
	}

	/**
	 * Stop both the animation timer and fixed update scheduler
	 */
	@Override
	public void stop() {
		super.stop();

		try {
			fixedProcessorScheduler.shutdown();
			if (!fixedProcessorScheduler.awaitTermination(500, TimeUnit.MILLISECONDS)) {
				fixedProcessorScheduler.shutdownNow();
			}
		} catch (InterruptedException e) {
			fixedProcessorScheduler.shutdownNow();
			Thread.currentThread().interrupt();
		}

		LOGGER.log(Level.INFO, "Game loop stopped");
	}

	/**
	 * Update that runs on a separate thread at a fixed interval
	 */
	private void fixedUpdate() {
		if (Time.isPaused()) {
			return;
		}

		processFixedUpdateSystems();
	}

	@Override
	public void handle(long now) {
		double deltaTime = calculateDeltaTime(now);

		Time.update(deltaTime);

		if (Time.isPaused()) {
			return; // Game is paused
		}

		processUpdateSystems();

		processLateUpdateSystems();

		Input.update();
	}

	/**
	 * Calculate time elapsed since last frame.
	 *
	 * @param now Current time in nanoseconds
	 * @return Delta time in seconds
	 */
	private double calculateDeltaTime(long now) {
		if (lastTime == 0) {
			lastTime = now;
			return 0;
		}

		double deltaTime = (now - lastTime) / 1_000_000_000.0;
		lastTime = now;

		// Cap delta time to prevent huge jumps
		return Math.min(deltaTime, 0.1);
	}

	/**
	 * Process all update systems.
	 */
	private void processUpdateSystems() {
		try {
			for (IUpdate processor : ModuleConfig.getUpdateServices()) {
				processor.update(gameData, world);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing update systems", e);
		}
	}

	/**
	 * Process fixed update systems in the separate thread.
	 * This runs at a consistent rate defined by Time.FIXED_UPDATE_RATE.
	 */
	private void processFixedUpdateSystems() {
		try {
			for (IFixedUpdate processor : ModuleConfig.getFixedUpdateServices()) {
				processor.fixedUpdate(gameData, world);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing fixed update systems", e);
		}
	}

	/**
	 * Process all late update systems.
	 */
	private void processLateUpdateSystems() {
		try {
			for (ILateUpdate postProcessor : ModuleConfig.getLateUpdateServices()) {
				postProcessor.process(gameData, world);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in post-processing", e);
		}
	}
}