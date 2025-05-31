package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.commonmovement.IMovementSPI;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for direct transform-based movement.
 */
public class MovementService implements IMovementSPI {
    private static final Logger LOGGER = Logger.getLogger(MovementService.class.getName());

    public MovementService() {
        LOGGER.log(Level.INFO, "MovementService initialized for direct transform movement");
    }

    @Override
    public void moveEntity(Entity entity, Vector2D direction, float speed, float deltaTime) {
        moveEntity(entity, direction, speed, 1.0f, deltaTime);
    }

    @Override
    public void moveEntity(Entity entity, Vector2D direction, float speed, float speedMultiplier, float deltaTime) {
        if (!canMove(entity)) {
            LOGGER.log(Level.WARNING, "Cannot move entity {0}: missing TransformComponent",
                    entity.getID());
            return;
        }

        if (direction.magnitudeSquared() < 0.001f) {
            return; // No movement
        }

        TransformComponent transform = entity.getComponent(TransformComponent.class);

        // Calculate velocity based on speed and direction
        float effectiveSpeed = speed * speedMultiplier;
        Vector2D velocity = direction.scale(effectiveSpeed * deltaTime);

        // Apply movement directly to transform
        transform.translate(velocity);

        LOGGER.log(Level.FINEST, "Moved entity {0} by {1} (speed: {2}, multiplier: {3})",
                new Object[]{entity.getID(), velocity, speed, speedMultiplier});
    }

    @Override
    public boolean canMove(Entity entity) {
        return entity.hasComponent(TransformComponent.class);
    }

    @Override
    public void stopMovement(Entity entity) {
        // For direct movement, there's no velocity to stop
        // Movement stops automatically when no input is provided
        LOGGER.log(Level.FINEST, "Stop movement called for entity {0} (direct movement has no velocity to stop)",
                entity.getID());
    }
}