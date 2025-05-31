package dk.sdu.mmmi.cbse.commonmovement;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Service Provider Interface for movement functionality.
 * Provides direct transform-based movement when physics is not available.
 */
public interface IMovementSPI {

    /**
     * Move an entity based on input direction and speed
     *
     * @param entity Entity to move
     * @param direction Normalized movement direction vector
     * @param speed Movement speed in units per second
     * @param deltaTime Time elapsed since last frame
     */
    void moveEntity(Entity entity, Vector2D direction, float speed, float deltaTime);

    /**
     * Move an entity with a speed multiplier (for effects like recoil)
     *
     * @param entity Entity to move
     * @param direction Normalized movement direction vector
     * @param speed Base movement speed in units per second
     * @param speedMultiplier Speed multiplier (1.0 = normal, 0.5 = half speed)
     * @param deltaTime Time elapsed since last frame
     */
    void moveEntity(Entity entity, Vector2D direction, float speed, float speedMultiplier, float deltaTime);

    /**
     * Check if an entity can be moved by this service
     *
     * @param entity Entity to check
     * @return true if entity has required components for movement
     */
    boolean canMove(Entity entity);

    /**
     * Stop an entity's movement immediately
     *
     * @param entity Entity to stop
     */
    void stopMovement(Entity entity);
}