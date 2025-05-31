package dk.sdu.mmmi.cbse;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for managing spawning of asteroids.
 */
public class AsteroidPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(AsteroidPlugin.class.getName());

    private final List<Entity> asteroids = new ArrayList<>();

    private IAsteroidSPI asteroidFactory;
    private IDifficultyService difficultyService;

    // Fallback configuration when difficulty service unavailable
    private static final int FALLBACK_INITIAL_ASTEROID_COUNT = 4;

    /**
     * Create a new asteroid plugin with difficulty service integration
     */
    public AsteroidPlugin() {
        this.asteroidFactory = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        this.difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);

        if (asteroidFactory == null) {
            LOGGER.log(Level.SEVERE, "IAsteroidSPI not found! Asteroid plugin will not work.");
        }

        if (difficultyService == null) {
            LOGGER.log(Level.WARNING, "IDifficultyService not available - using fallback asteroid count");
        }

        LOGGER.log(Level.INFO, "AsteroidPlugin initialized with difficulty scaling support");
    }

    /**
     * Lazy load the asteroid factory from the correct module layer
     */
    private IAsteroidSPI getAsteroidFactory() {
        if (asteroidFactory == null) {
            // Get the module layer that this class was loaded from
            ModuleLayer currentLayer = this.getClass().getModule().getLayer();

            if (currentLayer != null) {
                // Use layer-specific ServiceLoader
                asteroidFactory = ServiceLoader.load(currentLayer, IAsteroidSPI.class)
                        .findFirst()
                        .orElse(null);
                LOGGER.log(Level.INFO, "Loaded IAsteroidSPI from layer: {0}", currentLayer);
            } else {
                // Fallback to boot layer
                asteroidFactory = ServiceLoader.load(IAsteroidSPI.class)
                        .findFirst()
                        .orElse(null);
                LOGGER.log(Level.INFO, "Loaded IAsteroidSPI from boot layer (fallback)");
            }

            if (asteroidFactory == null) {
                LOGGER.log(Level.SEVERE, "Failed to load IAsteroidSPI from any layer!");
            }
        }
        return asteroidFactory;
    }

    @Override
    public void start(GameData gameData, World world) {
        if (asteroidFactory == null) {
            LOGGER.log(Level.SEVERE, "Cannot start AsteroidPlugin: IAsteroidSPI not available");
            return;
        }

        int initialAsteroidCount = calculateInitialAsteroidCount();

        LOGGER.log(Level.INFO, "AsteroidPlugin starting - spawning {0} initial asteroids (difficulty-based)",
                initialAsteroidCount);

        for (int i = 0; i < initialAsteroidCount; i++) {
            try {
                Entity asteroid = asteroidFactory.createAsteroid(gameData, world);
                if (asteroid != null) {
                    world.addEntity(asteroid);
                    asteroids.add(asteroid);
                    LOGGER.log(Level.FINE, "Initial asteroid {0} created with ID: {1}",
                            new Object[]{i + 1, asteroid.getID()});
                } else {
                    LOGGER.log(Level.WARNING, "Failed to create initial asteroid {0}", i + 1);
                }
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error creating initial asteroid " + (i + 1), e);
            }
        }

        LOGGER.log(Level.INFO, "Created {0} initial asteroids. World now contains {1} entities",
                new Object[]{asteroids.size(), world.getEntities().size()});

        // Log difficulty information if available
        if (difficultyService != null) {
            LOGGER.log(Level.INFO, "Initial difficulty: {0}, Max asteroids: {1}",
                    new Object[]{difficultyService.getCurrentDifficulty(),
                            difficultyService.getMaxAsteroidCount()});
        }
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "AsteroidPlugin stopping - removing all asteroids");

        // Remove tracked initial asteroids
        int removedInitial = 0;
        for (Entity asteroid : asteroids) {
            world.removeEntity(asteroid);
            removedInitial++;
        }
        asteroids.clear();

        // Remove any other asteroids that may have spawned
        List<Entity> remainingAsteroids = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && tagComponent.hasType(EntityType.ASTEROID)) {
                remainingAsteroids.add(entity);
            }
        }

        int removedSpawned = 0;
        for (Entity asteroid : remainingAsteroids) {
            world.removeEntity(asteroid);
            removedSpawned++;
            LOGGER.log(Level.FINE, "Removed spawned asteroid: {0}", asteroid.getID());
        }

        LOGGER.log(Level.INFO, "Removed {0} initial asteroids and {1} spawned asteroids (total: {2})",
                new Object[]{removedInitial, removedSpawned, removedInitial + removedSpawned});
    }

    /**
     * Calculate initial asteroid count based on difficulty service
     */
    private int calculateInitialAsteroidCount() {
        if (difficultyService != null) {
            try {
                int difficultyBasedCount = difficultyService.getMaxAsteroidCount();

                // For initial spawning, use the current target (starts at minimum difficulty)
                // The AsteroidSpawningSystem will handle increasing count as difficulty rises
                LOGGER.log(Level.INFO, "Using difficulty-based initial asteroid count: {0}", difficultyBasedCount);
                return difficultyBasedCount;

            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error getting difficulty-based asteroid count, using fallback", e);
            }
        }

        LOGGER.log(Level.INFO, "Using fallback initial asteroid count: {0}", FALLBACK_INITIAL_ASTEROID_COUNT);
        return FALLBACK_INITIAL_ASTEROID_COUNT;
    }
}