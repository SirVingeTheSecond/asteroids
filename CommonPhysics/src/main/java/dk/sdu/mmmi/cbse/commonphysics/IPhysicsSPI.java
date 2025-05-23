package dk.sdu.mmmi.cbse.commonphysics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Service Provider Interface for physics functionality.
 */
public interface IPhysicsSPI {

    /**
     * Apply a force to an entity over time
     * Force will be accumulated and applied during physics integration
     * @param entity Entity to apply force to
     * @param force Force vector to apply
     */
    void applyForce(Entity entity, Vector2D force);

    /**
     * Apply an impulse to an entity (immediate velocity change)
     * Impulse is applied immediately to the entity's velocity
     * @param entity Entity to apply impulse to
     * @param impulse Impulse vector to apply
     */
    void applyImpulse(Entity entity, Vector2D impulse);

    /**
     * Set the velocity of an entity directly
     * @param entity Entity to set velocity for
     * @param velocity New velocity vector
     */
    void setVelocity(Entity entity, Vector2D velocity);

    /**
     * Get the current velocity of an entity
     * @param entity Entity to get velocity from
     * @return Current velocity vector or Vector2D.zero() if no physics component
     */
    Vector2D getVelocity(Entity entity);

    /**
     * Apply angular force (torque) to an entity
     * @param entity Entity to apply torque to
     * @param torque Angular force to apply (degrees per second)
     */
    void applyTorque(Entity entity, float torque);

    /**
     * Set the angular velocity of an entity
     * @param entity Entity to set angular velocity for
     * @param angularVelocity New angular velocity (degrees per second)
     */
    void setAngularVelocity(Entity entity, float angularVelocity);

    /**
     * Get the current angular velocity of an entity
     * @param entity Entity to get angular velocity from
     * @return Current angular velocity in degrees per second
     */
    float getAngularVelocity(Entity entity);

    /**
     * Wake up a sleeping physics entity
     * @param entity Entity to wake up
     */
    void wakeUp(Entity entity);

    /**
     * Check if an entity has physics simulation enabled
     * @param entity Entity to check
     * @return true if entity has PhysicsComponent
     */
    boolean hasPhysics(Entity entity);
}