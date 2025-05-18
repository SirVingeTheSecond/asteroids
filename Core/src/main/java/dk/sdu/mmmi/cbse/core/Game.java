package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.input.Input;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game application class.

 */
public class Game extends Application {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());

    private final GameData gameData = new GameData();
    private final World world = new World();
    private GameLoop gameLoop;
    private Canvas canvas;
    private GraphicsContext graphicsContext;

    @Override
    public void start(Stage primaryStage) {
        try {
            LOGGER.log(Level.INFO, "Initializing game");

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

            setupInput(scene);

            gameLoop = new GameLoop(gameData, world, graphicsContext);

            startPlugins();

            primaryStage.show();

            gameLoop.start();

            LOGGER.log(Level.INFO, "Game initialized and running");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error starting game", e);
            throw new RuntimeException("Failed to start game", e);
        }
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
     * Set up input handling for the game.
     * Maps JavaFX events to our input system.
     *
     * @param scene The JavaFX scene
     */
    private void setupInput(Scene scene) {
        // Keyboard input
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            handleKeyPress(e.getCode(), true);
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
     *
     * @param code JavaFX key code
     * @param pressed Whether the key is pressed or released
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
        }
    }

    /**
     * Start all game plugins.
     * Loads and initializes all registered plugin services.
     */
    private void startPlugins() {
        ServiceLoader<IPluginService> loader = ServiceLoader.load(IPluginService.class);
        Iterator<IPluginService> iterator = loader.iterator();

        List<IPluginService> plugins = new ArrayList<>();
        iterator.forEachRemaining(plugins::add);

        LOGGER.log(Level.INFO, "Starting {0} game plugins", plugins.size());

        Iterator<IPluginService> services = plugins.iterator();
        services.forEachRemaining(plugin -> {
            try {
                plugin.start(gameData, world);
                LOGGER.log(Level.FINE, "Started plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error starting plugin: " + plugin.getClass().getName(), e);
            }
        });
    }

    /**
     * Stop all game plugins.
     * Shuts down all registered plugin services.
     */
    private void stopPlugins() {
        ServiceLoader<IPluginService> loader = ServiceLoader.load(IPluginService.class);
        Iterator<IPluginService> iterator = loader.iterator();

        List<IPluginService> plugins = new ArrayList<>();
        iterator.forEachRemaining(plugins::add);

        LOGGER.log(Level.INFO, "Stopping {0} game plugins", plugins.size());

        Iterator<IPluginService> services = plugins.iterator();
        services.forEachRemaining(plugin -> {
            try {
                plugin.stop(gameData, world);
                LOGGER.log(Level.FINE, "Stopped plugin: {0}", plugin.getClass().getName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE,
                        "Error stopping plugin: " + plugin.getClass().getName(), e);
            }
        });
    }
}