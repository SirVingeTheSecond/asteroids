package dk.sdu.mmmi.cbse.core.events;

import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of the event service.
 */
public class EventService implements IEventService {
    private static final Logger LOGGER = Logger.getLogger(EventService.class.getName());

    private final Map<Class<?>, List<IEventListener<?>>> listeners = new HashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, IEventListener<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                .add(listener);

        LOGGER.log(Level.FINE, "Subscribed {0} to {1}",
                new Object[]{listener.getClass().getName(), eventType.getName()});
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, IEventListener<T> listener) {
        if (listeners.containsKey(eventType)) {
            listeners.get(eventType).remove(listener);
            LOGGER.log(Level.FINE, "Unsubscribed {0} from {1}",
                    new Object[]{listener.getClass().getName(), eventType.getName()});
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        Class<?> eventType = event.getClass();
        if (!listeners.containsKey(eventType)) {
            return;
        }

        LOGGER.log(Level.FINE, "Publishing event: {0}", eventType.getName());

        List<IEventListener<?>> eventListeners = listeners.get(eventType);
        for (IEventListener<?> listener : eventListeners) {
            try {
                ((IEventListener<T>) listener).onEvent(event);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error notifying listener: " + e.getMessage(), e);
            }
        }
    }
}