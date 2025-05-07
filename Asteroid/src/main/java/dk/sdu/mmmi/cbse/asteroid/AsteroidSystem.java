package dk.sdu.mmmi.cbse.asteroid;

import dk.sdu.mmmi.cbse.common.asteroids.IAsteroidSplitter;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.common.events.IGameEventListener;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;

import java.util.ServiceLoader;

public class AsteroidSystem implements IEntityProcessingService, IGameEventListener<AsteroidSplitEvent> {
    private final IGameEventService eventService;
    private final IAsteroidSplitter asteroidSplitter;
    private World world;

    public AsteroidSystem() {
        // Get the event service
        this.eventService = ServiceLoader.load(IGameEventService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IGameEventService implementation found"));

        this.asteroidSplitter = ServiceLoader.load(IAsteroidSplitter.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IAsteroidSplitter implementation found"));

        // Register asteroid split events
        eventService.addListener(AsteroidSplitEvent.class, this);
    }

    @Override
    public void process(GameData gameData, World world) {
        // Store reference to world for event handling
        this.world = world;

        // Asteroid-specific processing could be added here
        // Basic movement is handled by MovementSystem
        // Collision is handled by CollisionSystem
    }

    @Override
    public void onEvent(AsteroidSplitEvent event) {
        if (world == null) {
            return; // Not initialized yet
        }

        Entity asteroid = event.getSource();
        asteroidSplitter.createSplitAsteroid(asteroid, world);
    }
}