package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.commoncollision.CollisionContext;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResult;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collision resolution system for resolving collisions.
 */
public class CollisionResolver {
    private static final Logger LOGGER = Logger.getLogger(CollisionResolver.class.getName());

    private final IEventService eventService;

    public CollisionResolver() {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "Generic CollisionResolver initialized with EventService: {0}",
                eventService != null ? "available" : "not available");
    }

    /**
     * Resolve all collisions using a generic, components-based approach
     */
    public List<Entity> resolveCollisions(List<Pair<Entity, Entity>> collisions, GameData gameData, World world) {
        List<Entity> entitiesToRemove = new ArrayList<>();
        CollisionContext context = new CollisionContext(gameData, world, eventService);

        for (Pair<Entity, Entity> collision : collisions) {
            Entity entity1 = collision.getFirst();
            Entity entity2 = collision.getSecond();

            CollisionResult result = resolveCollisionPair(entity1, entity2, context);

            if (!result.isEmpty()) {
                entitiesToRemove.addAll(result.getEntitiesToRemove());

                for (Runnable action : result.getActions()) {
                    try {
                        action.run();
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Error executing collision action", e);
                    }
                }

                LOGGER.log(Level.FINE, "Resolved collision between {0} and {1}",
                        new Object[]{entity1.getID(), entity2.getID()});

                if (result.shouldStopProcessing()) {
                    break;
                }
            }
        }

        return entitiesToRemove;
    }

    /**
     * Resolve collision between two entities using their collision response components
     */
    private CollisionResult resolveCollisionPair(Entity entity1, Entity entity2, CollisionContext context) {
        CollisionResult result = new CollisionResult();

        // Handle entity1's response to entity2
        CollisionResponseComponent response1 = entity1.getComponent(CollisionResponseComponent.class);
        if (response1 != null) {
            CollisionResult result1 = response1.handleCollision(entity1, entity2, context);
            result.combine(result1);

            if (response1.shouldStopOtherCollisions() || result1.shouldStopProcessing()) {
                return result.stopProcessing();
            }
        }

        // Handle entity2's response to entity1
        CollisionResponseComponent response2 = entity2.getComponent(CollisionResponseComponent.class);
        if (response2 != null) {
            CollisionResult result2 = response2.handleCollision(entity2, entity1, context);
            result.combine(result2);
        }

        return result;
    }
}