package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.asteroids.IAsteroidSplitter;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;

public class AsteroidSplitter implements IAsteroidSplitter {
    private final AsteroidFactory asteroidFactory;
    private static final int SPLIT_COUNT = 2;

    public AsteroidSplitter() {
        this.asteroidFactory = new AsteroidFactory();
    }

    @Override
    public void createSplitAsteroid(Entity asteroid, World world) {
        TagComponent tagComponent = asteroid.getComponent(TagComponent.class);

        if (tagComponent == null || !tagComponent.hasTag(TagComponent.TAG_ASTEROID)) {
            return;
        }

        if (!asteroid.hasComponent(AsteroidComponent.class) || !asteroid.hasComponent(TransformComponent.class)) {
            return;
        }

        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transformComponent = asteroid.getComponent(TransformComponent.class);

        // Check if asteroid is too small to split
        if (transformComponent.getRadius() < 10) {
            return;
        }

        // CHeck if asteroid has reached max splits
        if (asteroidComponent.getSplitCount() >= asteroidComponent.getMaxSplits()) {
            return;
        }

        Entity[] newAsteroids = asteroidFactory.createSplitAsteroids(asteroid, SPLIT_COUNT, null);

        for (Entity newAsteroid : newAsteroids) {
            world.addEntity(newAsteroid);
        }

        world.removeEntity(asteroid);
    }

}
