package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that handles movement for all entities.
 */
public class MovementSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(MovementSystem.class.getName());
    // ToDo: This might not be the smartest approach.
    private static final long DIRECTION_CHANGE_DELAY = 2000; // milliseconds

    private final Random random = new Random();


    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void process(GameData gameData, World world) {
        float deltaTime = (float) Time.getDeltaTime();

        for (Entity entity : world.getEntities()) {
            // Skip entities without required components
            if (!hasRequiredComponents(entity)) {
                continue;
            }

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            MovementComponent movement = entity.getComponent(MovementComponent.class);

            // Skip player entities (handled by PlayerControlSystem)
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                continue;
            }

            // Process based on movement pattern
            switch (movement.getPattern()) {
                case LINEAR:
                    processLinearMovement(transform, movement, deltaTime);
                    break;
                case RANDOM:
                    processRandomMovement(transform, movement, deltaTime);
                    break;
                case PLAYER:
                    break;
            }

            // Apply rotation - multiply by deltaTime to make sure we are independent of framerate
            if (Math.abs(movement.getRotationSpeed()) > 0.0001f) {
                transform.rotate(movement.getRotationSpeed() * deltaTime);
            }
        }
    }

    /**
     * Check if entity has required components for movement
     *
     * @param entity Entity to check
     * @return true if has required components
     */
    private boolean hasRequiredComponents(Entity entity) {
        return entity.hasComponent(TransformComponent.class) &&
                entity.hasComponent(MovementComponent.class);
    }

    /**
     * Process linear movement pattern
     *
     * @param transform Transform component
     * @param movement Movement component
     * @param deltaTime Time since last update
     */
    private void processLinearMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        if (movement.getSpeed() <= 0) {
            return;
        }

        Vector2D forward = transform.getForward();

        Vector2D velocity = forward.scale(movement.getSpeed() * deltaTime);

        transform.translate(velocity);
    }

    /**
     * Process random movement pattern
     *
     * @param transform Transform component
     * @param movement Movement component
     * @param deltaTime Time since last update
     */
    private void processRandomMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        // Check if it's time to change direction
        long lastChange = movement.getLastDirectionChange();
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastChange > DIRECTION_CHANGE_DELAY) {
            // Randomly adjust rotation between fixed time intervals
            if (random.nextFloat() < 0.2f) {
                float rotation = transform.getRotation();
                rotation += (random.nextFloat() * 60 - 30); // +/- 30 degrees
                transform.setRotation(rotation);
                movement.setLastDirectionChange(currentTime);

                LOGGER.log(Level.FINE, "Entity {0} changed direction to {1}",
                        new Object[]{transform.getPosition(), rotation});
            }
        }

        // Apply linear movement in current direction
        processLinearMovement(transform, movement, deltaTime);
    }
}