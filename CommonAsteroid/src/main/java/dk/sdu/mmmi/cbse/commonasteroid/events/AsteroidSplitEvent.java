package dk.sdu.mmmi.cbse.commonasteroid.events;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEvent;

/**
 * Event triggered when an asteroid is split.
 * Contains information about the bullet that caused the split.
 */
public class AsteroidSplitEvent implements IEvent {
    private final Entity source;
    private final Vector2D bulletVelocity;
    private final Vector2D impactPoint;

    /**
     * Create a new asteroid split event with bullet trajectory information
     *
     * @param source The asteroid entity that was split
     * @param bulletVelocity The velocity vector of the bullet that caused the split (can be null for non-bullet splits)
     * @param impactPoint The point where the impact occurred (can be null)
     */
    public AsteroidSplitEvent(Entity source, Vector2D bulletVelocity, Vector2D impactPoint) {
        if (source == null) {
            throw new IllegalArgumentException("Source entity cannot be null");
        }
        this.source = source;
        this.bulletVelocity = bulletVelocity;
        this.impactPoint = impactPoint;
    }

    /**
     * Create a new asteroid split event without bullet information (legacy)
     *
     * @param source The asteroid entity that was split
     */
    public AsteroidSplitEvent(Entity source) {
        this(source, null, null);
    }

    /**
     * Get the entity that is the source of this event
     *
     * @return The source entity
     */
    @Override
    public Entity source() {
        return source;
    }

    /**
     * Get the velocity of the bullet that caused the split
     *
     * @return Bullet velocity vector, or null if not bullet-caused
     */
    public Vector2D getBulletVelocity() {
        return bulletVelocity;
    }

    /**
     * Get the point where the impact occurred
     *
     * @return Impact point, or null to use asteroid center
     */
    public Vector2D getImpactPoint() {
        return impactPoint;
    }

    /**
     * Check if this split was caused by a bullet
     *
     * @return true if bullet information is available
     */
    public boolean isBulletCaused() {
        return bulletVelocity != null;
    }
}