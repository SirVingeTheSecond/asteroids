package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.collision.CollisionGroup;
import dk.sdu.mmmi.cbse.common.collision.CollisionPair;
import dk.sdu.mmmi.cbse.common.collision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.CollisionEvent;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;

import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collision resolution system.
 * Handles what happens when entities collide.
 */
public class CollisionResolver {
    private static final Logger LOGGER = Logger.getLogger(CollisionResolver.class.getName());
    private final IGameEventService eventService;

    /**
     * Create a new collision resolver
     * @param eventService Event service for publishing collision events
     */
    public CollisionResolver(IGameEventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Resolve all collisions detected by the system
     *
     * @param collisions Set of collision pairs to resolve
     * @param world Game world containing entities
     */
    public void resolveCollisions(Set<CollisionPair> collisions, World world) {
        for (CollisionPair pair : collisions) {
            resolveCollision(pair, world);
        }
    }

    /**
     * Resolve a specific collision between two entities
     *
     * @param pair The collision pair to resolve
     * @param world Game world containing entities
     */
    private void resolveCollision(CollisionPair pair, World world) {
        Entity entityA = pair.getEntityA();
        Entity entityB = pair.getEntityB();

        // Check if either entity has been removed
        if (!world.getEntities().contains(entityA) || !world.getEntities().contains(entityB)) {
            return;
        }

        // Check collision group compatibility
        if (!areGroupsCompatible(entityA, entityB)) {
            return;
        }

        // Publish collision event
        eventService.publish(new CollisionEvent(entityA, entityA, entityB));

        // Apply collision responses if present
        boolean handled = applyCollisionResponse(entityA, entityB, world);

        if (!handled) {
            applyCollisionResponse(entityB, entityA, world);
        }
    }

    /**
     * Apply collision response for an entity if it has a response component
     *
     * @param entity The entity to apply response for
     * @param other The other entity in the collision
     * @param world Game world containing entities
     * @return true if the collision was handled
     */
    private boolean applyCollisionResponse(Entity entity, Entity other, World world) {
        CollisionResponseComponent response = entity.getComponent(CollisionResponseComponent.class);
        if (response != null) {
            try {
                return response.handleCollision(entity, other, world);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Error handling collision response: " + e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * Check if the collision groups between entities are compatible
     *
     * @param entityA First entity
     * @param entityB Second entity
     * @return true if the entities can collide based on groups
     */
    private boolean areGroupsCompatible(Entity entityA, Entity entityB) {
        CollisionComponent ccA = entityA.getComponent(CollisionComponent.class);
        CollisionComponent ccB = entityB.getComponent(CollisionComponent.class);

        if (ccA == null || ccB == null) {
            return false;
        }

        // Check if either entity is in the FRIENDLY group and the other in the HOSTILE group
        boolean aIsFriendly = ccA.isInGroup(CollisionGroup.FRIENDLY);
        boolean bIsFriendly = ccB.isInGroup(CollisionGroup.FRIENDLY);
        boolean aIsHostile = ccA.isInGroup(CollisionGroup.HOSTILE);
        boolean bIsHostile = ccB.isInGroup(CollisionGroup.HOSTILE);

        // If both are friendly or both are hostile, they don't collide with each other
        if ((aIsFriendly && bIsFriendly) || (aIsHostile && bIsHostile)) {
            return false;
        }

        // If one is SOLID and the other is DESTRUCTIBLE, they collide
        boolean aIsSolid = ccA.isInGroup(CollisionGroup.SOLID);
        boolean bIsSolid = ccB.isInGroup(CollisionGroup.SOLID);
        boolean aIsDestructible = ccA.isInGroup(CollisionGroup.DESTRUCTIBLE);
        boolean bIsDestructible = ccB.isInGroup(CollisionGroup.DESTRUCTIBLE);

        if ((aIsSolid && bIsDestructible) || (bIsSolid && aIsDestructible)) {
            return true;
        }

        // POWERUP entities only collide with FRIENDLY entities
        boolean aIsPowerup = ccA.isInGroup(CollisionGroup.POWERUP);
        boolean bIsPowerup = ccB.isInGroup(CollisionGroup.POWERUP);

        if (aIsPowerup && !bIsFriendly) {
            return false;
        }

        if (bIsPowerup && !aIsFriendly) {
            return false;
        }

        // Default to allowing collision
        return true;
    }
}