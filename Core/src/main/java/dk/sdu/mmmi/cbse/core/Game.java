package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.*;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.input.Input;
import dk.sdu.mmmi.cbse.core.utils.Time;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game application class.
 */
public class Game extends Application {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private static Game instance;
    private GameLoop gameLoop;

    private final GameData gameData = new GameData();
    private final World world = new World();
    private Canvas canvas;
    private GraphicsContext graphicsContext;

    private boolean paused = false;
    private double previousTimeScale = 1.0;

    // Spring-injected dependencies
    private List<IPluginService> pluginServices;
    private List<IUpdate> updateServices;
    private List<IFixedUpdate> fixedUpdateServices;
    private List<ILateUpdate> lateUpdateServices;
    private List<IRenderingContext> renderingContexts;
    private IEventService eventService;

    /**
     * Default constructor required by JavaFX
     */
    public Game() {
        instance = this;
        LOGGER.log(Level.INFO, "Game instance created by JavaFX");
    }

    /**
     * Get singleton instance
     */
    public static Game getInstance() {
        return instance;
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.log(Level.INFO, "Initializing game with Spring dependencies from ApplicationContext");

            injectSpringDependencies();

            createGameWindow(primaryStage);
            setupInputHandling(canvas.getScene());

            gameLoop = new GameLoop(gameData, world, graphicsContext,
                    updateServices, fixedUpdateServices, lateUpdateServices);

            startPlugins();

            primaryStage.show();
            gameLoop.start();

            LOGGER.log(Level.INFO, "Game initialized and running with Spring DI");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting game", e);
            throw new RuntimeException("Failed to start game", e);
        }
    }

    /**
     * Inject Spring dependencies from the ApplicationContext
     */
    @SuppressWarnings("unchecked")
    private void injectSpringDependencies() {
        ApplicationContext context = Main.getApplicationContext();

        if (context == null) {
            throw new IllegalStateException("Spring ApplicationContext not available");
        }

        // Get all dependencies from Spring context
        this.pluginServices = context.getBean("pluginServices", List.class);
        this.updateServices = context.getBean("updateServices", List.class);
        this.fixedUpdateServices = context.getBean("fixedUpdateServices", List.class);
        this.lateUpdateServices = context.getBean("lateUpdateServices", List.class);
        this.renderingContexts = context.getBean("renderingContexts", List.class);
        this.eventService = context.getBean(IEventService.class);

        LOGGER.log(Level.INFO, "Spring dependencies injected - Plugins: {0}, Updates: {1}, FixedUpdates: {2}, LateUpdates: {3}, RenderingContexts: {4}",
                new Object[]{pluginServices.size(), updateServices.size(), fixedUpdateServices.size(),
                        lateUpdateServices.size(), renderingContexts.size()});
    }

    @Override
    public void stop() {
        LOGGER.log(Level.INFO, "Stopping game");

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

        // Set graphics context on all injected rendering contexts
        for (IRenderingContext context : renderingContexts) {
            context.setGraphicsContext(graphicsContext);
            LOGGER.log(Level.INFO, "GraphicsContext set on injected {0}", context.getClass().getName());
        }

        LOGGER.log(Level.INFO, "Rendering system initialized with {0} contexts", renderingContexts.size());
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
                Input.setButton(Button.UP, pressed);
            case UP:
                Input.setButton(Button.UP, pressed);
                break;
            case S:
                Input.setButton(Button.DOWN, pressed);
            case DOWN:
                Input.setButton(Button.DOWN, pressed);
                break;
            case A:
                Input.setButton(Button.LEFT, pressed);
            case LEFT:
                Input.setButton(Button.LEFT, pressed);
                break;
            case D:
                Input.setButton(Button.RIGHT, pressed);
            case RIGHT:
                Input.setButton(Button.RIGHT, pressed);
                break;
            case SPACE:
                Input.setButton(Button.SPACE, pressed);
                break;
        }
    }

    /**
     * Start all game plugins.
     */
    private void startPlugins() {
        LOGGER.log(Level.INFO, "Starting {0} Spring-injected game plugins", pluginServices.size());

        for (IPluginService plugin : pluginServices) {
            try {
                plugin.start(gameData, world);
                LOGGER.log(Level.FINE, "Started injected plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error starting injected plugin: " + plugin.getClass().getName(), e);
            }
        }
    }

    /**
     * Stop all game plugins.
     */
    private void stopPlugins() {
        LOGGER.log(Level.INFO, "Stopping {0} Spring-injected game plugins", pluginServices.size());

        for (IPluginService plugin : pluginServices) {
            try {
                plugin.stop(gameData, world);
                LOGGER.log(Level.FINE, "Stopped injected plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error stopping injected plugin: " + plugin.getClass().getName(), e);
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
     * Restart the game.
     */
    public void restart() {
        // Stop current game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Reset world
        world.getEntities().clear();

        // Re-initialize plugins
        stopPlugins();
        startPlugins();

        gameLoop = new GameLoop(gameData, world, graphicsContext,
                updateServices, fixedUpdateServices, lateUpdateServices);
        gameLoop.start();

        // Reset time scale
        Time.setTimeScale(1.0);
        paused = false;

        LOGGER.log(Level.INFO, "Game restarted with Spring DI");
    }

    /**
     * Get the event service
     */
    public IEventService getEventService() {
        return eventService;
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