package dk.sdu.mmmi.cbse.tests.services;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEvent;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.core.events.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;

/**
 * Unit tests for EventService - testing event-driven architecture
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    private EventService eventService;

    @Mock
    private IEventListener<TestEvent> mockListener1;

    @Mock
    private IEventListener<TestEvent> mockListener2;

    @Mock
    private IEventListener<AnotherTestEvent> mockAnotherListener;

    @BeforeEach
    void setUp() {
        eventService = new EventService();
    }

    @Test
    @DisplayName("Should subscribe and notify listeners correctly")
    void shouldSubscribeAndNotifyListeners() {
        // Subscribe listeners
        eventService.subscribe(TestEvent.class, mockListener1);
        eventService.subscribe(TestEvent.class, mockListener2);

        // Create and publish event
        TestEvent event = new TestEvent("test message");
        eventService.publish(event);

        // Verify both listeners were notified
        verify(mockListener1).onEvent(event);
        verify(mockListener2).onEvent(event);
    }

    @Test
    @DisplayName("Should only notify listeners of correct event type")
    void shouldOnlyNotifyListenersOfCorrectEventType() {
        // Subscribe listeners for different event types
        eventService.subscribe(TestEvent.class, mockListener1);
        eventService.subscribe(AnotherTestEvent.class, mockAnotherListener);

        // Publish TestEvent
        TestEvent testEvent = new TestEvent("test");
        eventService.publish(testEvent);

        // Only TestEvent listener should be notified
        verify(mockListener1).onEvent(testEvent);
        verify(mockAnotherListener, never()).onEvent(any());
    }

    @Test
    @DisplayName("Should unsubscribe listeners correctly")
    void shouldUnsubscribeListeners() {
        // Subscribe listener
        eventService.subscribe(TestEvent.class, mockListener1);

        // Publish event - should be notified
        TestEvent event1 = new TestEvent("first");
        eventService.publish(event1);
        verify(mockListener1).onEvent(event1);

        // Unsubscribe listener
        eventService.unsubscribe(TestEvent.class, mockListener1);

        // Publish another event - should not be notified
        TestEvent event2 = new TestEvent("second");
        eventService.publish(event2);
        verify(mockListener1, never()).onEvent(event2);
    }

    @Test
    @DisplayName("Should handle null events gracefully")
    void shouldHandleNullEventsGracefully() {
        eventService.subscribe(TestEvent.class, mockListener1);

        // Should not crash when publishing null
        eventService.publish(null);

        // Listener should not be called
        verify(mockListener1, never()).onEvent(any());
    }

    @Test
    @DisplayName("Should handle publishing events with no listeners")
    void shouldHandlePublishingEventsWithNoListeners() {
        // Should not crash when no listeners are subscribed
        TestEvent event = new TestEvent("no listeners");
        eventService.publish(event);

        // No exceptions should be thrown
    }

    @Test
    @DisplayName("Should handle listener exceptions gracefully")
    void shouldHandleListenerExceptionsGracefully() {
        // Create listener that throws exception
        IEventListener<TestEvent> faultyListener = event -> {
            throw new RuntimeException("Listener error");
        };

        eventService.subscribe(TestEvent.class, faultyListener);
        eventService.subscribe(TestEvent.class, mockListener1);

        TestEvent event = new TestEvent("test");

        // Should not crash and should still notify other listeners
        eventService.publish(event);

        verify(mockListener1).onEvent(event);
    }

    @Test
    @DisplayName("Should support multiple subscriptions of same listener")
    void shouldSupportMultipleSubscriptionsOfSameListener() {
        // Subscribe same listener multiple times (shouldn't duplicate)
        eventService.subscribe(TestEvent.class, mockListener1);
        eventService.subscribe(TestEvent.class, mockListener1);

        TestEvent event = new TestEvent("test");
        eventService.publish(event);

        // Listener should still only be called once
        verify(mockListener1, times(1)).onEvent(event);
    }

    // Test event classes
    private static class TestEvent implements IEvent {
        private final String message;

        public TestEvent(String message) {
            this.message = message;
        }

        @Override
        public Entity source() {
            return null;
        }

        public String getMessage() {
            return message;
        }
    }

    private static class AnotherTestEvent implements IEvent {
        @Override
        public Entity source() {
            return null;
        }
    }
}