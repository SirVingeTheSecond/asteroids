package dk.sdu.mmmi.cbse;

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
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System responsible for maintaining asteroid population based on difficulty scaling.
 * Spawns asteroids to maintain target count as difficulty increases.
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

    // Current target
    private int targetAsteroidCount = FALLBACK_TARGET_ASTEROIDS;
    private float currentDifficulty = 0.0f;

    public AsteroidSpawningSystem() {
        initializeServices();

        if (asteroidFactory == null) {
            LOGGER.log(Level.SEVERE, "IAsteroidSPI not found! Asteroid spawning will not work.");
        } else {
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem initialized with difficulty scaling support");
        }
    }

    /**
     * Initialize all services using layer-aware loading
     */
    private void initializeServices() {
        ModuleLayer currentLayer = this.getClass().getModule().getLayer();

        if (currentLayer != null) {
            // Load from current layer first
            asteroidFactory = ServiceLoader.load(currentLayer, IAsteroidSPI.class)
                    .findFirst()
                    .orElse(null);
            difficultyService = ServiceLoader.load(currentLayer, IDifficultyService.class)
                    .findFirst()
                    .orElse(null);
            eventService = ServiceLoader.load(currentLayer, IEventService.class)
                    .findFirst()
                    .orElse(null);
        }

        // Fallback to boot layer if services not found
        if (asteroidFactory == null) {
            asteroidFactory = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        }
        if (difficultyService == null) {
            difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
        }
        if (eventService == null) {
            eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        }

        LOGGER.log(Level.INFO, "Services loaded - Asteroid: {0}, Difficulty: {1}, Event: {2}",
                new Object[]{
                        asteroidFactory != null,
                        difficultyService != null,
                        eventService != null
                });

        initializeDifficultySystem();
    }

    /**
     * Initialize difficulty system and subscribe to events
     */
    private void initializeDifficultySystem() {
        if (difficultyService != null) {
            updateTargetAsteroidCount();
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem using difficulty scaling - target: {0} asteroids at difficulty {1}",
                    new Object[]{targetAsteroidCount, currentDifficulty});
        } else {
            targetAsteroidCount = FALLBACK_TARGET_ASTEROIDS;
            LOGGER.log(Level.WARNING, "IDifficultyService not available - using fallback target: {0} asteroids",
                    FALLBACK_TARGET_ASTEROIDS);
        }

        // Subscribe to events even without difficulty service initially
        if (eventService != null) {
            eventService.subscribe(DifficultyChangedEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSpawningSystem subscribed to DifficultyChangedEvent");
        } else {
            LOGGER.log(Level.SEVERE, "EventService not available - difficulty scaling disabled!");
        }
    }

    @Override
    public int getPriority() {
        return 85; // Run after main systems but before rendering
    }

    @Override
    public void update(GameData gameData, World world) {
        refreshServices();

        float deltaTime = Time.getDeltaTimeF();
        timeSinceLastSpawnCheck += deltaTime;

        float currentCheckInterval = (Time.getTime() < 60.0) ? 1.0f : SPAWN_CHECK_INTERVAL;

        if (timeSinceLastSpawnCheck >= currentCheckInterval) {
            timeSinceLastSpawnCheck = 0.0f;
            checkAndSpawnAsteroids(gameData, world);
        }
    }

    /**
     * Handle difficulty change events
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
            ModuleLayer currentLayer = this.getClass().getModule().getLayer();

            if (currentLayer != null) {
                asteroidFactory = ServiceLoader.load(currentLayer, IAsteroidSPI.class)
                        .findFirst()
                        .orElse(null);
            }

            if (asteroidFactory == null) {
                asteroidFactory = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
            }

            if (asteroidFactory != null) {
                LOGGER.log(Level.INFO, "IAsteroidSPI became available");
            }
        }

        if (difficultyService == null) {
            ModuleLayer currentLayer = this.getClass().getModule().getLayer();

            if (currentLayer != null) {
                difficultyService = ServiceLoader.load(currentLayer, IDifficultyService.class)
                        .findFirst()
                        .orElse(null);
            }

            if (difficultyService == null) {
                difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
            }

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
            LOGGER.log(Level.WARNING, "AsteroidFactory is null - cannot spawn asteroids");
            return;
        }

        int currentAsteroidCount = countAsteroids(world);
        int neededAsteroids = targetAsteroidCount - currentAsteroidCount;

        LOGGER.log(Level.INFO, "Asteroid Check - Current: {0}, Target: {1}, Needed: {2}, Difficulty: {3}",
                new Object[]{currentAsteroidCount, targetAsteroidCount, neededAsteroids, currentDifficulty});

        if (neededAsteroids > 0) {
            float currentTime = (float) Time.getTime();

            float dynamicInterval = Math.max(0.5f, MIN_SPAWN_INTERVAL - (currentDifficulty * 0.2f));

            if (currentTime - lastSpawnTime < dynamicInterval) {
                LOGGER.log(Level.FINE, "Spawn interval not met - waiting {0}s more",
                        (dynamicInterval - (currentTime - lastSpawnTime)));
                return;
            }

            Entity newAsteroid = asteroidFactory.createAsteroid(gameData, world);
            if (newAsteroid != null) {
                world.addEntity(newAsteroid);
                lastSpawnTime = currentTime;

                LOGGER.log(Level.INFO, "Spawned asteroid - Current: {0}/{1}, Difficulty: {2}",
                        new Object[]{currentAsteroidCount + 1, targetAsteroidCount, currentDifficulty});
            } else {
                LOGGER.log(Level.SEVERE, "CRITICAL: Asteroid factory returned null!");
            }
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