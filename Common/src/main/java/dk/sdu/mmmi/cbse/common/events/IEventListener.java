package dk.sdu.mmmi.cbse.common.events;

/**
 * Interface for event listeners.
 * Supports listening to any event type through generic type parameter.
 *
 * @param <T> The type of event this listener handles
 */
public interface IEventListener<T> {
    /**
     * Called when an event is fired.
     *
     * @param event The event that was fired
     */
    void onEvent(T event);
}