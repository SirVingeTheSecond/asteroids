package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.collision.CollisionGroup;
import dk.sdu.mmmi.cbse.common.collision.CollisionLayer;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.services.IEntityFactory;

import java.util.Random;

/**
 * Factory for creating asteroid entities.
 * Replaces the Asteroid subclass with component-based approach.
 */
public class AsteroidFactory implements IEntityFactory<Entity> {
    private static final float MIN_SPEED = 0.5f;
    private static final float MAX_SPEED = 1.5f;
    private static final float MIN_ROTATION_SPEED = -2.0f;
    private static final float MAX_ROTATION_SPEED = 2.0f;

    private final Random random = new Random();

    @Override
    public Entity createEntity(GameData gameData) {
        return createAsteroid(gameData, 0, null);
    }

    public Entity createAsteroid(GameData gameData, int splitCount, Entity parent) {
        Entity asteroid = new Entity();

        // Create transform component
        TransformComponent transform = new TransformComponent();
        float size = random.nextInt(10) + 20;

        // If this is a split asteroid, adjust size based on parent
        if (parent != null && parent.hasComponent(TransformComponent.class) &&
                parent.hasComponent(AsteroidComponent.class)) {

            TransformComponent parentTransform = parent.getComponent(TransformComponent.class);
            AsteroidComponent parentAsteroid = parent.getComponent(AsteroidComponent.class);

            size = parentTransform.getRadius() * parentAsteroid.getSplitSizeRatio();

            // Position slightly offset from parent
            double angle = random.nextDouble() * 2 * Math.PI;
            double offset = parentTransform.getRadius();
            transform.setX(parentTransform.getX() + Math.cos(angle) * offset);
            transform.setY(parentTransform.getY() + Math.sin(angle) * offset);
        } else {
            // Random starting position
            transform.setX(100 + random.nextDouble() * (gameData.getDisplayWidth() - 200));
            transform.setY(100 + random.nextDouble() * (gameData.getDisplayHeight() - 200));
        }

        transform.setRadius(size);
        transform.setPolygonCoordinates(generateAsteroidShape(size));
        transform.setRotation(random.nextInt(360));
        asteroid.addComponent(transform);

        // Add renderer component
        RendererComponent renderer = new RendererComponent();
        renderer.setStrokeColor(Color.LIGHTGRAY);
        renderer.setFillColor(Color.color(0.2, 0.2, 0.2, 0.5)); // Dark semi-transparent fill
        renderer.setStrokeWidth(1.0f);
        renderer.setRenderLayer(200); // Asteroids in middle layer
        asteroid.addComponent(renderer);

        // Create movement component
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.LINEAR);
        movement.setSpeed(MIN_SPEED + random.nextFloat() * (MAX_SPEED - MIN_SPEED));
        movement.setRotationSpeed(MIN_ROTATION_SPEED + random.nextFloat() * (MAX_ROTATION_SPEED - MIN_ROTATION_SPEED));

        // If this is a split asteroid with a parent, inherit some of parent's momentum
        if (parent != null && parent.hasComponent(MovementComponent.class)) {
            MovementComponent parentMovement = parent.getComponent(MovementComponent.class);
            movement.setSpeed(parentMovement.getSpeed() * (0.8f + random.nextFloat() * 0.4f));
        }

        asteroid.addComponent(movement);

        // Create asteroid component
        AsteroidComponent asteroidComponent = new AsteroidComponent();
        asteroidComponent.setSplitCount(splitCount);
        asteroid.addComponent(asteroidComponent);

        // Create collision component
        CollisionComponent collision = new CollisionComponent();
        collision.setLayer(CollisionLayer.OBSTACLE);
        collision.addGroup(CollisionGroup.SOLID);
        collision.addGroup(CollisionGroup.DESTRUCTIBLE);
        asteroid.addComponent(collision);

        // Add tag component
        asteroid.addComponent(new TagComponent(TagComponent.TAG_ASTEROID));

        return asteroid;
    }

    /**
     * Create multiple split asteroids from a parent asteroid
     * @param parent Parent asteroid being split
     * @param count Number of new asteroids to create
     * @param gameData Game data
     * @return Array of new asteroid entities
     */
    public Entity[] createSplitAsteroids(Entity parent, int count, GameData gameData) {
        if (!parent.hasComponent(AsteroidComponent.class) || !parent.hasComponent(TransformComponent.class)) {
            return new Entity[0];
        }

        AsteroidComponent asteroidComponent = parent.getComponent(AsteroidComponent.class);
        TransformComponent transform = parent.getComponent(TransformComponent.class);

        // Check if asteroid can be split
        if (asteroidComponent.getSplitCount() >= asteroidComponent.getMaxSplits() || transform.getRadius() < 10) {
            return new Entity[0];
        }

        // Create new split asteroids
        Entity[] newAsteroids = new Entity[count];
        for (int i = 0; i < count; i++) {
            newAsteroids[i] = createAsteroid(gameData, asteroidComponent.getSplitCount() + 1, parent);
        }

        return newAsteroids;
    }

    /**
     * Generate asteroid shape coordinates
     * @param size Base size of the asteroid
     * @return Array of coordinate pairs [x1,y1,x2,y2,...]
     */
    private double[] generateAsteroidShape(float size) {
        int vertices = 8;
        double[] shape = new double[vertices * 2]; // Creates an array with 16 elements (8 vertices * 2 coordinates)
        double angleStep = 360.0 / vertices;

        for (int i = 0; i < vertices; i++) {
            double angle = Math.toRadians(i * angleStep);
            // Randomize radius slightly for more natural shape
            double radius = size * (0.8 + random.nextDouble() * 0.4);

            // Store x,y coordinates in the array
            int index = i * 2;
            shape[index] = Math.cos(angle) * radius;       // x coordinate
            shape[index + 1] = Math.sin(angle) * radius;   // y coordinate
        }

        return shape;
    }
}