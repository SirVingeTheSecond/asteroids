package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.asteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.common.utils.ServiceLocator;
import dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing asteroid behavior.
 * Handles asteroid movement and responds to events for splitting.
 */
public class AsteroidSystem implements IProcessingService, IEventListener<AsteroidSplitEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidSystem.class.getName());

    private final IAsteroidSPI asteroidSplitter;
    private final IEventService eventService;
    private World world;

    public AsteroidSystem() {
        this.asteroidSplitter = ServiceLocator.getService(IAsteroidSPI.class);
        this.eventService = ServiceLocator.getService(IEventService.class);

        // Register event
        eventService.subscribe(AsteroidSplitEvent.class, this);

        LOGGER.log(Level.INFO, "AsteroidSystem initialized with splitter: {0}",
                asteroidSplitter.getClass().getName());
    }

    @Override
    public void process(GameData gameData, World world) {
        this.world = world;

        // Basic asteroid movement is handled by MovementSystem
        // Collision is handled by CollisionSystem

        // ToDo: Add rotation patterns for Asteroids.
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

        // Verify this is an asteroid
        TagComponent tag = asteroid.getComponent(TagComponent.class);
        if (tag == null || !tag.hasType(EntityType.ASTEROID)) {
            LOGGER.log(Level.WARNING, "Received split event for non-asteroid entity");
            return;
        }

        LOGGER.log(Level.FINE, "Processing asteroid split event for {0}", asteroid.getID());

        // Delegate to asteroid splitter
        asteroidSplitter.createSplitAsteroid(asteroid, world);
    }
}