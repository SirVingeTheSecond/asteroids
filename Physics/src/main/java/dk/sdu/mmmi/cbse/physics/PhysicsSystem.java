package dk.sdu.mmmi.cbse.physics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System handling physics behavior.
 */
public class PhysicsSystem implements IUpdate, IFixedUpdate {
    private static final Logger LOGGER = Logger.getLogger(PhysicsSystem.class.getName());

    public PhysicsSystem() {
        LOGGER.log(Level.INFO, "PhysicsSystem initialized - boundary collision handled by CollisionSystem");
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
     * Update physics integration for a single entity
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

        if (velocity.magnitudeSquared() < 0.001f) return;

        Vector2D displacement = velocity.scale(deltaTime);
        Vector2D currentPosition = transform.getPosition();
        Vector2D targetPosition = currentPosition.add(displacement);

        // Apply position directly - BoundarySystem will handle boundary collision
        transform.setPosition(targetPosition);

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