package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.common.services.IRendererSPI;
import dk.sdu.mmmi.cbse.core.input.Input;
import dk.sdu.mmmi.cbse.core.utils.Time;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Game loop implementation using JavaFX AnimationTimer and a separate thread for fixed updates.
 * Handles the main processing cycle of the game.
 */
public class GameLoop extends AnimationTimer {
	private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getName());

	private final GameData gameData;
	private final World world;
	private final GraphicsContext context;

	// Separate thread for fixed-interval processing
	private final ScheduledExecutorService fixedProcessorScheduler;

	// loaded once at initialization
	private final List<IProcessingService> processors = new ArrayList<>();
	private final List<IPostProcessingService> postProcessors = new ArrayList<>();

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

		// Load services once
		ServiceLoader.load(IProcessingService.class).forEach(processors::add);
		ServiceLoader.load(IPostProcessingService.class).forEach(postProcessors::add);

		// Initialize fixed update thread
		fixedProcessorScheduler = Executors.newScheduledThreadPool(1, r -> {
			Thread t = Executors.defaultThreadFactory().newThread(r);
			t.setDaemon(true);
			return t;
		});

		LOGGER.log(Level.INFO, "GameLoop initialized with {0} processors and {1} post-processors",
				new Object[]{processors.size(), postProcessors.size()});
	}

	/**
	 * Start both the animation timer and fixed update scheduler
	 */
	@Override
	public void start() {
		super.start();

		// Schedule fixed update at 60Hz (16ms intervals)
		fixedProcessorScheduler.scheduleAtFixedRate(() -> {
			try {
				fixedUpdate();
			} catch (Exception e) {
				LOGGER.log(Level.SEVERE, "Error in fixed update", e);
			}
		}, 0, 16, TimeUnit.MILLISECONDS);

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
		if (Time.getTimeScale() == 0) {
			return;
		}

		// ToDo: Add Collisions and Physics here.
	}

	@Override
	public void handle(long now) {
		// Calculate delta time in seconds
		double deltaTime = calculateDeltaTime(now);

		// Update global time
		Time.update(deltaTime);

		if (Time.getTimeScale() == 0) {
			return; // Game is paused
		}

		// Process all entities
		processEntities(deltaTime);

		// Process post-entity systems
		processPostEntitySystems();

		// Clear screen and render
		render();

		// Update input for next frame
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
	 * Process all systems.
	 *
	 * @param deltaTime Time since last frame
	 */
	private void processEntities(double deltaTime) {
		gameData.setDeltaTime((float) deltaTime);

		try {
			for (IProcessingService processor : processors) {
				processor.process(gameData, world);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing entities", e);
		}
	}

	/**
	 * Process all post systems.
	 * These run after the main processing.
	 */
	private void processPostEntitySystems() {
		try {
			for (IPostProcessingService postProcessor : postProcessors) {
				postProcessor.process(gameData, world);
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in post-processing", e);
		}
	}

	/**
	 * Render the game state.
	 */
	private void render() {
		// ToDo: Implement a smart way to only process renderers (Entity and Debug Renderers) here.
		for (IPostProcessingService processor : postProcessors) {
			if (processor instanceof IRendererSPI) {
				if (context != null) {
					((IRendererSPI) processor).setGraphicsContext(context);
				}
				break;
			}
		}
	}
}