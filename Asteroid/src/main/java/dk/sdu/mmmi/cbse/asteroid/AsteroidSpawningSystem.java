package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.commondifficulty.events.DifficultyChangedEvent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System responsible for maintaining asteroid population based on difficulty scaling.
 * Dynamically spawns asteroids to maintain target count as difficulty increases.
 */
public class AsteroidSpawningSystem implements IUpdate, IEventListener<DifficultyChangedEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidSpawningSystem.class.getName());

    private IAsteroidSPI asteroidFactory;
    private IDifficultyService difficultyService;
    private IEventService eventService;

    // Fallback configuration when difficulty service unavailable
    private static final int FALLBACK_TARGET_ASTEROIDS = 4;
    private static final float SPAWN_CHECK_INTERVAL = 2.0f; // Check every 2 seconds

    // Spawn timing and spacing
    private float timeSinceLastSpawnCheck = 0.0f;
    private float lastSpawnTime = 0.0f;
    private static final float MIN_SPAWN_INTERVAL = 1.0f; // Minimum 1 second between spawns

    // Current target (cached for performance)
    private int targetAsteroidCount = FALLBACK_TARGET_ASTEROIDS;
    private float currentDifficulty = 0.0f;

    public AsteroidSpawningSystem() {
        this.asteroidFactory = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        this.difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        initializeDifficultySystem();

        if (asteroidFactory == null) {
            LOGGER.log(Level.SEVERE, "IAsteroidSPI not found! Asteroid spawning will not work.");
        } else {
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem initialized with difficulty scaling support");
        }
    }

    /**
     * Initialize difficulty system and subscribe to events
     */
    private void initializeDifficultySystem() {
        if (difficultyService != null) {
            updateTargetAsteroidCount();
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem using difficulty scaling - target: {0} asteroids",
                    targetAsteroidCount);
        } else {
            targetAsteroidCount = FALLBACK_TARGET_ASTEROIDS;
            LOGGER.log(Level.WARNING, "IDifficultyService not available - using fallback target: {0} asteroids",
                    FALLBACK_TARGET_ASTEROIDS);
        }

        // Subscribe to difficulty change events
        if (eventService != null) {
            eventService.subscribe(DifficultyChangedEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem subscribed to DifficultyChangedEvent");
        }
    }

    @Override
    public int getPriority() {
        return 85; // Run after main systems but before rendering
    }

    @Override
    public void update(GameData gameData, World world) {
        // Refresh services if not available
        refreshServices();

        float deltaTime = gameData.getDeltaTime();
        timeSinceLastSpawnCheck += deltaTime;

        // Only check spawn conditions periodically to avoid excessive entity counting
        if (timeSinceLastSpawnCheck >= SPAWN_CHECK_INTERVAL) {
            timeSinceLastSpawnCheck = 0.0f;
            checkAndSpawnAsteroids(gameData, world);
        }
    }

    /**
     * Handle difficulty change events for real-time scaling
     */
    @Override
    public void onEvent(DifficultyChangedEvent event) {
        int previousTarget = targetAsteroidCount;
        updateTargetAsteroidCount();

        LOGGER.log(Level.INFO, "AsteroidSpawningSystem difficulty updated - Level: {0}, Target: {1} -> {2} asteroids",
                new Object[]{currentDifficulty, previousTarget, targetAsteroidCount});
    }

    /**
     * Update target asteroid count from difficulty service
     */
    private void updateTargetAsteroidCount() {
        if (difficultyService != null) {
            currentDifficulty = difficultyService.getCurrentDifficulty();
            targetAsteroidCount = difficultyService.getMaxAsteroidCount();
        }
    }

    /**
     * Refresh service references if they weren't available during initialization
     */
    private void refreshServices() {
        if (asteroidFactory == null) {
            asteroidFactory = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        }
        if (difficultyService == null) {
            difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
            if (difficultyService != null) {
                updateTargetAsteroidCount();
                LOGGER.log(Level.INFO, "IDifficultyService became available - switching to difficulty scaling");
            }
        }
    }

    /**
     * Check current asteroid count and spawn new ones if needed
     */
    private void checkAndSpawnAsteroids(GameData gameData, World world) {
        if (asteroidFactory == null) {
            return;
        }

        int currentAsteroidCount = countAsteroids(world);
        int neededAsteroids = targetAsteroidCount - currentAsteroidCount;

        if (neededAsteroids > 0) {
            // Respect minimum spawn interval to prevent flooding
            float currentTime = (float) dk.sdu.mmmi.cbse.core.utils.Time.getTime();
            if (currentTime - lastSpawnTime < MIN_SPAWN_INTERVAL) {
                return;
            }

            // Spawn only one asteroid per check to maintain steady flow
            Entity newAsteroid = asteroidFactory.createAsteroid(gameData, world);
            if (newAsteroid != null) {
                world.addEntity(newAsteroid);
                lastSpawnTime = currentTime;

                LOGGER.log(Level.INFO, "Spawned asteroid - Current: {0}/{1}, Difficulty: {2}",
                        new Object[]{currentAsteroidCount + 1, targetAsteroidCount, currentDifficulty});
            } else {
                LOGGER.log(Level.WARNING, "Failed to create asteroid via IAsteroidSPI");
            }
        } else if (neededAsteroids < 0) {
            // Too many asteroids (difficulty decreased)
            LOGGER.log(Level.FINE, "Asteroid count above target: {0}/{1} (will naturally decrease)",
                    new Object[]{currentAsteroidCount, targetAsteroidCount});
        } else {
            // Perfect count
            LOGGER.log(Level.FINEST, "Asteroid count optimal: {0}/{1}",
                    new Object[]{currentAsteroidCount, targetAsteroidCount});
        }
    }

    /**
     * Count current asteroids in the world
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
}