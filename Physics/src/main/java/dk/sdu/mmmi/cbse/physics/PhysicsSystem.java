package dk.sdu.mmmi.cbse.physics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Physics system focused on physics simulation.
 */
public class PhysicsSystem implements IUpdate, IFixedUpdate {
    private static final Logger LOGGER = Logger.getLogger(PhysicsSystem.class.getName());

    public PhysicsSystem() {
        LOGGER.log(Level.INFO, "PhysicsSystem initialized");
    }

    @Override
    public int getPriority() {
        return 90; // Run before collision system
    }

    @Override
    public void fixedUpdate(GameData gameData, World world) {
        float fixedDeltaTime = Time.getFixedDeltaTime();
        List<Entity> physicsEntities = getPhysicsEntities(world);

        for (Entity entity : physicsEntities) {
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

            // Update physics state
            physics.updateSleepState(fixedDeltaTime);
            physics.applyAccumulatedForcesAndImpulses(fixedDeltaTime);
            physics.applyDrag(fixedDeltaTime);
        }
    }

    @Override
    public void update(GameData gameData, World world) {
        float deltaTime = Time.getDeltaTimeF();
        List<Entity> physicsEntities = getPhysicsEntities(world);

        for (Entity entity : physicsEntities) {
            updateEntityPhysics(entity, deltaTime);
        }
    }

    /**
     * Update physics integration for a single entity with boundary collision resolution
     */
    private void updateEntityPhysics(Entity entity, float deltaTime) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);

        if (physics == null || transform == null) return;

        // Skip static entities and sleeping entities
        if (physics.getPhysicsType() == PhysicsComponent.PhysicsType.STATIC ||
                physics.isSleeping()) {
            return;
        }

        Vector2D velocity = physics.getVelocity();

        // Skip if velocity is negligible
        if (velocity.magnitudeSquared() < 0.001f) return;

        // Calculate intended displacement
        Vector2D displacement = velocity.scale(deltaTime);
        Vector2D currentPosition = transform.getPosition();
        Vector2D targetPosition = currentPosition.add(displacement);

        // Check if entity has collider and might hit boundaries
        if (entity.hasComponent(ColliderComponent.class)) {
            Vector2D validPosition = resolvePositionWithBoundaries(entity, targetPosition, currentPosition);
            transform.setPosition(validPosition);
        } else {
            // No collision resolution needed
            transform.setPosition(targetPosition);
        }

        // Update rotation based on angular velocity
        float angularVelocity = physics.getAngularVelocity();
        if (Math.abs(angularVelocity) > 0.1f) {
            float newRotation = transform.getRotation() + (angularVelocity * deltaTime);
            transform.setRotation(newRotation);
        }

        LOGGER.log(Level.FINEST, "Updated entity {0} - pos: {1}, velocity: {2}",
                new Object[]{entity.getID(), transform.getPosition(), velocity});
    }

    /**
     * Boundary collision resolution that prevents entities from going through walls.
     */
    private Vector2D resolvePositionWithBoundaries(Entity entity, Vector2D targetPosition, Vector2D currentPosition) {
        ColliderComponent collider = entity.getComponent(ColliderComponent.class);

        // Only resolve boundary collisions for entities that can collide with boundaries
        if (collider == null || collider.getLayer() == CollisionLayer.PLAYER_PROJECTILE ||
                collider.getLayer() == CollisionLayer.ENEMY_PROJECTILE) {
            return targetPosition; // Bullets pass through boundaries
        }

        // boundary collision: check if position would be out of screen bounds
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) return targetPosition;

        float radius = transform.getRadius();
        float x = targetPosition.x();
        float y = targetPosition.y();

        // Get screen bounds
        float minX = radius;
        float maxX = 800 - radius; // ToDo: Get from GameData
        float minY = radius;
        float maxY = 600 - radius; // ToDo: Get from GameData

        float clampedX = Math.max(minX, Math.min(maxX, x));
        float clampedY = Math.max(minY, Math.min(maxY, y));

        Vector2D clampedPosition = new Vector2D(clampedX, clampedY);

        // If position was clamped, adjust velocity to prevent accumulation against walls
        if (!clampedPosition.equals(targetPosition)) {
            PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
            if (physics != null) {
                Vector2D velocity = physics.getVelocity();

                // Zero out velocity components that would push into the boundary
                float newVelX = velocity.x();
                float newVelY = velocity.y();

                if (clampedX != x) newVelX = 0; // Hit horizontal boundary
                if (clampedY != y) newVelY = 0; // Hit vertical boundary

                physics.setVelocity(new Vector2D(newVelX, newVelY));
            }

            LOGGER.log(Level.FINE, "Entity {0} position clamped to boundaries", entity.getID());
        }

        return clampedPosition;
    }

    /**
     * Get all entities with physics components
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