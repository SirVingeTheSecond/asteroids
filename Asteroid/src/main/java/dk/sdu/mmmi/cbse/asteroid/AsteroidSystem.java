package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.common.utils.ServiceLocator;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing asteroid behavior.
 */
public class AsteroidSystem implements IProcessingService, IEventListener<AsteroidSplitEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidSystem.class.getName());

    private final IAsteroidSPI asteroidSplitter;
    private final IEventService eventService;
    private World world;

    public AsteroidSystem() {
        this.asteroidSplitter = ServiceLocator.getService(IAsteroidSPI.class);
        this.eventService = ServiceLocator.getService(IEventService.class);

        // Register for asteroid split events
        if (eventService != null) {
            eventService.subscribe(AsteroidSplitEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSystem subscribed to AsteroidSplitEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available, AsteroidSystem won't receive split events");
        }

        LOGGER.log(Level.INFO, "AsteroidSystem initialized with splitter: {0}",
                asteroidSplitter.getClass().getName());
    }

    @Override
    public void process(GameData gameData, World world) {
        this.world = world;

        // ToDo: Add Asteroid rotation logic.
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

        asteroidSplitter.createSplitAsteroid(asteroid, world);
    }

    /**
     * Clean up resources when system is destroyed.
     * Call when module is stopped.
     */
    public void cleanup() {
        if (eventService != null) {
            eventService.unsubscribe(AsteroidSplitEvent.class, this);
            LOGGER.log(Level.INFO, "AsteroidSystem unsubscribed from events");
        }
    }
}