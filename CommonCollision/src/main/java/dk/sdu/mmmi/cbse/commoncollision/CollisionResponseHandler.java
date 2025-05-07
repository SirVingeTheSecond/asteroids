package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Functional interface for collision response handlers.
 * Used to implement custom collision responses.
 */
@FunctionalInterface
public interface CollisionResponseHandler {
    /**
     * Handle a collision between two entities
     * @param self The entity this handler belongs to
     * @param other The other entity in the collision
     * @param world The game world
     * @return true if collision was handled, false to continue checking
     */
    boolean onCollision(Entity self, Entity other, World world);
}