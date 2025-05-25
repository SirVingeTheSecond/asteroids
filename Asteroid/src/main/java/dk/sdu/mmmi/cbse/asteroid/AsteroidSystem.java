package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing asteroid behavior and handling split events.
 */
public class AsteroidSystem implements IUpdate, IEventListener<AsteroidSplitEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidSystem.class.getName());

    private final IAsteroidSPI asteroidSplitter;
    private final IEventService eventService;
    private final IPhysicsSPI physicsSPI;
    private World world;

    public AsteroidSystem() {
        this.asteroidSplitter = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);

        if (eventService != null) {
            eventService.subscribe(AsteroidSplitEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSystem subscribed to AsteroidSplitEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available, AsteroidSystem won't receive split events");
        }

        LOGGER.log(Level.INFO, "AsteroidSystem initialized - Splitter: {0}, EventService: {1}, PhysicsSPI: {2}",
                new Object[]{asteroidSplitter != null, eventService != null, physicsSPI != null});
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        this.world = world;
        float deltaTime = Time.getDeltaTimeF();

        // Process all asteroids
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag == null || !tag.hasType(EntityType.ASTEROID)) {
                continue;
            }

            processAsteroid(entity, deltaTime);
        }
    }

    /**
     * Process individual asteroid behavior
     */
    private void processAsteroid(Entity asteroid, float deltaTime) {
        // Update flickering effect for damaged asteroids
        FlickerUtility.updateFlicker(asteroid, deltaTime);

        // Ensure asteroid has proper physics setup
        ensureAsteroidPhysics(asteroid);

        // Log debug information for diagnostic purposes
        logAsteroidDebugInfo(asteroid);
    }

    /**
     * Ensure asteroid has proper physics setup for movement
     */
    private void ensureAsteroidPhysics(Entity asteroid) {
        PhysicsComponent physics = asteroid.getComponent(PhysicsComponent.class);

        if (physics == null) {
            LOGGER.log(Level.WARNING, "Asteroid {0} missing PhysicsComponent, adding one", asteroid.getID());

            // Add missing physics component
            PhysicsComponent newPhysics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
            newPhysics.setMass(1.2f);
            newPhysics.setDrag(0.998f);
            newPhysics.setAngularDrag(0.999f);
            newPhysics.setMaxSpeed(300.0f);
            asteroid.addComponent(newPhysics);

            // Give it some initial velocity if it doesn't have any
            if (physicsSPI != null) {
                if (physicsSPI.getVelocity(asteroid).magnitudeSquared() < 1.0f) {
                    // Generate random velocity
                    float speed = 60.0f + (float)(Math.random() * 60.0f);
                    float angle = (float)(Math.random() * 360.0);
                    float radians = (float) Math.toRadians(angle);

                    dk.sdu.mmmi.cbse.common.Vector2D velocity = new dk.sdu.mmmi.cbse.common.Vector2D(
                            (float) Math.cos(radians) * speed,
                            (float) Math.sin(radians) * speed
                    );

                    physicsSPI.setVelocity(asteroid, velocity);
                    LOGGER.log(Level.INFO, "Added initial velocity to asteroid {0}: {1}",
                            new Object[]{asteroid.getID(), velocity.magnitude()});
                }
            }
        }
    }

    /**
     * Log debug information for asteroids
     */
    private void logAsteroidDebugInfo(Entity asteroid) {
        if (!LOGGER.isLoggable(Level.FINEST)) return;

        AsteroidComponent asteroidComp = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (asteroidComp != null && transform != null) {
            dk.sdu.mmmi.cbse.common.Vector2D velocity = dk.sdu.mmmi.cbse.common.Vector2D.zero();
            if (physicsSPI != null) {
                velocity = physicsSPI.getVelocity(asteroid);
            }

            LOGGER.log(Level.FINEST, "Asteroid {0}: size={1}, health={2}/{3}, splits={4}, pos={5}, vel={6}",
                    new Object[]{asteroid.getID(), asteroidComp.getSize(),
                            asteroidComp.getCurrentHealth(), asteroidComp.getMaxHealth(),
                            asteroidComp.getSplitCount(), transform.getPosition(), velocity.magnitude()});
        }
    }

    /**
     * Handle asteroid split events
     */
    @Override
    public void onEvent(AsteroidSplitEvent event) {
        if (world == null) {
            LOGGER.log(Level.WARNING, "Cannot process asteroid split: world not initialized");
            return;
        }

        Entity asteroid = event.source();
        LOGGER.log(Level.INFO, "Received AsteroidSplitEvent for asteroid: {0}", asteroid.getID());

        // Validate asteroid for splitting
        if (!validateAsteroidForSplitting(asteroid)) {
            return;
        }

        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        // Log pre-split state
        dk.sdu.mmmi.cbse.common.Vector2D velocity = dk.sdu.mmmi.cbse.common.Vector2D.zero();
        if (physicsSPI != null) {
            velocity = physicsSPI.getVelocity(asteroid);
        }

        LOGGER.log(Level.INFO, "Processing split - size: {0}, splitCount: {1}, health: {2}/{3}, velocity: {4}, position: {5}",
                new Object[]{asteroidComponent.getSize(), asteroidComponent.getSplitCount(),
                        asteroidComponent.getCurrentHealth(), asteroidComponent.getMaxHealth(),
                        velocity.magnitude(), transform.getPosition()});

        // Check if asteroid can be split further
        if (!canAsteroidSplit(asteroidComponent)) {
            LOGGER.log(Level.INFO, "Asteroid {0} cannot be split further", asteroid.getID());
            return;
        }

        // Perform the split
        if (asteroidSplitter != null) {
            try {
                int entitiesBeforeSplit = world.getEntities().size();
                int asteroidsBeforeSplit = countAsteroids(world);

                asteroidSplitter.createSplitAsteroid(asteroid, world);

                int entitiesAfterSplit = world.getEntities().size();
                int asteroidsAfterSplit = countAsteroids(world);
                int newEntities = entitiesAfterSplit - entitiesBeforeSplit;
                int newAsteroids = asteroidsAfterSplit - asteroidsBeforeSplit;

                LOGGER.log(Level.INFO, "Split completed - created {0} new entities ({1} asteroids), total entities: {2}, total asteroids: {3}",
                        new Object[]{newEntities, newAsteroids, entitiesAfterSplit, asteroidsAfterSplit});

                if (newAsteroids == 0) {
                    LOGGER.log(Level.WARNING, "Split event processed but no new asteroids were created!");
                }

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error splitting asteroid " + asteroid.getID(), e);
            }
        } else {
            LOGGER.log(Level.WARNING, "Cannot split asteroid: asteroidSplitter not available");
        }
    }

    /**
     * Count asteroids in the world for debugging
     */
    private int countAsteroids(World world) {
        int count = 0;
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.ASTEROID)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Validate that an asteroid is suitable for splitting
     */
    private boolean validateAsteroidForSplitting(Entity asteroid) {
        TagComponent tag = asteroid.getComponent(TagComponent.class);
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (tag == null || !tag.hasType(EntityType.ASTEROID)) {
            LOGGER.log(Level.WARNING, "Split event for non-asteroid entity: {0}", asteroid.getID());
            return false;
        }

        if (asteroidComponent == null) {
            LOGGER.log(Level.WARNING, "Asteroid {0} missing AsteroidComponent", asteroid.getID());
            return false;
        }

        if (transform == null) {
            LOGGER.log(Level.WARNING, "Asteroid {0} missing TransformComponent", asteroid.getID());
            return false;
        }

        return true;
    }

    /**
     * Check if an asteroid can be split based on game rules
     */
    private boolean canAsteroidSplit(AsteroidComponent asteroidComponent) {
        boolean canSplit = asteroidComponent.getSplitCount() < asteroidComponent.getMaxSplits() &&
                asteroidComponent.getSize() != AsteroidSize.SMALL;

        LOGGER.log(Level.FINE, "Can split check: splitCount={0}, maxSplits={1}, size={2}, result={3}",
                new Object[]{asteroidComponent.getSplitCount(), asteroidComponent.getMaxSplits(),
                        asteroidComponent.getSize(), canSplit});

        return canSplit;
    }
}