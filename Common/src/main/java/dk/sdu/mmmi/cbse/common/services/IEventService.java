package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.events.IEventListener;

/**
 * Service interface for event management.
 */
public interface IEventService {
    /**
     * Subscribe to events of a specific type
     *
     * @param eventType Class of the event to subscribe to
     * @param listener Listener that will receive events
     * @param <T> Event type
     */
    <T> void subscribe(Class<T> eventType, IEventListener<T> listener);

    /**
     * Unsubscribe from events of a specific type
     *
     * @param eventType Class of the event to unsubscribe from
     * @param listener Listener to remove
     * @param <T> Event type
     */
    <T> void unsubscribe(Class<T> eventType, IEventListener<T> listener);

    /**
     * Publish an event to all subscribers
     *
     * @param event Event to publish
     * @param <T> Event type
     */
    <T> void publish(T event);
}