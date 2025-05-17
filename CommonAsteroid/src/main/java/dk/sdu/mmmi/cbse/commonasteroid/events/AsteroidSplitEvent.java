package dk.sdu.mmmi.cbse.commonasteroid.events;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEvent;

/**
 * Event triggered when an asteroid is split.
 */
public record AsteroidSplitEvent(Entity source) implements IEvent {
    /**
     * Create a new asteroid split event
     *
     * @param source The asteroid entity that was split
     */
    public AsteroidSplitEvent {
        if (source == null) {
            throw new IllegalArgumentException("Source entity cannot be null");
        }
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
}