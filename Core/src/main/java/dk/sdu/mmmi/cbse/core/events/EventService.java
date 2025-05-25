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
 * Singleton implementation of the event service to ensure all systems use the same instance.
 */
public class EventService implements IEventService {
    private static final Logger LOGGER = Logger.getLogger(EventService.class.getName());

    private static final Map<Class<?>, List<IEventListener<?>>> STATIC_LISTENERS = new HashMap<>();
    private static int instanceCount = 0;

    /**
     * Default constructor required by ServiceLoader
     */
    public EventService() {
        instanceCount++;
        LOGGER.log(Level.INFO, "EventService instance #{0} created (using shared static listeners)", instanceCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, IEventListener<T> listener) {
        synchronized (STATIC_LISTENERS) {
            STATIC_LISTENERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                    .add(listener);
        }

        LOGGER.log(Level.INFO, "Subscribed {0} to {1} (total listeners: {2})",
                new Object[]{listener.getClass().getName(), eventType.getName(),
                        STATIC_LISTENERS.get(eventType).size()});
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, IEventListener<T> listener) {
        synchronized (STATIC_LISTENERS) {
            if (STATIC_LISTENERS.containsKey(eventType)) {
                STATIC_LISTENERS.get(eventType).remove(listener);
                LOGGER.log(Level.FINE, "Unsubscribed {0} from {1}",
                        new Object[]{listener.getClass().getName(), eventType.getName()});
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> void publish(T event) {
        Class<?> eventType = event.getClass();

        synchronized (STATIC_LISTENERS) {
            if (!STATIC_LISTENERS.containsKey(eventType)) {
                LOGGER.log(Level.WARNING, "No listeners registered for event type: {0}", eventType.getName());
                return;
            }

            List<IEventListener<?>> eventListeners = STATIC_LISTENERS.get(eventType);
            LOGGER.log(Level.INFO, "Publishing event {0} to {1} listeners",
                    new Object[]{eventType.getName(), eventListeners.size()});

            for (IEventListener<?> listener : eventListeners) {
                try {
                    LOGGER.log(Level.INFO, "Notifying listener: {0}", listener.getClass().getName());
                    ((IEventListener<T>) listener).onEvent(event);
                    LOGGER.log(Level.INFO, "Successfully notified listener: {0}", listener.getClass().getName());
                } catch (Exception e) {
                    LOGGER.log(Level.SEVERE, "Error notifying listener: " + listener.getClass().getName(), e);
                }
            }
        }
    }

    /**
     * Get debug information about current subscriptions
     */
    public String getDebugInfo() {
        StringBuilder sb = new StringBuilder("EventService Debug Info:\n");
        synchronized (STATIC_LISTENERS) {
            for (Map.Entry<Class<?>, List<IEventListener<?>>> entry : STATIC_LISTENERS.entrySet()) {
                sb.append(String.format("  %s: %d listeners\n",
                        entry.getKey().getSimpleName(), entry.getValue().size()));
                for (IEventListener<?> listener : entry.getValue()) {
                    sb.append(String.format("    - %s\n", listener.getClass().getName()));
                }
            }
        }
        return sb.toString();
    }
}