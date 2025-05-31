package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.GameOverEvent;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.services.IRenderingContext;
import dk.sdu.mmmi.cbse.core.events.EventService;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.input.Input;
import dk.sdu.mmmi.cbse.core.utils.Time;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game application class.
 */
public class Game extends Application implements IEventListener<GameOverEvent> {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private static Game instance;
    private GameLoop gameLoop;

    private final GameData gameData = new GameData();
    private final World world = new World();
    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private boolean paused = false;
    private double previousTimeScale = 1.0;

    private final List<IPluginService> plugins = new ArrayList<>();
    private IEventService eventService;

    // Game over
    private final AtomicBoolean gameOverTriggered = new AtomicBoolean(false);
    private static final float GAME_OVER_DELAY = 2.0f;
    private float gameOverTimer = 0.0f;

    /**
     * Get singleton instance
     */
    public static Game getInstance() {
        return instance;
    }

    public Game() {
        instance = this;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.log(Level.INFO, "Initializing game with event-driven architecture");

            initializeMainServices();
            createGameWindow(primaryStage);
            setupInputHandling(canvas.getScene());

            gameLoop = new GameLoop(gameData, world, graphicsContext);

            subscribeToGameEvents();

            startPlugins();

            primaryStage.show();
            gameLoop.start();

            LOGGER.log(Level.INFO, "Game initialized");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting game", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }

    /**
     * Initialize core services like event system
     */
    private void initializeMainServices() {
        // Create and register the EventService
        eventService = new EventService();

        // Load all plugins
        ServiceLoader.load(IPluginService.class).forEach(plugins::add);

        LOGGER.log(Level.INFO, "Main services initialized with {0} plugins", plugins.size());
    }

    /**
     * Subscribe to game events
     */
    private void subscribeToGameEvents() {
        if (eventService != null) {
            eventService.subscribe(GameOverEvent.class, this);
            LOGGER.log(Level.INFO, "Game class subscribed to GameOverEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available - game over events won't be handled");
        }
    }

    /**
     * Handle game over event.
     */
    @Override
    public void onEvent(GameOverEvent event) {
        if (gameOverTriggered.compareAndSet(false, true)) {
            LOGGER.log(Level.INFO, "Received GameOverEvent - initiating shutdown sequence");
            LOGGER.log(Level.INFO, "Reason: {0}, Final Score: {1}, Source: {2}",
                    new Object[]{event.reason(), event.finalScore(), event.scoreSource()});

            Platform.runLater(this::startGameOverCountdown);
        }
    }

    /**
     * Start the game over countdown and shutdown sequence
     */
    private void startGameOverCountdown() {
        // Create a timeline for countdown
        gameOverTimer = 0.0f;

        // Use AnimationTimer for countdown to ensure proper thread management
        javafx.animation.AnimationTimer countdownTimer = new javafx.animation.AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0;
                lastUpdate = now;
                gameOverTimer += deltaTime;

                if (gameOverTimer >= GAME_OVER_DELAY) {
                    this.stop();
                    shutdownApplication();
                } else {
                    // Show countdown
                    int secondsLeft = (int) Math.ceil(GAME_OVER_DELAY - gameOverTimer);
                    System.out.println("Closing game in " + secondsLeft + " second" + (secondsLeft != 1 ? "s" : "") + "...");
                }
            }
        };

        countdownTimer.start();
    }

    private void shutdownApplication() {
        LOGGER.log(Level.INFO, "Starting application shutdown sequence");

        try {
            // Stop game loop first
            if (gameLoop != null) {
                gameLoop.stop();
                LOGGER.log(Level.INFO, "Game loop stopped");
            }

            // Stop all plugins
            stopPlugins();

            // Clear world
            world.getEntities().clear();

            // Platform exit - this should work now that everything is cleaned up
            Platform.exit();

            LOGGER.log(Level.INFO, "Application shutdown completed");

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error during shutdown, forcing exit", e);
            // Force exit if graceful shutdown fails
            System.exit(1);
        }
    }

    @Override
    public void stop() {
        LOGGER.log(Level.INFO, "Application stop() called");

        if (gameLoop != null) {
            gameLoop.stop();
        }

        stopPlugins();
    }

    /**
     * Create game window.
     */
    private void createGameWindow(Stage primaryStage) {
        int width = gameData.getDisplayWidth();
        int height = gameData.getDisplayHeight();

        Pane gamePane = new Pane();
        gamePane.setPrefSize(width, height);
        gamePane.setStyle("-fx-background-color: black;");

        canvas = new Canvas(width, height);
        graphicsContext = canvas.getGraphicsContext2D();
        gamePane.getChildren().add(canvas);

        Scene scene = new Scene(gamePane);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Astrostrike");
        primaryStage.setResizable(false);

        // Set close request handler for proper cleanup
        primaryStage.setOnCloseRequest(event -> {
            LOGGER.log(Level.INFO, "Window close requested");
            if (!gameOverTriggered.get()) {
                // User manually closed window
                shutdownApplication();
            }
        });

        for (IRenderingContext context : ModuleConfig.getRenderingContexts()) {
            context.setGraphicsContext(graphicsContext);
            LOGGER.log(Level.INFO, "GraphicsContext set on {0}", context.getClass().getName());
        }

        LOGGER.log(Level.INFO, "Rendering system initialized");
    }

    /**
     * Set up input handling for the game.
     * Maps JavaFX events to the input system.
     */
    private void setupInputHandling(Scene scene) {
        // Keyboard input
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            handleKeyPress(e.getCode(), true);

            // Special key handling
            if (e.getCode() == KeyCode.ESCAPE) {
                togglePause();
            }
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            handleKeyPress(e.getCode(), false);
        });

        // Mouse movement
        scene.setOnMouseMoved(e -> {
            Input.setMousePosition(new Vector2D((float) e.getX(), (float) e.getY()));
        });

        scene.setOnMouseDragged(e -> {
            Input.setMousePosition(new Vector2D((float) e.getX(), (float) e.getY()));
        });

        // Mouse buttons
        scene.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Input.setButton(Button.MOUSE1, true);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                Input.setButton(Button.MOUSE2, true);
            }
        });

        scene.setOnMouseReleased(e -> {
            if (e.getButton() == MouseButton.PRIMARY) {
                Input.setButton(Button.MOUSE1, false);
            } else if (e.getButton() == MouseButton.SECONDARY) {
                Input.setButton(Button.MOUSE2, false);
            }
        });
    }

    /**
     * Handle keyboard input.
     * Maps JavaFX key codes to the input system.
     */
    private void handleKeyPress(KeyCode code, boolean pressed) {
        switch (code) {
            case W:
            case UP:
                Input.setButton(Button.UP, pressed);
                break;
            case S:
            case DOWN:
                Input.setButton(Button.DOWN, pressed);
                break;
            case A:
            case LEFT:
                Input.setButton(Button.LEFT, pressed);
                break;
            case D:
            case RIGHT:
                Input.setButton(Button.RIGHT, pressed);
                break;
            case SPACE:
                Input.setButton(Button.SPACE, pressed);
                break;
            case Q:
                Input.setButton(Button.Q, pressed);
                break;
        }
    }

    /**
     * Start all game plugins.
     * Loads and initializes all registered plugin services.
     */
    private void startPlugins() {
        LOGGER.log(Level.INFO, "Starting {0} game plugins", plugins.size());

        for (IPluginService plugin : ModuleConfig.getPluginServices()) {
            try {
                plugin.start(gameData, world);
                LOGGER.log(Level.FINE, "Started plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error starting plugin: " + plugin.getClass().getName(), e);
            }
        }
    }

    /**
     * Stop all game plugins.
     * Shuts down all registered plugin services.
     */
    private void stopPlugins() {
        LOGGER.log(Level.INFO, "Stopping {0} game plugins", plugins.size());

        for (IPluginService plugin : ModuleConfig.getPluginServices()) {
            try {
                plugin.stop(gameData, world);
                LOGGER.log(Level.FINE, "Stopped plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error stopping plugin: " + plugin.getClass().getName(), e);
            }
        }
    }

    /**
     * Toggle game pause state
     */
    public void togglePause() {
        if (paused) {
            unpauseGame();
        } else {
            pauseGame();
        }
    }

    /**
     * Pause the game
     */
    public void pauseGame() {
        previousTimeScale = Time.getTimeScale();
        Time.setTimeScale(0);
        paused = true;
        LOGGER.log(Level.INFO, "Game paused");
    }

    /**
     * Unpause the game
     */
    public void unpauseGame() {
        Time.setTimeScale(previousTimeScale);
        paused = false;
        LOGGER.log(Level.INFO, "Game resumed");
    }

    /**
     * Restart the game with a fresh state
     */
    public void restart() {
        // Stop current game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Reset game over state
        gameOverTriggered.set(false);
        gameOverTimer = 0.0f;

        // Reset world
        world.getEntities().clear();

        // Re-initialize plugins
        stopPlugins();
        startPlugins();

        // Create new game loop
        gameLoop = new GameLoop(gameData, world, graphicsContext);
        gameLoop.start();

        // Reset time scale
        Time.setTimeScale(1.0);
        paused = false;

        LOGGER.log(Level.INFO, "Game restarted");
    }

    /**
     * Access to game data for other systems
     */
    public GameData getGameData() {
        return gameData;
    }

    /**
     * Access to world for other systems
     */
    public World getWorld() {
        return world;
    }
}