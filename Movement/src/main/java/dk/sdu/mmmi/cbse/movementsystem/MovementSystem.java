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
 * System that handles entity movement for non-physics entities.
 * Physics entities are handled by PhysicsSystem for consistent behavior.
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
            if (transform == null) continue;

            TagComponent tag = entity.getComponent(TagComponent.class);

            // Skip bullets - they're handled in fixedUpdate for smooth movement
            if (tag != null && tag.hasType(EntityType.BULLET)) {
                continue;
            }

            // Skip entities with physics components - they're handled by PhysicsSystem
            if (entity.hasComponent(PhysicsComponent.class)) {
                continue;
            }

            // Only process entities that have MovementComponent but no PhysicsComponent
            if (entity.hasComponent(MovementComponent.class)) {
                moveEntity(entity, transform, deltaTime);
            } else if (tag != null && tag.hasType(EntityType.ASTEROID)) {
                // Fallback: if asteroid doesn't have MovementComponent but no physics, basic movement!
                handleAsteroidFallback(entity, transform);
            }
        }
    }

    @Override
    public void fixedUpdate(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            TagComponent tag = entity.getComponent(TagComponent.class);

            // Only process bullets in fixed update (and only if they don't have physics)
            if (tag != null && tag.hasType(EntityType.BULLET) &&
                    !entity.hasComponent(PhysicsComponent.class)) {
                if (entity.hasComponent(MovementComponent.class)) {
                    moveEntity(entity, transform, FIXED_DELTA_TIME);
                }
            }
        }
    }

    /**
     * Handle movement for entities without physics components.
     */
    private void moveEntity(Entity entity, TransformComponent transform, float deltaTime) {
        MovementComponent movement = entity.getComponent(MovementComponent.class);

        // Skip player entities with physics (handled by PhysicsSystem)
        TagComponent tag = entity.getComponent(TagComponent.class);
        if (tag != null && tag.hasType(EntityType.PLAYER) &&
                entity.hasComponent(PhysicsComponent.class)) {
            return;
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
                // Player movement with physics is handled by PlayerSystem + PhysicsSystem
                // Player movement without physics is handled by PlayerSystem directly
                break;
        }

        // Apply rotation if specified
        if (Math.abs(movement.getRotationSpeed()) > 0.0001f) {
            transform.rotate(movement.getRotationSpeed() * deltaTime);
        }
    }

    /**
     * Fallback movement for asteroids without MovementComponent or PhysicsComponent
     */
    private void handleAsteroidFallback(Entity entity, TransformComponent transform) {
        LOGGER.log(Level.WARNING, "Asteroid {0} has no MovementComponent or PhysicsComponent, applying fallback movement",
                entity.getID());

        MovementComponent movement = new MovementComponent(MovementComponent.MovementPattern.LINEAR);
        movement.setSpeed(80.0f + random.nextFloat() * 40.0f); // Random speed between 80-120
        movement.setRotationSpeed(random.nextFloat() * 60.0f - 30.0f); // Random rotation ±30°/s
        entity.addComponent(movement);

        // Set random initial rotation if not set
        if (transform.getRotation() == 0) {
            transform.setRotation(random.nextFloat() * 360.0f);
        }

        LOGGER.log(Level.INFO, "Added fallback MovementComponent to asteroid {0} with speed {1}",
                new Object[]{entity.getID(), movement.getSpeed()});
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
     * Process random movement pattern
     */
    private void processRandomMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        long lastChange = movement.getLastDirectionChange();
        long currentTime = System.currentTimeMillis(); // ToDo: Should use Time.getTime()

        if (currentTime - lastChange > DIRECTION_CHANGE_DELAY) {
            if (random.nextFloat() < 0.2f) {
                float rotation = transform.getRotation();
                rotation += (random.nextFloat() * 60 - 30); // +/- 30 degrees
                transform.setRotation(rotation);
                movement.setLastDirectionChange(currentTime);

                LOGGER.log(Level.FINE, "Entity at {0} changed direction to {1}°",
                        new Object[]{transform.getPosition(), rotation});
            }
        }

        processLinearMovement(transform, movement, deltaTime);
    }
}