package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Movement system that handles all entities with MovementComponent.
 */
public class MovementSystem implements IUpdate, IFixedUpdate {
    private static final Logger LOGGER = Logger.getLogger(MovementSystem.class.getName());
    private final Random random = new Random();

    private static final float FIXED_DELTA_TIME = 1.0f / 120.0f;
    private static final long DIRECTION_CHANGE_DELAY = 2000; // milliseconds

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        float deltaTime = (float) Time.getDeltaTime();

        for (Entity entity : world.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null || !entity.hasComponent(MovementComponent.class)) {
                continue;
            }

            TagComponent tag = entity.getComponent(TagComponent.class);

            // Skip bullets - handled in fixedUpdate
            if (tag != null && tag.hasType(EntityType.BULLET)) {
                continue;
            }

            // Skip entities with physics - handled by PhysicsSystem
            if (entity.hasComponent(PhysicsComponent.class)) {
                continue;
            }

            // Skip player entities with physics
            if (tag != null && tag.hasType(EntityType.PLAYER) &&
                    entity.hasComponent(PhysicsComponent.class)) {
                continue;
            }

            moveEntity(entity, transform, deltaTime);
        }
    }

    @Override
    public void fixedUpdate(GameData gameData, World world) {
        // Handle bullet movement at fixed rate for smoothness
        for (Entity entity : world.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null || !entity.hasComponent(MovementComponent.class)) {
                continue;
            }

            TagComponent tag = entity.getComponent(TagComponent.class);

            // Only process bullets without physics
            if (tag != null && tag.hasType(EntityType.BULLET) &&
                    !entity.hasComponent(PhysicsComponent.class)) {
                moveEntity(entity, transform, FIXED_DELTA_TIME);
            }
        }
    }

    /**
     * Handle movement for entities with MovementComponent
     */
    private void moveEntity(Entity entity, TransformComponent transform, float deltaTime) {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        if (movement == null) {
            return;
        }

        switch (movement.getPattern()) {
            case LINEAR:
                processLinearMovement(transform, movement, deltaTime);
                break;
            case RANDOM:
                processRandomMovement(transform, movement, deltaTime);
                break;
            case PLAYER:
                // Player movement handled by PlayerSystem
                break;
        }

        if (Math.abs(movement.getRotationSpeed()) > 0.0001f) {
            transform.rotate(movement.getRotationSpeed() * deltaTime);
        }
    }

    /**
     * Process linear movement pattern
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
     * Process random movement pattern (for asteroids)
     */
    private void processRandomMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        long currentTime = System.currentTimeMillis();
        long lastChange = movement.getLastDirectionChange();

        // Occasionally change direction
        if (currentTime - lastChange > DIRECTION_CHANGE_DELAY && random.nextFloat() < 0.2f) {
            float currentRotation = transform.getRotation();
            float newRotation = currentRotation + (random.nextFloat() * 60 - 30); // +-30 degrees
            transform.setRotation(newRotation);
            movement.setLastDirectionChange(currentTime);

            LOGGER.log(Level.FINE, "Entity at {0} changed direction to {1}°",
                    new Object[]{transform.getPosition(), newRotation});
        }

        processLinearMovement(transform, movement, deltaTime);
    }
}