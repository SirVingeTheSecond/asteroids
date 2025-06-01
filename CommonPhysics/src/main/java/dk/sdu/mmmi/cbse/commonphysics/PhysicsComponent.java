package dk.sdu.mmmi.cbse.commonphysics;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for entity physics properties.
 */
public class PhysicsComponent implements IComponent {
    private Vector2D velocity = new Vector2D(0, 0);
    private Vector2D acceleration = new Vector2D(0, 0);
    private Vector2D accumulatedForces = new Vector2D(0, 0);
    private Vector2D accumulatedImpulses = new Vector2D(0, 0);

    private float mass = 1.0f;
    private float drag = 0.98f; // Velocity multiplier per frame (0.98 = 2% drag)
    private float maxSpeed = Float.MAX_VALUE;
    private float angularVelocity = 0.0f;
    private float angularDrag = 0.95f;

    private PhysicsType physicsType = PhysicsType.DYNAMIC;
    private boolean sleeping = false;
    private float sleepTimer = 0.0f;
    private static final float SLEEP_THRESHOLD = 0.01f;
    private static final float SLEEP_TIME = 1.0f; // Seconds before sleeping

    /**
     * Physics body types for simulation behavior
     */
    public enum PhysicsType {
        DYNAMIC,     // Affected by forces and collisions
        KINEMATIC,   // Moved by code, affects others but not affected by forces
        STATIC       // Immovable and unaffected by forces
    }

    /**
     * Create a physics components with default values
     */
    public PhysicsComponent() {
        // Using default values
    }

    /**
     * Create a physics components with specific physics type
     * @param physicsType The physics simulation type
     */
    public PhysicsComponent(PhysicsType physicsType) {
        this.physicsType = physicsType;

        // Configure defaults based on type
        switch (physicsType) {
            case STATIC:
                this.mass = Float.MAX_VALUE;
                this.drag = 1.0f; // No movement
                break;
            case KINEMATIC:
                this.mass = Float.MAX_VALUE;
                this.drag = 1.0f;
                break;
            case DYNAMIC:
                // Use default values
                break;
        }
    }

    /**
     * Get current velocity vector
     * @return Velocity as Vector2D
     */
    public Vector2D getVelocity() {
        return velocity;
    }

    /**
     * Set velocity vector
     * @param velocity New velocity
     */
    public void setVelocity(Vector2D velocity) {
        this.velocity = velocity;
        resetSleepTimer();
    }

    /**
     * Set velocity using separate x,y components
     * @param x X velocity components
     * @param y Y velocity components
     */
    public void setVelocity(float x, float y) {
        setVelocity(new Vector2D(x, y));
    }

    /**
     * Get current acceleration vector
     * @return Acceleration as Vector2D
     */
    public Vector2D getAcceleration() {
        return acceleration;
    }

    /**
     * Set acceleration vector
     * @param acceleration New acceleration
     */
    public void setAcceleration(Vector2D acceleration) {
        this.acceleration = acceleration;
    }

    /**
     * Add a force to be applied next physics update
     * Forces are accumulated and applied during physics integration
     * @param force Force vector to apply
     */
    public void addForce(Vector2D force) {
        if (physicsType == PhysicsType.STATIC) {
            return;
        }
        this.accumulatedForces = this.accumulatedForces.add(force);
        resetSleepTimer();
    }

    /**
     * Add an impulse (immediate velocity change)
     * Impulses are applied immediately to velocity
     * @param impulse Impulse vector to apply
     */
    public void addImpulse(Vector2D impulse) {
        if (physicsType == PhysicsType.STATIC) {
            return;
        }
        this.accumulatedImpulses = this.accumulatedImpulses.add(impulse);
        resetSleepTimer();
    }

    /**
     * Apply accumulated forces and impulses to velocity
     * Called by physics system during integration
     * @param deltaTime Time step for integration
     */
    public void applyAccumulatedForcesAndImpulses(float deltaTime) {
        if (physicsType == PhysicsType.STATIC || sleeping) {
            clearAccumulatedForces();
            return;
        }

        // Apply impulses directly to velocity (J = m * Δv, so Δv = J / m)
        if (accumulatedImpulses.magnitudeSquared() > 0.001f) {
            Vector2D impulseVelocity = accumulatedImpulses.scale(1.0f / mass);
            velocity = velocity.add(impulseVelocity);
        }

        // Apply forces to acceleration (F = ma, so a = F/m)
        if (accumulatedForces.magnitudeSquared() > 0.001f) {
            Vector2D forceAcceleration = accumulatedForces.scale(1.0f / mass);
            acceleration = acceleration.add(forceAcceleration);
        }

        // Integrate acceleration into velocity (v = v0 + a*t)
        if (acceleration.magnitudeSquared() > 0.001f) {
            Vector2D velocityChange = acceleration.scale(deltaTime);
            velocity = velocity.add(velocityChange);
        }

        // Clear accumulated forces and impulses for next frame
        clearAccumulatedForces();

        // Apply speed limit
        if (velocity.magnitudeSquared() > maxSpeed * maxSpeed) {
            velocity = velocity.normalize().scale(maxSpeed);
        }
    }

    /**
     * Clear accumulated forces and impulses
     */
    private void clearAccumulatedForces() {
        accumulatedForces = new Vector2D(0, 0);
        accumulatedImpulses = new Vector2D(0, 0);
        acceleration = new Vector2D(0, 0);
    }

    /**
     * Apply drag/friction to velocity
     * @param deltaTime Time step
     */
    public void applyDrag(float deltaTime) {
        if (physicsType == PhysicsType.STATIC || sleeping) {
            return;
        }

        // Apply drag as velocity multiplier (exponential decay)
        float dragFactor = (float) Math.pow(drag, deltaTime * 60.0f); // Normalize for 60 FPS
        velocity = velocity.scale(dragFactor);

        // Apply angular drag
        float angularDragFactor = (float) Math.pow(angularDrag, deltaTime * 60.0f);
        angularVelocity *= angularDragFactor;

        // Stop very small velocities to prevent jitter
        if (velocity.magnitudeSquared() < 0.01f) {
            velocity = new Vector2D(0, 0);
        }
        if (Math.abs(angularVelocity) < 0.1f) {
            angularVelocity = 0.0f;
        }
    }

    /**
     * Update sleep state based on velocity
     * @param deltaTime Time step
     */
    public void updateSleepState(float deltaTime) {
        boolean shouldSleep = velocity.magnitudeSquared() < SLEEP_THRESHOLD * SLEEP_THRESHOLD &&
                Math.abs(angularVelocity) < SLEEP_THRESHOLD;

        if (shouldSleep) {
            sleepTimer += deltaTime;
            if (sleepTimer >= SLEEP_TIME) {
                sleeping = true;
                velocity = new Vector2D(0, 0);
                angularVelocity = 0.0f;
            }
        } else {
            resetSleepTimer();
        }
    }

    /**
     * Reset sleep timer and wake up the entity
     */
    private void resetSleepTimer() {
        sleepTimer = 0.0f;
        sleeping = false;
    }

    // Getters and setters

    public float getMass() {
        return mass;
    }

    public void setMass(float mass) {
        if (mass <= 0) {
            throw new IllegalArgumentException("Mass must be positive");
        }
        this.mass = mass;
    }

    public float getDrag() {
        return drag;
    }

    public void setDrag(float drag) {
        this.drag = Math.max(0, Math.min(1, drag));
    }

    public float getMaxSpeed() {
        return maxSpeed;
    }

    public void setMaxSpeed(float maxSpeed) {
        this.maxSpeed = Math.max(0, maxSpeed);
    }

    public float getAngularVelocity() {
        return angularVelocity;
    }

    public void setAngularVelocity(float angularVelocity) {
        this.angularVelocity = angularVelocity;
        resetSleepTimer();
    }

    public float getAngularDrag() {
        return angularDrag;
    }

    public void setAngularDrag(float angularDrag) {
        this.angularDrag = Math.max(0, Math.min(1, angularDrag));
    }

    public PhysicsType getPhysicsType() {
        return physicsType;
    }

    public void setPhysicsType(PhysicsType physicsType) {
        this.physicsType = physicsType;
    }

    public boolean isSleeping() {
        return sleeping;
    }

    public Vector2D getAccumulatedForces() {
        return accumulatedForces;
    }

    public Vector2D getAccumulatedImpulses() {
        return accumulatedImpulses;
    }
}