package dk.sdu.mmmi.cbse.physics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.ICollisionSPI;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System responsible for physics simulation.
 */
public class PhysicsSystem implements IUpdate, IFixedUpdate {
    private static final Logger LOGGER = Logger.getLogger(PhysicsSystem.class.getName());

    private ICollisionSPI collisionSPI;

    private static final float MIN_VELOCITY_THRESHOLD = 0.001f;
    private static final float COLLISION_STEP_SIZE = 0.1f;

    public PhysicsSystem() {
        this.collisionSPI = ServiceLoader.load(ICollisionSPI.class).findFirst().orElse(null);

        if (collisionSPI != null) {
            LOGGER.log(Level.INFO, "PhysicsSystem initialized with collision service");
        } else {
            LOGGER.log(Level.WARNING, "PhysicsSystem initialized without collision service - entities may pass through colliders");
        }
    }

    @Override
    public int getPriority() {
        return 90; // Run before movement system but after input
    }

    @Override
    public void fixedUpdate(GameData gameData, World world) {
        float fixedDeltaTime = 1.0f / 120.0f;

        List<Entity> physicsEntities = getPhysicsEntities(world);

        for (Entity entity : physicsEntities) {
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

            physics.updateSleepState(fixedDeltaTime);

            physics.applyAccumulatedForcesAndImpulses(fixedDeltaTime);

            physics.applyDrag(fixedDeltaTime);
        }
    }

    @Override
    public void update(GameData gameData, World world) {
        float deltaTime = gameData.getDeltaTime();

        List<Entity> physicsEntities = getPhysicsEntities(world);

        // Update positions based on velocity
        for (Entity entity : physicsEntities) {
            updateEntityPhysics(entity, deltaTime);
        }
    }

    /**
     * Update physics for a single entity
     * @param entity Entity to update
     * @param deltaTime Time since last update
     */
    private void updateEntityPhysics(Entity entity, float deltaTime) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);

        if (physics == null || transform == null) {
            return;
        }

        // Skip static entities and sleeping entities
        if (physics.getPhysicsType() == PhysicsComponent.PhysicsType.STATIC ||
                physics.isSleeping()) {
            return;
        }

        Vector2D velocity = physics.getVelocity();

        // Skip if velocity is negligible
        if (velocity.magnitudeSquared() < MIN_VELOCITY_THRESHOLD) {
            return;
        }

        // Calculate displacement
        Vector2D displacement = velocity.scale(deltaTime);

        // Handle collision detection if entity has collider
        if (entity.hasComponent(ColliderComponent.class) && collisionSPI != null) {
            moveWithCollisionDetection(entity, transform, displacement);
        } else {
            // Move directly without collision detection
            Vector2D newPosition = transform.getPosition().add(displacement);
            transform.setPosition(newPosition);
        }

        // Update rotation based on angular velocity
        float angularVelocity = physics.getAngularVelocity();
        if (Math.abs(angularVelocity) > 0.1f) {
            float newRotation = transform.getRotation() + (angularVelocity * deltaTime);
            transform.setRotation(newRotation);
        }

        // ToDo: Is this for backward compatibility? No backwards compatibility!
        updateMovementComponent(entity, physics);
    }

    /**
     * Move entity with collision detection
     * Uses stepped movement to prevent tunneling through thin colliders
     * @param entity Entity to move
     * @param transform Entity's transform component
     * @param displacement Total displacement this frame
     */
    private void moveWithCollisionDetection(Entity entity, TransformComponent transform, Vector2D displacement) {
        Vector2D currentPosition = transform.getPosition();
        Vector2D targetPosition = currentPosition.add(displacement);

        if (isPositionValid(entity, targetPosition)) {
            transform.setPosition(targetPosition);
            return;
        }

        float steps = Math.max(1, displacement.magnitude() / COLLISION_STEP_SIZE);
        Vector2D stepDisplacement = displacement.scale(1.0f / steps);

        Vector2D testPosition = currentPosition;

        for (int i = 0; i < steps; i++) {
            Vector2D nextPosition = testPosition.add(stepDisplacement);

            if (isPositionValid(entity, nextPosition)) {
                testPosition = nextPosition;
            } else {
                if (tryAxisSeparatedMovement(entity, testPosition, stepDisplacement)) {
                    // Successfully moved in at least one axis
                    break;
                } else {
                    PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
                    if (physics != null) {
                        physics.setVelocity(new Vector2D(0, 0));
                    }
                    break;
                }
            }
        }

        if (!testPosition.equals(currentPosition)) {
            transform.setPosition(testPosition);
        }
    }

    /**
     * Try to move along individual axes when direct movement is blocked
     * @param entity Entity to move
     * @param currentPosition Current position
     * @param displacement Attempted displacement
     * @return true if movement succeeded in at least one axis
     */
    private boolean tryAxisSeparatedMovement(Entity entity, Vector2D currentPosition, Vector2D displacement) {
        boolean movedX = false;
        boolean movedY = false;
        Vector2D finalPosition = currentPosition;

        // Try X-axis movement
        if (Math.abs(displacement.x()) > MIN_VELOCITY_THRESHOLD) {
            Vector2D xPosition = currentPosition.add(new Vector2D(displacement.x(), 0));
            if (isPositionValid(entity, xPosition)) {
                finalPosition = xPosition;
                movedX = true;
            } else {
                // X movement blocked, reduce X velocity
                reduceVelocityComponent(entity, true, false);
            }
        }

        // Try Y-axis movement from the new X position
        if (Math.abs(displacement.y()) > MIN_VELOCITY_THRESHOLD) {
            Vector2D yPosition = finalPosition.add(new Vector2D(0, displacement.y()));
            if (isPositionValid(entity, yPosition)) {
                finalPosition = yPosition;
                movedY = true;
            } else {
                // Y movement blocked, reduce Y velocity
                reduceVelocityComponent(entity, false, true);
            }
        }

        // Update position if we moved in any direction
        if (movedX || movedY) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform != null) {
                transform.setPosition(finalPosition);
            }
        }

        return movedX || movedY;
    }

    /**
     * Reduce velocity components when blocked by collision
     * @param entity Entity to modify
     * @param reduceX Whether to reduce X velocity
     * @param reduceY Whether to reduce Y velocity
     */
    private void reduceVelocityComponent(Entity entity, boolean reduceX, boolean reduceY) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            return;
        }

        Vector2D velocity = physics.getVelocity();
        float newX = reduceX ? 0 : velocity.x();
        float newY = reduceY ? 0 : velocity.y();

        physics.setVelocity(new Vector2D(newX, newY));
    }

    /**
     * Check if a position is valid for an entity (no collision)
     * @param entity Entity to check
     * @param position Position to test
     * @return true if position is valid
     */
    private boolean isPositionValid(Entity entity, Vector2D position) {
        if (collisionSPI == null) {
            return true;
        }

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            return true;
        }

        Vector2D originalPosition = transform.getPosition();

        // Temporarily move entity to test position
        transform.setPosition(position);

        // Check for collisions
        boolean valid = true;
        // ToDo: Implement based on the collision system
        // For now, assume the position is valid
        // You might want to extend ICollisionSPI to include position validation

        // Restore original position
        transform.setPosition(originalPosition);

        return valid;
    }

    /**
     * Update MovementComponent to maintain compatibility with existing systems
     * @param entity Entity to update
     * @param physics Physics component
     */
    private void updateMovementComponent(Entity entity, PhysicsComponent physics) {
        MovementComponent movement = entity.getComponent(MovementComponent.class);
        if (movement == null) {
            return;
        }

        float currentSpeed = physics.getVelocity().magnitude();
        movement.setSpeed(currentSpeed);

        // Update acceleration state
        boolean isAccelerating = physics.getAccumulatedForces().magnitudeSquared() > 0.001f ||
                physics.getAccumulatedImpulses().magnitudeSquared() > 0.001f;
        movement.setAccelerating(isAccelerating);
    }

    /**
     * Get all entities with physics components
     * @param world Game world
     * @return List of entities with physics components
     */
    private List<Entity> getPhysicsEntities(World world) {
        List<Entity> physicsEntities = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            if (entity.hasComponent(PhysicsComponent.class) &&
                    entity.hasComponent(TransformComponent.class)) {
                physicsEntities.add(entity);
            }
        }

        return physicsEntities;
    }
}