package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Time;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;

import java.util.Random;

/**
 * System that handles movement for all entities.
 * Uses component filtering to process only relevant entities.
 */
public class MovementSystem implements IEntityProcessingService {
    private final Random random = new Random();
    private static final long DIRECTION_CHANGE_DELAY = 120; // frames

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
                case HOMING:
                    // Homing movement is handled by AI systems that set rotation directly
                    // Just apply linear movement based on current rotation
                    processLinearMovement(transform, movement, deltaTime);
                    break;
                case PLAYER:
                    // Player movement is handled by PlayerControlSystem
                    break;
            }

            // Apply rotation - multiply by deltaTime for framerate independence
            if (Math.abs(movement.getRotationSpeed()) > 0.0001f) {
                transform.rotate(movement.getRotationSpeed() * deltaTime);
            }
        }
    }

    /**
     * Check if entity has required components for movement
     */
    private boolean hasRequiredComponents(Entity entity) {
        return entity.hasComponent(TransformComponent.class) &&
                entity.hasComponent(MovementComponent.class);
    }

    /**
     * Process linear movement pattern
     */
    private void processLinearMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        if (movement.getSpeed() <= 0) {
            return; // No movement
        }

        // Get forward direction from transform
        Vector2D forward = transform.getForward();

        // Scale movement by speed and deltaTime
        Vector2D velocity = forward.scale(movement.getSpeed() * deltaTime);

        // Update position
        transform.translate(velocity);
    }

    /**
     * Process random movement pattern
     */
    private void processRandomMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        // Check if it's time to change direction
        long lastChange = movement.getLastDirectionChange();
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastChange > DIRECTION_CHANGE_DELAY) {
            // Randomly adjust rotation between fixed time intervals
            if (random.nextFloat() < 0.1f) {
                float rotation = transform.getRotation();
                rotation += (random.nextFloat() * 60 - 30) * deltaTime; // +/- 30 degrees
                transform.setRotation(rotation);
                movement.setLastDirectionChange(currentTime);
            }
        }

        // Apply linear movement in current direction
        processLinearMovement(transform, movement, deltaTime);
    }
}