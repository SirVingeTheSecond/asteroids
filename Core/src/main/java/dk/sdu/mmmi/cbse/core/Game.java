package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.input.Input;
import dk.sdu.mmmi.cbse.common.services.*;
import dk.sdu.mmmi.cbse.common.util.ServiceLocator;
import dk.sdu.mmmi.cbse.core.input.Key;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main game application class.
 * Initializes the game environment and manages the lifecycle.
 */
public class Game extends Application {
    private static final Logger LOGGER = Logger.getLogger(Game.class.getName());
    private final GameData gameData = new GameData();
    private final World world = new World();
    private GameLoop gameLoop;

    @Override
    public void start(Stage primaryStage) {
        // Create game window
        Pane gameWindow = new Pane();
        gameWindow.setPrefSize(gameData.getDisplayWidth(), gameData.getDisplayHeight());
        gameWindow.setStyle("-fx-background-color: black;");

        // Create game canvas
        Canvas gameCanvas = new Canvas(gameData.getDisplayWidth(), gameData.getDisplayHeight());
        gameWindow.getChildren().add(gameCanvas);

        // Setup scene
        Scene scene = new Scene(gameWindow);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Astrostrike");
        primaryStage.setResizable(false);

        // Setup input handling
        setupInput(scene);

        // Create the game loop
        gameLoop = new GameLoop(gameData, world, gameCanvas.getGraphicsContext2D());

        // Start all game plugins using ServiceLocator
        List<IGamePluginService> plugins = ServiceLocator.locateAll(IGamePluginService.class);
        LOGGER.log(Level.INFO, "Starting {0} game plugins", plugins.size());
        plugins.forEach(plugin -> plugin.start(gameData, world));

        // Show the window
        primaryStage.show();

        // Start the game loop
        gameLoop.start();
    }

    @Override
    public void stop() {
        // Stop the game loop
        if (gameLoop != null) {
            gameLoop.stop();
        }

        // Stop all game plugins
        List<IGamePluginService> plugins = ServiceLocator.locateAll(IGamePluginService.class);
        LOGGER.log(Level.INFO, "Stopping {0} game plugins", plugins.size());
        plugins.forEach(plugin -> plugin.stop(gameData, world));
    }

    /**
     * Setup input handling for the game.
     */
    private void setupInput(Scene scene) {
        // Map JavaFX key events to our input system
        scene.addEventHandler(KeyEvent.KEY_PRESSED, e -> {
            Input.KeyCode code = mapKeyCode(e.getCode());
            if (code != null) {
                Input.setKey(code, true);
            }

            // Toggle debug mode with F3
            if (e.getCode() == KeyCode.F3) {
                gameData.setDebugMode(!gameData.isDebugMode());
            }
        });

        scene.addEventHandler(KeyEvent.KEY_RELEASED, e -> {
            Input.KeyCode code = mapKeyCode(e.getCode());
            if (code != null) {
                Input.setKey(code, false);
            }
        });

        // Handle mouse events
        scene.setOnMouseMoved(e -> {
            Input.setAxis(Input.AxisName.MOUSEX, (float) e.getX());
            Input.setAxis(Input.AxisName.MOUSEY, (float) e.getY());
        });
    }

    /**
     * Map JavaFX key codes to our input system's key codes.
     */
    private Input.KeyCode mapKeyCode(KeyCode code) {
        switch (code) {
            case UP:    return Input.KeyCode.UP;
            case DOWN:  return Input.KeyCode.DOWN;
            case LEFT:  return Input.KeyCode.LEFT;
            case RIGHT: return Input.KeyCode.RIGHT;
            case SPACE: return Input.KeyCode.SPACE;
            case ESCAPE: return Input.KeyCode.ESCAPE;
            case W:     return Input.KeyCode.W;
            case A:     return Input.KeyCode.A;
            case S:     return Input.KeyCode.S;
            case D:     return Input.KeyCode.D;
            // Map other keys as needed
            default:    return null;
        }
    }

    /**
     * Launch the game application.
     */
    public static void main(String[] args) {
        launch(args);
    }
}

public class Game {
    private static Game instance = new Game();
    Logging LOGGER = Logging.createLogger("Game", LoggingLevel.DEBUG);

    private AnimationTimer renderLoop;
    private GameLoop gameLoop;
    private FXRenderSystem renderSystem;
    private Canvas canvas;
    private IMenuSPI menuManager;
    private Stage stage;
    private StackPane root;
    boolean paused = false;
    double prevScale;

    private Game() {

    }

    public static Game getInstance() {
        return instance;
    }

    private void setupInputs(Scene scene) {
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W:
                    Input.setKeyPressed(Key.UP, true);
                    break;
                case S:
                    Input.setKeyPressed(Key.DOWN, true);
                    break;
                case A:
                    Input.setKeyPressed(Key.LEFT, true);
                    break;
                case D:
                    Input.setKeyPressed(Key.RIGHT, true);
                    break;
                case SPACE:
                    Input.setKeyPressed(Key.SPACE, true);
                    break;
                case ESCAPE:
                    togglePause();
                    break;
                case F5:
                    LOGGER.debug("F5 pressed - toggling collider visualization");
                    toggleDebugVisualization(IDebugController::toggleColliderVisualization);
                    break;
            }
        });

        scene.setOnKeyReleased(event -> {
            switch (event.getCode()) {
                case W:
                    Input.setKeyPressed(Key.UP, false);
                    break;
                case S:
                    Input.setKeyPressed(Key.DOWN, false);
                    break;
                case A:
                    Input.setKeyPressed(Key.LEFT, false);
                    break;
                case D:
                    Input.setKeyPressed(Key.RIGHT, false);
                    break;
                case SPACE:
                    Input.setKeyPressed(Key.SPACE, false);
                    break;
            }
        });

        scene.setOnMousePressed(event -> {
            LOGGER.debug("ON MOUSE CLICKED " + (event.getButton() == MouseButton.PRIMARY));
            switch (event.getButton()) {
                case PRIMARY:
                    Input.setKeyPressed(Key.MOUSE1, true);
                    break;
                case SECONDARY:
                    Input.setKeyPressed(Key.MOUSE2, true);
                    break;
            }
        });

        scene.setOnMouseReleased(event -> {
            switch (event.getButton()) {
                case PRIMARY:
                    Input.setKeyPressed(Key.MOUSE1, false);
                    break;
                case SECONDARY:
                    Input.setKeyPressed(Key.MOUSE2, false);
                    break;
            }
        });

        scene.setOnMouseMoved(event -> {
            Point2D p = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());

            Input.setMousePosition(new Vector2D(
                    (float)(p.getX()),
                    (float)(p.getY())
            ));
        });

        scene.setOnMouseDragged(event -> {
            Point2D p = canvas.sceneToLocal(event.getSceneX(), event.getSceneY());

            Input.setMousePosition(new Vector2D(
                    (float)(p.getX()),
                    (float)(p.getY())
            ));
        });
    }

    /**
     * Helper method to toggle a debug visualization.
     */
    private void toggleDebugVisualization(java.util.function.Consumer<IDebugController> toggler) {
        LOGGER.debug("Attempting to toggle debug visualization");
        ServiceLoader<IDebugController> serviceLoader = ServiceLoader.load(IDebugController.class);

        boolean found = false;
        for (IDebugController controller : serviceLoader) {
            LOGGER.debug("Found controller: " + controller.getClass().getName());
            toggler.accept(controller);
            found = true;
        }

        if (!found) {
            LOGGER.error("No IDebugController implementation found!");
        }
    }

    /**
     * Sets up the game world.
     */
    private void setupGameWorld() {
        ServiceLoader.load(ILevelSPI.class).findFirst().ifPresent(spi -> spi.generateLevel(6,8, 10, 10));

        // We should consider renaming Scene to something like "GameScene"
        dk.sdu.sem.commonsystem.Scene activeScene = SceneManager.getInstance().getActiveScene();

        // Create player
        Optional<IPlayerFactory> playerFactoryOpt = ServiceLoader.load(IPlayerFactory.class).findFirst();
        if (playerFactoryOpt.isEmpty()) {
            throw new RuntimeException("No IPlayerFactory implementation found");
        }
        IPlayerFactory playerFactory = playerFactoryOpt.get();
        Entity player = playerFactory.create();

        activeScene.addEntity(player);
        activeScene.addPersistedEntity(player);

        //Enable to spawn enemy and items in start room
        boolean testing = false;

        if (testing)
            testSpawner(activeScene);

        IItemFactory itemFactory = ServiceLoader.load(IItemFactory.class).findFirst().orElse(null);
        if (itemFactory != null) {
            Entity item = itemFactory.createItemFromPool(new Vector2D(10 * GameConstants.TILE_SIZE, 13 * GameConstants.TILE_SIZE), "enemy");
            dk.sdu.sem.commonsystem.Scene.getActiveScene().addEntity(item);
        }


        LOGGER.debug("Game world setup complete with map, player, enemy, and items");
    }

    public void createGameView(Stage stage) {
        float baseWidth = GameConstants.WORLD_SIZE.x() * GameConstants.TILE_SIZE;
        float baseHeight = GameConstants.WORLD_SIZE.y() * GameConstants.TILE_SIZE;

        canvas = new Canvas(baseWidth, baseHeight);
        Group canvasGroup = new Group(canvas);
        root = new StackPane(canvasGroup);
        root.setStyle("-fx-background-color: black;");

        Scene gameScene = new Scene(root, baseWidth, baseHeight);
        gameScene.setCursor(Cursor.NONE);

        // Bind scale properties while maintaining aspect ratio
        canvas.scaleXProperty().bind(Bindings.createDoubleBinding(
                () -> Math.min(gameScene.getWidth() / baseWidth, gameScene.getHeight() / baseHeight),
                gameScene.widthProperty(), gameScene.heightProperty()
        ));
        canvas.scaleYProperty().bind(canvas.scaleXProperty()); // Keep proportions

        // Center the canvas dynamically
        canvas.layoutXProperty().bind(gameScene.widthProperty().subtract(baseWidth).divide(2));
        canvas.layoutYProperty().bind(gameScene.heightProperty().subtract(baseHeight).divide(2));

        //This apparently helps performance, needs to be tested
        canvas.setCache(true);
        canvas.setCacheHint(CacheHint.SPEED);

        stage.setScene(gameScene);

        stage.show();
    }

    public void startGame(Stage stage) {
        try {
            this.stage = stage;
            createGameView(stage);

            setupInputs(canvas.getScene());

            // IMPORTANT: Init assets BEFORE creating any game entities
            initializeAssets();

            gameLoop = new GameLoop();
            gameLoop.start();

            // Get renderer
            GraphicsContext gc = canvas.getGraphicsContext2D();
            renderSystem = FXRenderSystem.getInstance();
            renderSystem.initialize(gc);

            // For rendering and UI
            renderLoop = new AnimationTimer() {
                private double lastNanoTime = System.nanoTime();

                @Override
                public void handle(long now) {
                    double deltaTime = (now - lastNanoTime) / 1_000_000_000.0;
                    lastNanoTime = now;

                    if (gameLoop == null || Time.getTimeScale() == 0)
                        return;

                    gameLoop.update(deltaTime);
                    if (gameLoop == null || Time.getTimeScale() == 0)
                        return;

                    gameLoop.lateUpdate();
                    if (gameLoop == null || Time.getTimeScale() == 0)
                        return;

                    renderSystem.lateUpdate(); // Not adhering to architecture, I know

                    gameLoop.guiUpdate(gc);

                    Input.update();
                }
            };

            menuManager = ServiceLoader.load(IMenuSPI.class).findFirst().orElse(null);
            if (menuManager != null) {
                menuManager.showMainMenu(stage);
                stopGame();
            }

            renderLoop.start();

        } catch (Throwable t) {
            LOGGER.error("Application start failed:");
            t.printStackTrace();
        }
    }