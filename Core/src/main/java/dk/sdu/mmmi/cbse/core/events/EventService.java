package dk.sdu.mmmi.cbse.core.events;

import dk.sdu.mmmi.cbse.common.events.IEvent;
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

    private static final Map<Class<? extends IEvent>, List<IEventListener<? extends IEvent>>> STATIC_LISTENERS = new HashMap<>();
    private static int instanceCount = 0;

    public EventService() {
        instanceCount++;
        LOGGER.log(Level.INFO, "EventService instance #{0} created (using shared static listeners)", instanceCount);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEvent> void subscribe(Class<T> eventType, IEventListener<T> listener) {
        synchronized (STATIC_LISTENERS) {
            List<IEventListener<? extends IEvent>> listeners =
                    STATIC_LISTENERS.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>());

            // Prevent duplicate subscriptions - only add if not already present
            if (!listeners.contains(listener)) {
                listeners.add(listener);
                LOGGER.log(Level.INFO, "Subscribed {0} to {1} (total listeners: {2})",
                        new Object[]{listener.getClass().getName(), eventType.getName(), listeners.size()});
            } else {
                LOGGER.log(Level.FINE, "Listener {0} already subscribed to {1}, skipping duplicate",
                        new Object[]{listener.getClass().getName(), eventType.getName()});
            }
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends IEvent> void unsubscribe(Class<T> eventType, IEventListener<T> listener) {
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
    public <T extends IEvent> void publish(T event) {
        if (event == null) {
            LOGGER.log(Level.WARNING, "Attempted to publish null event - ignoring");
            return;
        }

        Class<? extends IEvent> eventType = event.getClass();

        synchronized (STATIC_LISTENERS) {
            if (!STATIC_LISTENERS.containsKey(eventType)) {
                LOGGER.log(Level.WARNING, "No listeners registered for event type: {0}", eventType.getName());
                return;
            }

            List<IEventListener<? extends IEvent>> eventListeners = STATIC_LISTENERS.get(eventType);
            LOGGER.log(Level.INFO, "Publishing event {0} to {1} listeners",
                    new Object[]{eventType.getName(), eventListeners.size()});

            for (IEventListener<? extends IEvent> listener : eventListeners) {
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
}