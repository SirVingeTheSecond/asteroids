package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
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
import dk.sdu.mmmi.cbse.commoncollision.CollisionHandlers;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating asteroid entities.
 */
public class AsteroidFactory implements IAsteroidSPI {
    private static final Logger LOGGER = Logger.getLogger(AsteroidFactory.class.getName());

    // Initial asteroid movement constants
    private static final float MIN_INITIAL_SPEED = 60.0f;
    private static final float MAX_INITIAL_SPEED = 120.0f;
    private static final float MIN_ANGULAR_VELOCITY = -90.0f;
    private static final float MAX_ANGULAR_VELOCITY = 90.0f;

    // Split physics constants
    private static final int NUM_SPLIT_ASTEROIDS = 2;
    private static final float SPLIT_VELOCITY_INHERITANCE = 0.6f; // Lower = dramatic splitting
    private static final float SEPARATION_IMPULSE_MIN = 120.0f;
    private static final float SEPARATION_IMPULSE_MAX = 200.0f;
    private static final float MIN_SEPARATION_DISTANCE = 30.0f;

    private final Random random = new Random();
    private final IPhysicsSPI physicsSPI;

    public AsteroidFactory() {
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        if (physicsSPI == null) {
            LOGGER.log(Level.WARNING, "PhysicsSPI not available - asteroids will use basic movement");
        }
        LOGGER.log(Level.INFO, "AsteroidFactory initialized");
    }

    @Override
    public Entity createAsteroid(GameData gameData, World world) {
        Vector2D randomVelocity = generateRandomVelocity();
        return createAsteroid(gameData, AsteroidSize.LARGE, 0, null, randomVelocity);
    }

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
            LOGGER.log(Level.FINE, "Asteroid cannot be split further: size={0}, splitCount={1}",
                    new Object[]{asteroidComponent.getSize(), asteroidComponent.getSplitCount()});
            return;
        }

        AsteroidSize newSize = getNextSmallerSize(asteroidComponent.getSize());
        if (newSize == null) return;

        Vector2D parentVelocity = getAsteroidVelocity(asteroid);

        LOGGER.log(Level.INFO, "Splitting asteroid {0}: {1} -> {2}, creating {3} pieces",
                new Object[]{asteroid.getID(), asteroidComponent.getSize(), newSize, NUM_SPLIT_ASTEROIDS});

        // Create split asteroids
        for (int i = 0; i < NUM_SPLIT_ASTEROIDS; i++) {
            Vector2D separationDirection = generateSeparationDirection(i, NUM_SPLIT_ASTEROIDS);
            Entity newAsteroid = createSplitPiece(
                    newSize,
                    asteroidComponent.getSplitCount() + 1,
                    asteroid,
                    parentVelocity,
                    separationDirection
            );

            world.addEntity(newAsteroid);
            LOGGER.log(Level.FINE, "Created split asteroid piece: {0} with separation: {1}",
                    new Object[]{newAsteroid.getID(), separationDirection});
        }
    }

    /**
     * Create split asteroids using bullet trajectory
     */
    public void createSplitAsteroidWithTrajectory(Entity asteroid, Vector2D bulletVelocity,
                                                  Vector2D impactPoint, World world) {
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

        AsteroidSize newSize = getNextSmallerSize(asteroidComponent.getSize());
        if (newSize == null) return;

        // Get parent velocity
        Vector2D parentVelocity = getAsteroidVelocity(asteroid);
        Vector2D actualImpactPoint = impactPoint != null ? impactPoint : transform.getPosition();

        LOGGER.log(Level.INFO, "Splitting asteroid with trajectory: bullet velocity {0}, impact at {1}",
                new Object[]{bulletVelocity != null ? bulletVelocity.magnitude() : "null", actualImpactPoint});

        // Create split pieces using bullet trajectory for perpendicular directions
        Vector2D[] splitDirections = calculateTrajectoryBasedDirections(bulletVelocity);

        for (int i = 0; i < NUM_SPLIT_ASTEROIDS && i < splitDirections.length; i++) {
            Entity newAsteroid = createTrajectoryBasedSplitPiece(
                    newSize,
                    asteroidComponent.getSplitCount() + 1,
                    asteroid,
                    parentVelocity,
                    splitDirections[i],
                    actualImpactPoint
            );

            world.addEntity(newAsteroid);
            LOGGER.log(Level.FINE, "Created trajectory-based split piece: {0} with direction: {1}",
                    new Object[]{newAsteroid.getID(), splitDirections[i]});
        }
    }

    /**
     * Calculate perpendicular directions based on bullet trajectory
     */
    private Vector2D[] calculateTrajectoryBasedDirections(Vector2D bulletVelocity) {
        if (bulletVelocity == null || bulletVelocity.magnitudeSquared() < 0.001f) {
            // Fallback to default directions if no bullet velocity
            return new Vector2D[] {
                    new Vector2D(1, 0),
                    new Vector2D(-1, 0)
            };
        }

        // Calculate bullet heading angle
        float bulletHeading = (float) Math.atan2(bulletVelocity.y(), bulletVelocity.x());

        // Create perpendicular directions (θ + 90° and θ - 90°)
        float perp1Angle = bulletHeading + (float) Math.PI / 2; // +90 degrees
        float perp2Angle = bulletHeading - (float) Math.PI / 2; // -90 degrees

        return new Vector2D[] {
                new Vector2D((float) Math.cos(perp1Angle), (float) Math.sin(perp1Angle)),
                new Vector2D((float) Math.cos(perp2Angle), (float) Math.sin(perp2Angle))
        };
    }

    /**
     * Create a trajectory-based split piece
     */
    private Entity createTrajectoryBasedSplitPiece(AsteroidSize size, int splitCount, Entity parent,
                                                   Vector2D parentVelocity, Vector2D splitDirection,
                                                   Vector2D impactPoint) {
        TransformComponent parentTransform = parent.getComponent(TransformComponent.class);

        float radius = calculateSizeForType(size);
        Vector2D position = calculateSplitPosition(parentTransform, splitDirection, radius);

        double[] shape = generateAsteroidShape(radius);

        Entity asteroid = EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(position)
                .withRotation(random.nextInt(360))
                .withRadius(radius)
                .withShape(shape)
                .with(createAsteroidComponent(size, splitCount))
                .with(createPhysicsComponent())
                .with(createRendererComponent(size))
                .with(createCollisionComponent())
                .with(createAsteroidCollisionResponse())
                .with(createFlickerComponent())
                .build();

        // Apply trajectory-based physics
        applyTrajectoryBasedPhysics(asteroid, parentVelocity, splitDirection);

        return asteroid;
    }

    /**
     * Apply physics based on bullet trajectory for realistic splitting
     */
    private void applyTrajectoryBasedPhysics(Entity asteroid, Vector2D parentVelocity, Vector2D splitDirection) {
        if (physicsSPI == null || !physicsSPI.hasPhysics(asteroid)) {
            LOGGER.log(Level.WARNING, "Cannot apply trajectory physics to asteroid {0}: PhysicsSPI unavailable",
                    asteroid.getID());
            return;
        }

        // Inherit reduced parent velocity
        Vector2D inheritedVelocity = parentVelocity.scale(SPLIT_VELOCITY_INHERITANCE);

        // Add perpendicular impulse based on bullet trajectory
        float separationMagnitude = SEPARATION_IMPULSE_MIN +
                random.nextFloat() * (SEPARATION_IMPULSE_MAX - SEPARATION_IMPULSE_MIN);
        Vector2D trajectoryImpulse = splitDirection.scale(separationMagnitude);

        // Combine velocities for splitting effect
        Vector2D totalVelocity = inheritedVelocity.add(trajectoryImpulse);
        physicsSPI.setVelocity(asteroid, totalVelocity);

        // Add random angular velocity
        float angularVelocity = MIN_ANGULAR_VELOCITY +
                random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
        physicsSPI.setAngularVelocity(asteroid, angularVelocity);

        LOGGER.log(Level.FINE, "Applied trajectory physics - Velocity: {0}, Angular: {1}°/s",
                new Object[]{totalVelocity.magnitude(), angularVelocity});
    }

    /**
     * Generate random velocity for initial asteroids
     */
    private Vector2D generateRandomVelocity() {
        float speed = MIN_INITIAL_SPEED + random.nextFloat() * (MAX_INITIAL_SPEED - MIN_INITIAL_SPEED);
        float angle = random.nextFloat() * 360.0f;
        float radians = (float) Math.toRadians(angle);

        return new Vector2D(
                (float) Math.cos(radians) * speed,
                (float) Math.sin(radians) * speed
        );
    }

    /**
     * Create a single split asteroid piece with standard physics (fallback)
     */
    private Entity createSplitPiece(AsteroidSize size, int splitCount, Entity parent,
                                    Vector2D parentVelocity, Vector2D separationDirection) {
        TransformComponent parentTransform = parent.getComponent(TransformComponent.class);

        float radius = calculateSizeForType(size);
        Vector2D position = calculateSplitPosition(parentTransform, separationDirection, radius);

        double[] shape = generateAsteroidShape(radius);

        Entity asteroid = EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(position)
                .withRotation(random.nextInt(360))
                .withRadius(radius)
                .withShape(shape)
                .with(createAsteroidComponent(size, splitCount))
                .with(createPhysicsComponent())
                .with(createRendererComponent(size))
                .with(createCollisionComponent())
                .with(createAsteroidCollisionResponse())
                .with(createFlickerComponent())
                .build();

        // Apply standard split physics
        applySplitPhysics(asteroid, parentVelocity, separationDirection);

        return asteroid;
    }

    /**
     * Calculate split position with proper separation
     */
    private Vector2D calculateSplitPosition(TransformComponent parentTransform,
                                            Vector2D separationDirection, float newRadius) {
        Vector2D parentPos = parentTransform.getPosition();
        float parentRadius = parentTransform.getRadius();

        // Calculate minimum separation distance to prevent overlap
        float separationDistance = Math.max(MIN_SEPARATION_DISTANCE,
                parentRadius + newRadius + 10.0f);

        // Apply separation direction
        Vector2D offset = separationDirection.scale(separationDistance);
        return parentPos.add(offset);
    }

    /**
     * Apply standard split physics (fallback method)
     */
    private void applySplitPhysics(Entity asteroid, Vector2D parentVelocity, Vector2D separationDirection) {
        if (physicsSPI == null || !physicsSPI.hasPhysics(asteroid)) {
            return;
        }

        // Inherit parent velocity
        Vector2D inheritedVelocity = parentVelocity.scale(SPLIT_VELOCITY_INHERITANCE);

        // Add separation impulse
        float separationMagnitude = SEPARATION_IMPULSE_MIN +
                random.nextFloat() * (SEPARATION_IMPULSE_MAX - SEPARATION_IMPULSE_MIN);
        Vector2D separationImpulse = separationDirection.scale(separationMagnitude);

        // Apply total velocity
        Vector2D totalVelocity = inheritedVelocity.add(separationImpulse);
        physicsSPI.setVelocity(asteroid, totalVelocity);

        // Add random angular velocity
        float angularVelocity = MIN_ANGULAR_VELOCITY +
                random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
        physicsSPI.setAngularVelocity(asteroid, angularVelocity);
    }

    /**
     * Generate separation direction for split pieces (standard method)
     */
    private Vector2D generateSeparationDirection(int pieceIndex, int totalPieces) {
        // Create evenly spaced directions around a circle
        double baseAngle = (2.0 * Math.PI * pieceIndex) / totalPieces;

        // Add random variation (+-45 degrees)
        double variation = (random.nextDouble() - 0.5) * Math.PI / 2.0;
        double finalAngle = baseAngle + variation;

        return new Vector2D(
                (float) Math.cos(finalAngle),
                (float) Math.sin(finalAngle)
        );
    }

    /**
     * Get asteroid velocity (from physics or fallback)
     */
    private Vector2D getAsteroidVelocity(Entity asteroid) {
        if (physicsSPI != null && physicsSPI.hasPhysics(asteroid)) {
            return physicsSPI.getVelocity(asteroid);
        }

        // Fallback: estimate velocity from movement component or return default
        MovementComponent movement = asteroid.getComponent(MovementComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (movement != null && transform != null) {
            Vector2D forward = transform.getForward();
            return forward.scale(movement.getSpeed());
        }

        // Return a small default velocity to ensure some separation
        return new Vector2D(50.0f, 0.0f);
    }

    /**
     * Get the next smaller asteroid size
     */
    private AsteroidSize getNextSmallerSize(AsteroidSize currentSize) {
        switch (currentSize) {
            case LARGE: return AsteroidSize.MEDIUM;
            case MEDIUM: return AsteroidSize.SMALL;
            case SMALL: return null; // Cannot split further
            default: return null;
        }
    }

    /**
     * Create physics component
     */
    private PhysicsComponent createPhysicsComponent() {
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.2f);
        physics.setDrag(1f);
        physics.setAngularDrag(1f);
        physics.setMaxSpeed(500f);
        return physics;
    }

    /**
     * Create Asteroid component for the specified size
     */
    private AsteroidComponent createAsteroidComponent(AsteroidSize size, int splitCount) {
        AsteroidComponent component = new AsteroidComponent(size);
        component.setSplitCount(splitCount);

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
     * Create renderer component for visualization
     */
    private RendererComponent createRendererComponent(AsteroidSize size) {
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(RenderLayer.OBSTACLE);

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

        renderer.setFilled(true);
        return renderer;
    }

    /**
     * Create collision component for Asteroid
     */
    private ColliderComponent createCollisionComponent() {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);
        return collider;
    }

    /**
     * Create collision response component for Asteroid
     */
    private CollisionResponseComponent createAsteroidCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        // Asteroids damage player on collision
        response.addHandler(EntityType.PLAYER, CollisionHandlers.createPlayerDamageHandler(1));

        // Asteroids bounce off other asteroids with realistic physics
        response.addHandler(EntityType.ASTEROID, CollisionHandlers.ASTEROID_ASTEROID_COLLISION_HANDLER);

        return response;
    }

    /**
     * Create flicker component for damage effect
     */
    private FlickerComponent createFlickerComponent() {
        FlickerComponent flicker = new FlickerComponent();
        flicker.setFlickerRate(8.0f);
        return flicker;
    }

    /**
     * Calculate size based on Asteroid type
     */
    private float calculateSizeForType(AsteroidSize size) {
        switch (size) {
            case LARGE:
                return 25.0f + random.nextFloat() * 10.0f;
            case MEDIUM:
                return 12.0f + random.nextFloat() * 6.0f;
            case SMALL:
                return 6.0f + random.nextFloat() * 4.0f;
            default:
                return 20.0f;
        }
    }

    /**
     * Generate Asteroid shape coordinates
     */
    private double[] generateAsteroidShape(float size) {
        int vertices = 8;
        double[] shape = new double[vertices * 2];
        double angleStep = 360.0 / vertices;

        for (int i = 0; i < vertices; i++) {
            double angle = Math.toRadians(i * angleStep);
            // Randomize radius slightly to vary shape
            double radius = size * (0.8 + random.nextDouble() * 0.4);

            int index = i * 2;
            shape[index] = Math.cos(angle) * radius;       // x coordinate
            shape[index + 1] = Math.sin(angle) * radius;   // y coordinate
        }

        return shape;
    }

    /**
     * Create Asteroid (used by split system)
     */
    private Entity createAsteroid(GameData gameData, AsteroidSize size, int splitCount,
                                  Entity parent, Vector2D initialVelocity) {
        float radius = calculateSizeForType(size);

        // Calculate position
        Vector2D position;
        if (parent != null && parent.hasComponent(TransformComponent.class)) {
            TransformComponent parentTransform = parent.getComponent(TransformComponent.class);
            // Small offset from parent for non-split creation
            double angle = random.nextDouble() * 2 * Math.PI;
            double offset = parentTransform.getRadius() + radius + 15.0f;
            float x = parentTransform.getX() + (float) (Math.cos(angle) * offset);
            float y = parentTransform.getY() + (float) (Math.sin(angle) * offset);
            position = new Vector2D(x, y);
        } else {
            // Random starting position for initial asteroids (away from center where player spawns)
            float centerX = gameData.getDisplayWidth() / 2.0f;
            float centerY = gameData.getDisplayHeight() / 2.0f;
            float minDistance = 150.0f; // Keep away from player spawn

            float angle = random.nextFloat() * 360.0f;
            float distance = minDistance + random.nextFloat() * 200.0f;
            float radians = (float) Math.toRadians(angle);

            float x = centerX + (float) Math.cos(radians) * distance;
            float y = centerY + (float) Math.sin(radians) * distance;

            // Clamp to screen bounds
            x = Math.max(radius, Math.min(gameData.getDisplayWidth() - radius, x));
            y = Math.max(radius, Math.min(gameData.getDisplayHeight() - radius, y));

            position = new Vector2D(x, y);
        }

        double[] shape = generateAsteroidShape(radius);

        Entity asteroid = EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(position)
                .withRotation(random.nextInt(360))
                .withRadius(radius)
                .withShape(shape)
                .with(createAsteroidComponent(size, splitCount))
                .with(createPhysicsComponent())
                .with(createRendererComponent(size))
                .with(createCollisionComponent())
                .with(createAsteroidCollisionResponse())
                .with(createFlickerComponent())
                .build();

        // Apply initial velocity using physics
        if (physicsSPI != null && !initialVelocity.equals(Vector2D.zero())) {
            physicsSPI.setVelocity(asteroid, initialVelocity);

            // Add random angular velocity for initial asteroids
            float angularVelocity = MIN_ANGULAR_VELOCITY +
                    random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
            physicsSPI.setAngularVelocity(asteroid, angularVelocity);

            LOGGER.log(Level.FINE, "Created asteroid with velocity: {0}, angular: {1}°/s",
                    new Object[]{initialVelocity.magnitude(), angularVelocity});
        }

        return asteroid;
    }
}