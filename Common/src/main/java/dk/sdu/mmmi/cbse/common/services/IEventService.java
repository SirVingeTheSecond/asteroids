package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.events.IEvent;
import dk.sdu.mmmi.cbse.common.events.IEventListener;

/**
 * Service Provider Interface for event management.
 */
public interface IEventService {

    /**
     * Publish an event to all registered listeners
     *
     * @param event Event to publish
     * @param <T> Type of event
     */
    <T extends IEvent> void publish(T event);

    /**
     * Subscribe to events of a specific type
     *
     * @param eventType Class of event to listen for
     * @param listener Listener to receive events
     * @param <T> Type of event
     */
    <T extends IEvent> void subscribe(Class<T> eventType, IEventListener<T> listener);

    /**
     * Unsubscribe from events of a specific type
     *
     * @param eventType Class of event to stop listening for
     * @param listener Listener to remove
     * @param <T> Type of event
     */
    <T extends IEvent> void unsubscribe(Class<T> eventType, IEventListener<T> listener);
}