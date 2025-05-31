package dk.sdu.mmmi.cbse.commonenemy.events;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEvent;

/**
 * Event published when an enemy is destroyed.
 */
public record EnemyDestroyedEvent(Entity source, DestructionCause cause, int scoreValue) implements IEvent {
    /**
     * Reasons for enemy destruction
     */
    public enum DestructionCause {
        PLAYER_BULLET,      // Destroyed by player's projectile
        PLAYER_COLLISION,   // Destroyed by collision with player
        ASTEROID,           // Destroyed by hitting an Asteroid
        OTHER               // I mean, what is even the point?
    }

    /**
     * Create a new enemy destroyed event
     *
     * @param source The destroyed enemy entity
     * @param cause The cause of destruction
     * @param scoreValue Score value of the destroyed enemy
     */
    public EnemyDestroyedEvent {
        if (source == null) {
            throw new IllegalArgumentException("Source entity cannot be null");
        }
    }

    /**
     * Get the entity that was destroyed
     *
     * @return The source entity
     */
    @Override
    public Entity source() {
        return source;
    }
}