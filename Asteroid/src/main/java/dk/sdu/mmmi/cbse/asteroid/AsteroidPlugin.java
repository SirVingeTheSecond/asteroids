package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IGamePluginService;

import java.util.ArrayList;
import java.util.List;

/**
 * Plugin for managing asteroids in the game.
 */
public class AsteroidPlugin implements IGamePluginService, IPluginLifecycle {
    private static final int ASTEROIDS_TO_SPAWN = 4;
    private final AsteroidFactory asteroidFactory;
    private final List<Entity> asteroids = new ArrayList<>(); // Changed from Asteroid to Entity

    public AsteroidPlugin() {
        this.asteroidFactory = new AsteroidFactory();
    }

    @Override
    public void start(GameData gameData, World world) {
        System.out.println("AsteroidPlugin.start() called - attempting to spawn " + ASTEROIDS_TO_SPAWN + " asteroids");

        // Spawn initial asteroids
        for (int i = 0; i < ASTEROIDS_TO_SPAWN; i++) {
            Entity asteroid = asteroidFactory.createEntity(gameData);
            world.addEntity(asteroid);
            asteroids.add(asteroid); // No cast needed
            System.out.println("Asteroid " + i + " created with ID: " + asteroid.getID());
        }

        System.out.println("World now contains " + world.getEntities().size() + " entities");
    }

    @Override
    public void stop(GameData gameData, World world) {
        System.out.println("AsteroidPlugin.stop() called - removing all asteroids");

        for (Entity asteroid : asteroids) {
            world.removeEntity(asteroid);
        }
        asteroids.clear();

        // Force remove all asteroids
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && tagComponent.hasTag(TagComponent.TAG_ASTEROID)) {
                world.removeEntity(entity);
            }
        }
    }
}