package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Random;

import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import javafx.scene.paint.Color;

/**
 * Factory for creating asteroid entities.
 * Implements the IAsteroidSPI service interface.
 */
public class AsteroidFactory implements IAsteroidSPI {
    private static final Logger LOGGER = Logger.getLogger(AsteroidFactory.class.getName());

    private static final float MIN_SPEED = 30.0f;  // Units per second
    private static final float MAX_SPEED = 60.0f;  // Units per second
    private static final float MIN_ROTATION_SPEED = -50.0f;  // Degrees per second
    private static final float MAX_ROTATION_SPEED = 50.0f;   // Degrees per second
    private static final int NUM_SPLIT_ASTEROIDS = 2; // Number of asteroids to create when splitting

    private final Random random = new Random();

    /**
     * Create a new asteroid entity
     *
     * @param gameData Current game data
     * @param world The game world to add the asteroid to
     * @return New asteroid entity
     */
    @Override
    public Entity createAsteroid(GameData gameData, World world) {
        return createAsteroid(gameData, AsteroidSize.LARGE, 0, null);
    }

    /**
     * Create smaller asteroids from a parent asteroid when it's destroyed
     *
     * @param asteroid The parent asteroid being split
     * @param world The game world to add the new asteroids to
     */
    @Override
    public void createSplitAsteroid(Entity asteroid, World world) {
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (asteroidComponent == null || transform == null) {
            LOGGER.log(Level.WARNING, "Cannot split asteroid: missing required components");
            return;
        }

        // Check if asteroid can be split
        if (asteroidComponent.getSplitCount() >= asteroidComponent.getMaxSplits() ||
                asteroidComponent.getSize() == AsteroidSize.SMALL) {
            return;
        }

        // Determine new asteroid size
        AsteroidSize newSize;
        switch (asteroidComponent.getSize()) {
            case LARGE:
                newSize = AsteroidSize.MEDIUM;
                break;
            case MEDIUM:
                newSize = AsteroidSize.SMALL;
                break;
            default:
                return; // Can't split smaller than SMALL
        }

        // Create game data from the asteroid's current position
        GameData gameData = new GameData();
        gameData.setDisplayWidth(1000); // Use reasonable defaults
        gameData.setDisplayHeight(800);

        // Create new split asteroids
        for (int i = 0; i < NUM_SPLIT_ASTEROIDS; i++) {
            Entity newAsteroid = createAsteroid(
                    gameData,
                    newSize,
                    asteroidComponent.getSplitCount() + 1,
                    asteroid
            );
            world.addEntity(newAsteroid);
            LOGGER.log(Level.FINE, "Created split asteroid: {0}", newAsteroid.getID());
        }
    }

    /**
     * Create an asteroid with specific properties
     *
     * @param gameData Game data containing screen dimensions
     * @param size Asteroid size category
     * @param splitCount Number of times this asteroid has been split
     * @param parent Parent asteroid (if split from another)
     * @return New asteroid entity
     */
    private Entity createAsteroid(GameData gameData, AsteroidSize size, int splitCount, Entity parent) {
        // Calculate size based on asteroid category
        float radius = calculateSizeForType(size);

        // Calculate position - either random or based on parent
        float x, y;
        if (parent != null && parent.hasComponent(TransformComponent.class)) {
            TransformComponent parentTransform = parent.getComponent(TransformComponent.class);
            // Position slightly offset from parent
            double angle = random.nextDouble() * 2 * Math.PI;
            double offset = parentTransform.getRadius();
            x = parentTransform.getX() + (float) (Math.cos(angle) * offset);
            y = parentTransform.getY() + (float) (Math.sin(angle) * offset);
        } else {
            // Random starting position
            x = 100 + random.nextFloat() * (gameData.getDisplayWidth() - 200);
            y = 100 + random.nextFloat() * (gameData.getDisplayHeight() - 200);
        }

        // Calculate movement speed - can inherit from parent
        float speed = MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED);
        float rotationSpeed = MIN_ROTATION_SPEED + random.nextFloat() * (MAX_ROTATION_SPEED - MIN_ROTATION_SPEED);

        if (parent != null && parent.hasComponent(MovementComponent.class)) {
            MovementComponent parentMovement = parent.getComponent(MovementComponent.class);
            // Inherit some of parent's speed, with variation
            speed = parentMovement.getSpeed() * (0.8f + random.nextFloat() * 0.4f);
        }

        // Create asteroid shape
        double[] shape = generateAsteroidShape(radius);

        // Set up and return the new asteroid entity using EntityBuilder for clean composition
        return EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(x, y)
                .withRotation(random.nextInt(360))
                .withRadius(radius)
                .withShape(shape)
                .with(createAsteroidComponent(size, splitCount))
                .with(createMovementComponent(speed, rotationSpeed))
                .with(createRendererComponent(size))
                .with(createCollisionComponent())
                .build();
    }

    /**
     * Create asteroid component for the specified size
     */
    private AsteroidComponent createAsteroidComponent(AsteroidSize size, int splitCount) {
        AsteroidComponent component = new AsteroidComponent(size);
        component.setSplitCount(splitCount);

        // Set score value based on size - smaller asteroids are worth more
        switch (size) {
            case LARGE:
                component.setScoreValue(100);
                break;
            case MEDIUM:
                component.setScoreValue(150);
                break;
            case SMALL:
                component.setScoreValue(200);
                break;
        }

        return component;
    }

    /**
     * Create movement component with specified parameters
     */
    private MovementComponent createMovementComponent(float speed, float rotationSpeed) {
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.LINEAR);
        movement.setSpeed(speed);
        movement.setRotationSpeed(rotationSpeed);
        return movement;
    }

    /**
     * Create renderer component for asteroid visualization
     *
     * @param size Asteroid size
     * @return Configured RendererComponent
     */
    private RendererComponent createRendererComponent(AsteroidSize size) {
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(200); // Asteroids in middle layer

        // Configure visuals based on size
        switch (size) {
            case LARGE:
                renderer.setStrokeColor(Color.GRAY);
                renderer.setFillColor(Color.DARKGRAY);
                renderer.setStrokeWidth(2.0f);
                break;
            case MEDIUM:
                renderer.setStrokeColor(Color.LIGHTGRAY);
                renderer.setFillColor(Color.GRAY);
                renderer.setStrokeWidth(1.5f);
                break;
            case SMALL:
                renderer.setStrokeColor(Color.WHITE);
                renderer.setFillColor(Color.LIGHTGRAY);
                renderer.setStrokeWidth(1.0f);
                break;
        }

        return renderer;
    }

    /**
     * Create collision component for asteroid
     */
    private ColliderComponent createCollisionComponent() {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);
        return collider;
    }

    /**
     * Calculate size based on asteroid type
     *
     * @param size Asteroid size category
     * @return Radius in pixels
     */
    private float calculateSizeForType(AsteroidSize size) {
        switch (size) {
            case LARGE:
                return 30.0f + random.nextFloat() * 10.0f;
            case MEDIUM:
                return 15.0f + random.nextFloat() * 5.0f;
            case SMALL:
                return 7.0f + random.nextFloat() * 3.0f;
            default:
                return 20.0f;
        }
    }

    /**
     * Generate asteroid shape coordinates
     *
     * @param size Base size of the asteroid
     * @return Array of coordinate pairs [x1,y1,x2,y2,...]
     */
    private double[] generateAsteroidShape(float size) {
        int vertices = 8;
        double[] shape = new double[vertices * 2];
        double angleStep = 360.0 / vertices;

        for (int i = 0; i < vertices; i++) {
            double angle = Math.toRadians(i * angleStep);
            // Randomize radius slightly to vary shape
            double radius = size * (0.8 + random.nextDouble() * 0.4);

            // x,y coordinates in the array
            int index = i * 2;
            shape[index] = Math.cos(angle) * radius;       // x coordinate
            shape[index + 1] = Math.sin(angle) * radius;   // y coordinate
        }

        return shape;
    }
}