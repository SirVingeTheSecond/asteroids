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
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing asteroid behavior.
 */
public class AsteroidSystem implements IUpdate, IEventListener<AsteroidSplitEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidSystem.class.getName());

    private final IAsteroidSPI asteroidSplitter;
    private final IEventService eventService;
    private World world;

    public AsteroidSystem() {
        this.asteroidSplitter = ServiceLoader.load(IAsteroidSPI.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        if (eventService != null) {
            eventService.subscribe(AsteroidSplitEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSystem subscribed to AsteroidSplitEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available, AsteroidSystem won't receive split events");
        }

        LOGGER.log(Level.INFO, "AsteroidSystem initialized with splitter: {0}",
                asteroidSplitter != null ? asteroidSplitter.getClass().getName() : "not available");
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

        // Debug: Check if asteroid has flicker component and is flickering
        if (asteroid.hasComponent(dk.sdu.mmmi.cbse.common.components.FlickerComponent.class)) {
            dk.sdu.mmmi.cbse.common.components.FlickerComponent flicker =
                    asteroid.getComponent(dk.sdu.mmmi.cbse.common.components.FlickerComponent.class);
            if (flicker != null && flicker.isFlickering()) {
                LOGGER.log(Level.FINE, "Asteroid {0} is flickering - timer: {1}/{2}",
                        new Object[]{asteroid.getID(), flicker.getFlickerTimer(), flicker.getFlickerDuration()});
            }
        }

        // TODO: Add asteroid rotation logic here if desired
        // TransformComponent transform = asteroid.getComponent(TransformComponent.class);
        // MovementComponent movement = asteroid.getComponent(MovementComponent.class);
        // if (transform != null && movement != null) {
        //     transform.rotate(movement.getRotationSpeed() * deltaTime);
        // }
    }

    /**
     * Handle asteroid split events
     *
     * @param event The asteroid split event
     */
    @Override
    public void onEvent(AsteroidSplitEvent event) {
        if (world == null) {
            LOGGER.log(Level.WARNING, "Cannot process asteroid split: world not initialized");
            return;
        }

        Entity asteroid = event.source();

        TagComponent tag = asteroid.getComponent(TagComponent.class);
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        TransformComponent transform = asteroid.getComponent(TransformComponent.class);

        if (tag == null || !tag.hasType(EntityType.ASTEROID) || asteroidComponent == null || transform == null) {
            LOGGER.log(Level.WARNING, "Invalid asteroid entity for splitting");
            return;
        }

        // Check if asteroid can be split further
        if (asteroidComponent.getSplitCount() >= asteroidComponent.getMaxSplits() ||
                asteroidComponent.getSize() == AsteroidSize.SMALL) {
            LOGGER.log(Level.FINE, "Asteroid {0} cannot be split further", asteroid.getID());
            return;
        }

        LOGGER.log(Level.FINE, "Processing split for asteroid {0}, size: {1}, split count: {2}",
                new Object[]{asteroid.getID(), asteroidComponent.getSize(),
                        asteroidComponent.getSplitCount()});

        if (asteroidSplitter != null) {
            asteroidSplitter.createSplitAsteroid(asteroid, world);
        }
    }
}