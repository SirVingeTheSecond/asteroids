package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.collision.CollisionPair;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ICollisionService;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;
import dk.sdu.mmmi.cbse.common.services.IPostEntityProcessingService;
import dk.sdu.mmmi.cbse.common.util.ServiceLocator;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for collision detection and resolution.
 * Implements the ICollisionService interface.
 */
public class CollisionSystem implements IPostEntityProcessingService, ICollisionService {
    private static final Logger LOGGER = Logger.getLogger(CollisionSystem.class.getName());

    private final CollisionDetector detector;
    private final CollisionResolver resolver;
    private boolean debugEnabled = false;

    /**
     * Create a new collision system
     */
    public CollisionSystem() {
        // Initialize collision components
        this.detector = new CollisionDetector();

        // Load required services
        IGameEventService eventService = ServiceLocator.getService(IGameEventService.class);
        this.resolver = new CollisionResolver(eventService);
    }

    @Override
    public void process(GameData gameData, World world) {
        long startTime = System.nanoTime();

        // Clear the detector for this frame
        detector.clear();

        // Add all entities with collision components to the detector
        for (Entity entity : world.getEntities()) {
            CollisionComponent cc = entity.getComponent(CollisionComponent.class);
            if (cc != null && cc.isActive()) {
                detector.addEntity(entity);
            }
        }

        // Detect all collisions
        Set<CollisionPair> collisions = detector.detectCollisions();

        // Resolve the collisions
        resolver.resolveCollisions(collisions, world);

        long endTime = System.nanoTime();

        if (debugEnabled) {
            LOGGER.log(Level.INFO,
                    "Collision processing: {0} entities, {1} collisions, {2}ms",
                    new Object[]{
                            world.getEntities().size(),
                            collisions.size(),
                            (endTime - startTime) / 1_000_000.0
                    });
        }
    }

    @Override
    public void setDebugEnabled(boolean enabled) {
        this.debugEnabled = enabled;
    }

    @Override
    public boolean isDebugEnabled() {
        return debugEnabled;
    }
}