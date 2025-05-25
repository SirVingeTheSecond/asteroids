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
        LOGGER.log(Level.FINEST, "Applied force {0} to entity {1}",
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
        LOGGER.log(Level.FINEST, "Set velocity {0} for entity {1}",
                new Object[]{velocity, entity.getID()});
    }

    @Override
    public Vector2D getVelocity(Entity entity) {
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        if (physics == null) {
            return Vector2D.zero();
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

        // Convert torque to angular acceleration and apply as angular force
        float angularAcceleration = torque / physics.getMass();
        float currentAngularVel = physics.getAngularVelocity();
        physics.setAngularVelocity(currentAngularVel + angularAcceleration);

        LOGGER.log(Level.FINEST, "Applied torque {0} to entity {1}",
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
        LOGGER.log(Level.FINEST, "Set angular velocity {0}°/s for entity {1}",
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
            return;
        }

        // Wake up sleeping physics bodies by applying a tiny impulse
        if (physics.isSleeping()) {
            physics.addImpulse(new Vector2D(0.001f, 0.001f));
            LOGGER.log(Level.FINE, "Woke up sleeping entity {0}", entity.getID());
        }
    }

    @Override
    public boolean hasPhysics(Entity entity) {
        return entity.hasComponent(PhysicsComponent.class);
    }
}