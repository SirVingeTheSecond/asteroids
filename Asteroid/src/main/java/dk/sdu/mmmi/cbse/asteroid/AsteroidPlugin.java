package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.common.utils.ServiceLocator;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for managing asteroids in the game.
 */
public class AsteroidPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(AsteroidPlugin.class.getName());

    private final IAsteroidSPI asteroidFactory;
    private final List<Entity> asteroids = new ArrayList<>();

    private static final int INITIAL_ASTEROID_COUNT = 4;

    /**
     * Create a new asteroid plugin, retrieving the IAsteroidSPI implementation
     */
    public AsteroidPlugin() {
        this.asteroidFactory = ServiceLocator.getService(IAsteroidSPI.class);
        LOGGER.log(Level.INFO, "AsteroidPlugin initialized with factory: {0}",
                asteroidFactory.getClass().getName());
    }

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "AsteroidPlugin.start() called - spawning {0} asteroids",
                INITIAL_ASTEROID_COUNT);

        // Create initial asteroids
        for (int i = 0; i < INITIAL_ASTEROID_COUNT; i++) {
            Entity asteroid = asteroidFactory.createAsteroid(gameData, world);
            world.addEntity(asteroid);
            asteroids.add(asteroid);
            LOGGER.log(Level.FINE, "Asteroid {0} created with ID: {1}", new Object[]{i, asteroid.getID()});
        }

        LOGGER.log(Level.INFO, "World now contains {0} entities", world.getEntities().size());
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "AsteroidPlugin.stop() called - removing all asteroids");

        // Remove tracked asteroids
        for (Entity asteroid : asteroids) {
            world.removeEntity(asteroid);
        }
        asteroids.clear();

        // Force remove any remaining asteroids (those created from splits)
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && tagComponent.hasType(EntityType.ASTEROID)) {
                world.removeEntity(entity);
                LOGGER.log(Level.FINE, "Removed untracked asteroid: {0}", entity.getID());
            }
        }

        LOGGER.log(Level.INFO, "All asteroids removed from world");
    }
}