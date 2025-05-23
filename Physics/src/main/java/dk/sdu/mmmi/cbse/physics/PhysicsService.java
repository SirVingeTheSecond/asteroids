package dk.sdu.mmmi.cbse.physics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for physics functionality.
 */
public class PhysicsService implements IPhysicsSPI {
    private static final Logger LOGGER = Logger.getLogger(PhysicsService.class.getName());

    public PhysicsService() {
        LOGGER.log(Level.INFO, "PhysicsService initialized");
    }

    @Override
    public void applyForce(Entity entity, Vector2D force) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot apply force to entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        physics.addForce(force);

        LOGGER.log(Level.FINE, "Applied force {0} to entity {1}",
                new Object[]{force, entity.getID()});
    }

    @Override
    public void applyImpulse(Entity entity, Vector2D impulse) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot apply impulse to entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        physics.addImpulse(impulse);

        LOGGER.log(Level.FINE, "Applied impulse {0} to entity {1}",
                new Object[]{impulse, entity.getID()});
    }

    @Override
    public void setVelocity(Entity entity, Vector2D velocity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot set velocity for entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        physics.setVelocity(velocity);

        LOGGER.log(Level.FINE, "Set velocity {0} for entity {1}",
                new Object[]{velocity, entity.getID()});
    }

    @Override
    public Vector2D getVelocity(Entity entity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            return new Vector2D(0, 0);
        }

        return physics.getVelocity();
    }

    @Override
    public void applyTorque(Entity entity, float torque) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot apply torque to entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        // Apply torque as angular acceleration (simplified)
        // τ = I * α, assuming unit moment of inertia: α = τ
        float currentAngularVelocity = physics.getAngularVelocity();
        physics.setAngularVelocity(currentAngularVelocity + torque);

        LOGGER.log(Level.FINE, "Applied torque {0} to entity {1}",
                new Object[]{torque, entity.getID()});
    }

    @Override
    public void setAngularVelocity(Entity entity, float angularVelocity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot set angular velocity for entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        physics.setAngularVelocity(angularVelocity);

        LOGGER.log(Level.FINE, "Set angular velocity {0} for entity {1}",
                new Object[]{angularVelocity, entity.getID()});
    }

    @Override
    public float getAngularVelocity(Entity entity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            return 0.0f;
        }

        return physics.getAngularVelocity();
    }

    @Override
    public void wakeUp(Entity entity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            LOGGER.log(Level.WARNING, "Cannot wake up entity {0}: no PhysicsComponent",
                    entity.getID());
            return;
        }

        // Wake up by applying a tiny impulse
        physics.addImpulse(new Vector2D(0.001f, 0.001f));

        LOGGER.log(Level.FINE, "Woke up entity {0}", entity.getID());
    }

    @Override
    public boolean hasPhysics(Entity entity) {
        return entity.hasComponent(PhysicsComponent.class);
    }
}