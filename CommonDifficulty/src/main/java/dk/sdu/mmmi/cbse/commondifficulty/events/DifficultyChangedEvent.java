package dk.sdu.mmmi.cbse.commondifficulty.events;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEvent;
import dk.sdu.mmmi.cbse.commondifficulty.DifficultyParameters;

/**
 * Event published when difficulty level changes.
 */
public record DifficultyChangedEvent(
        DifficultyParameters oldParameters,
        DifficultyParameters newParameters
) implements IEvent {

    @Override
    public Entity source() {
        return null; // System-generated event
    }
}