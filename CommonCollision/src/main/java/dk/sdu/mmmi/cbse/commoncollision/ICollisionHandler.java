package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Interface for handling collisions between entities.
 */
@FunctionalInterface
public interface ICollisionHandler {
    /**
     * Handle a collision between two entities
     *
     * @param self The entity that owns this handler
     * @param other The other entity in the collision
     * @param context Additional context information
     * @return Result of the collision handling
     */
    CollisionResult handle(Entity self, Entity other, CollisionContext context);
}