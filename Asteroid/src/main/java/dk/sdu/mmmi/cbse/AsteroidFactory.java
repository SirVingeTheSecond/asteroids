package dk.sdu.mmmi.cbse;

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

public class AsteroidFactory implements IAsteroidSPI {
    private static final Logger LOGGER = Logger.getLogger(AsteroidFactory.class.getName());

    private static final float MIN_INITIAL_SPEED = 60.0f;
    private static final float MAX_INITIAL_SPEED = 120.0f;
    private static final float MIN_ANGULAR_VELOCITY = -90.0f;
    private static final float MAX_ANGULAR_VELOCITY = 90.0f;

    private static final int NUM_SPLIT_ASTEROIDS = 2;
    private static final float SPLIT_VELOCITY_INHERITANCE = 0.6f;
    private static final float SEPARATION_IMPULSE_MIN = 120.0f;
    private static final float SEPARATION_IMPULSE_MAX = 200.0f;
    private static final float MIN_SEPARATION_DISTANCE = 30.0f;

    private static final float OFFSCREEN_SPAWN_MARGIN = 100.0f;
    private static final float APPROACH_SPEED_MIN = 40.0f;
    private static final float APPROACH_SPEED_MAX = 100.0f;

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
        Vector2D spawnPosition = generateOffScreenPosition(gameData);
        Vector2D approachVelocity = calculateApproachVelocity(spawnPosition, gameData);
        return createAsteroid(AsteroidSize.LARGE, 0, approachVelocity, spawnPosition);
    }

    @Override
    public void createSplitAsteroid(Entity asteroid, World world) {
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (asteroidComponent == null || transform == null) {
            LOGGER.log(Level.WARNING, "Cannot split asteroid: missing required components");
            return;
        }

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

    public void createSplitAsteroidWithTrajectory(Entity asteroid, Vector2D bulletVelocity,
                                                  Vector2D impactPoint, World world) {
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (asteroidComponent == null || transform == null) {
            LOGGER.log(Level.WARNING, "Cannot split asteroid: missing required components");
            return;
        }

        if (asteroidComponent.getSplitCount() >= asteroidComponent.getMaxSplits() ||
                asteroidComponent.getSize() == AsteroidSize.SMALL) {
            return;
        }

        AsteroidSize newSize = getNextSmallerSize(asteroidComponent.getSize());
        if (newSize == null) return;

        Vector2D parentVelocity = getAsteroidVelocity(asteroid);
        Vector2D actualImpactPoint = impactPoint != null ? impactPoint : transform.getPosition();

        LOGGER.log(Level.INFO, "Splitting asteroid with trajectory: bullet velocity {0}, impact at {1}",
                new Object[]{bulletVelocity != null ? bulletVelocity.magnitude() : "null", actualImpactPoint});

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

    private Vector2D generateOffScreenPosition(GameData gameData) {
        int side = random.nextInt(4);
        float width = gameData.getDisplayWidth();
        float height = gameData.getDisplayHeight();

        switch (side) {
            case 0:
                return new Vector2D(
                        random.nextFloat() * width,
                        -OFFSCREEN_SPAWN_MARGIN
                );
            case 1:
                return new Vector2D(
                        width + OFFSCREEN_SPAWN_MARGIN,
                        random.nextFloat() * height
                );
            case 2:
                return new Vector2D(
                        random.nextFloat() * width,
                        height + OFFSCREEN_SPAWN_MARGIN
                );
            default:
                return new Vector2D(
                        -OFFSCREEN_SPAWN_MARGIN,
                        random.nextFloat() * height
                );
        }
    }

    private Vector2D calculateApproachVelocity(Vector2D spawnPosition, GameData gameData) {
        Vector2D playAreaCenter = new Vector2D(
                gameData.getDisplayWidth() / 2.0f,
                gameData.getDisplayHeight() / 2.0f
        );

        Vector2D directionToCenter = playAreaCenter.subtract(spawnPosition).normalize();

        float approachSpeed = APPROACH_SPEED_MIN +
                random.nextFloat() * (APPROACH_SPEED_MAX - APPROACH_SPEED_MIN);

        float angleVariation = (random.nextFloat() - 0.5f) * 60.0f;
        float radians = (float) Math.toRadians(angleVariation);
        Vector2D rotatedDirection = directionToCenter.rotate(radians);

        return rotatedDirection.scale(approachSpeed);
    }

    private Vector2D[] calculateTrajectoryBasedDirections(Vector2D bulletVelocity) {
        if (bulletVelocity == null || bulletVelocity.magnitudeSquared() < 0.001f) {
            return new Vector2D[] {
                    new Vector2D(1, 0),
                    new Vector2D(-1, 0)
            };
        }

        float bulletHeading = (float) Math.atan2(bulletVelocity.y(), bulletVelocity.x());

        float perp1Angle = bulletHeading + (float) Math.PI / 2;
        float perp2Angle = bulletHeading - (float) Math.PI / 2;

        return new Vector2D[] {
                new Vector2D((float) Math.cos(perp1Angle), (float) Math.sin(perp1Angle)),
                new Vector2D((float) Math.cos(perp2Angle), (float) Math.sin(perp2Angle))
        };
    }

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

        applyTrajectoryBasedPhysics(asteroid, parentVelocity, splitDirection);

        return asteroid;
    }

    private void applyTrajectoryBasedPhysics(Entity asteroid, Vector2D parentVelocity, Vector2D splitDirection) {
        if (physicsSPI == null || !physicsSPI.hasPhysics(asteroid)) {
            LOGGER.log(Level.WARNING, "Cannot apply trajectory physics to asteroid {0}: PhysicsSPI unavailable",
                    asteroid.getID());
            return;
        }

        Vector2D inheritedVelocity = parentVelocity.scale(SPLIT_VELOCITY_INHERITANCE);

        float separationMagnitude = SEPARATION_IMPULSE_MIN +
                random.nextFloat() * (SEPARATION_IMPULSE_MAX - SEPARATION_IMPULSE_MIN);
        Vector2D trajectoryImpulse = splitDirection.scale(separationMagnitude);

        Vector2D totalVelocity = inheritedVelocity.add(trajectoryImpulse);
        physicsSPI.setVelocity(asteroid, totalVelocity);

        float angularVelocity = MIN_ANGULAR_VELOCITY +
                random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
        physicsSPI.setAngularVelocity(asteroid, angularVelocity);

        LOGGER.log(Level.FINE, "Applied trajectory physics - Velocity: {0}, Angular: {1}°/s",
                new Object[]{totalVelocity.magnitude(), angularVelocity});
    }

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

        applySplitPhysics(asteroid, parentVelocity, separationDirection);

        return asteroid;
    }

    private Vector2D calculateSplitPosition(TransformComponent parentTransform,
                                            Vector2D separationDirection, float newRadius) {
        Vector2D parentPos = parentTransform.getPosition();
        float parentRadius = parentTransform.getRadius();

        float separationDistance = Math.max(MIN_SEPARATION_DISTANCE,
                parentRadius + newRadius + 10.0f);

        Vector2D offset = separationDirection.scale(separationDistance);
        return parentPos.add(offset);
    }

    private void applySplitPhysics(Entity asteroid, Vector2D parentVelocity, Vector2D separationDirection) {
        if (physicsSPI == null || !physicsSPI.hasPhysics(asteroid)) {
            return;
        }

        Vector2D inheritedVelocity = parentVelocity.scale(SPLIT_VELOCITY_INHERITANCE);

        float separationMagnitude = SEPARATION_IMPULSE_MIN +
                random.nextFloat() * (SEPARATION_IMPULSE_MAX - SEPARATION_IMPULSE_MIN);
        Vector2D separationImpulse = separationDirection.scale(separationMagnitude);

        Vector2D totalVelocity = inheritedVelocity.add(separationImpulse);
        physicsSPI.setVelocity(asteroid, totalVelocity);

        float angularVelocity = MIN_ANGULAR_VELOCITY +
                random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
        physicsSPI.setAngularVelocity(asteroid, angularVelocity);
    }

    private Vector2D generateSeparationDirection(int pieceIndex, int totalPieces) {
        double baseAngle = (2.0 * Math.PI * pieceIndex) / totalPieces;

        double variation = (random.nextDouble() - 0.5) * Math.PI / 2.0;
        double finalAngle = baseAngle + variation;

        return new Vector2D(
                (float) Math.cos(finalAngle),
                (float) Math.sin(finalAngle)
        );
    }

    private Vector2D getAsteroidVelocity(Entity asteroid) {
        if (physicsSPI != null && physicsSPI.hasPhysics(asteroid)) {
            return physicsSPI.getVelocity(asteroid);
        }

        MovementComponent movement = asteroid.getComponent(MovementComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (movement != null && transform != null) {
            Vector2D forward = transform.getForward();
            return forward.scale(movement.getSpeed());
        }

        return new Vector2D(50.0f, 0.0f);
    }

    private AsteroidSize getNextSmallerSize(AsteroidSize currentSize) {
        switch (currentSize) {
            case LARGE: return AsteroidSize.MEDIUM;
            case MEDIUM: return AsteroidSize.SMALL;
            case SMALL: return null;
            default: return null;
        }
    }

    private PhysicsComponent createPhysicsComponent() {
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.2f);
        physics.setDrag(1f);
        physics.setAngularDrag(1f);
        physics.setMaxSpeed(500f);
        return physics;
    }

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

    private ColliderComponent createCollisionComponent() {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);
        return collider;
    }

    private CollisionResponseComponent createAsteroidCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        response.addHandler(EntityType.PLAYER, CollisionHandlers.createPlayerDamageHandler(1));

        response.addHandler(EntityType.ASTEROID, CollisionHandlers.ASTEROID_ASTEROID_COLLISION_HANDLER);

        return response;
    }

    private FlickerComponent createFlickerComponent() {
        FlickerComponent flicker = new FlickerComponent();
        flicker.setFlickerRate(8.0f);
        return flicker;
    }

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

    private double[] generateAsteroidShape(float size) {
        int vertices = 8;
        double[] shape = new double[vertices * 2];
        double angleStep = 360.0 / vertices;

        for (int i = 0; i < vertices; i++) {
            double angle = Math.toRadians(i * angleStep);
            double radius = size * (0.8 + random.nextDouble() * 0.4);

            int index = i * 2;
            shape[index] = Math.cos(angle) * radius;
            shape[index + 1] = Math.sin(angle) * radius;
        }

        return shape;
    }

    private Entity createAsteroid(AsteroidSize size, int splitCount, Vector2D initialVelocity, Vector2D spawnPosition) {
        float radius = calculateSizeForType(size);
        double[] shape = generateAsteroidShape(radius);

        Entity asteroid = EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(spawnPosition)
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

        if (physicsSPI != null && !initialVelocity.equals(Vector2D.zero())) {
            physicsSPI.setVelocity(asteroid, initialVelocity);

            float angularVelocity = MIN_ANGULAR_VELOCITY +
                    random.nextFloat() * (MAX_ANGULAR_VELOCITY - MIN_ANGULAR_VELOCITY);
            physicsSPI.setAngularVelocity(asteroid, angularVelocity);

            LOGGER.log(Level.FINE, "Created asteroid with velocity: {0}, angular: {1}°/s",
                    new Object[]{initialVelocity.magnitude(), angularVelocity});
        }

        return asteroid;
    }
}