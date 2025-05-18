package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.core.input.Input;
import dk.sdu.mmmi.cbse.core.utils.Time;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;

import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Game loop implementation using JavaFX AnimationTimer.
 * Handles the main processing cycle of the game.
 */
public class GameLoop extends AnimationTimer {
	private static final Logger LOGGER = Logger.getLogger(GameLoop.class.getName());

	private final GameData gameData;
	private final World world;
	private final GraphicsContext context;

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
	}

	@Override
	public void handle(long now) {
		// Calculate delta time in seconds
		double deltaTime = calculateDeltaTime(now);

		// Update global time
		Time.update(deltaTime);

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
			ServiceLoader<IProcessingService> loader = ServiceLoader.load(IProcessingService.class);

			Iterator<IProcessingService> iterator = loader.iterator();
			iterator.forEachRemaining(processor ->
					processor.process(gameData, world)
			);
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
			ServiceLoader<IPostProcessingService> loader = ServiceLoader.load(IPostProcessingService.class);

			Iterator<IPostProcessingService> iterator = loader.iterator();
			iterator.forEachRemaining(postProcessor ->
					postProcessor.process(gameData, world)
			);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error processing post-entity systems", e);
		}
	}


	/**
	 * Render the game state.
	 */
	private void render() {
		// Clear screen
		context.clearRect(0, 0, gameData.getDisplayWidth(), gameData.getDisplayHeight());

		// Rendering would be handled by a separate RenderSystem registered as a service
		// The RenderSystem would be responsible for rendering all entities
	}
}