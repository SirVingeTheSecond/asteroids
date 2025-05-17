package dk.sdu.mmmi.cbse.common.events;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Base interface for all game events.
 * Defines the contract for identifying event sources.
 */
public interface IEvent {
    /**
     * Get the entity that is the source of this event.
     *
     * @return The source entity or null if no specific entity triggered this event
     */
    Entity source();
}