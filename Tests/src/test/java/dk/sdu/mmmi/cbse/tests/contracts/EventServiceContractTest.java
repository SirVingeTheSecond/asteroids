package dk.sdu.mmmi.cbse.tests.contracts;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Contract tests for IEventService implementations using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Event Service Contract Tests")
public class EventServiceContractTest {

    @Mock
    private IEventListener<AsteroidSplitEvent> mockListener;

    private IEventService eventService;

    @BeforeEach
    void setUp() {
        eventService = ServiceLoader.load(IEventService.class)
                .findFirst()
                .orElseThrow(() -> new AssertionError("IEventService implementation not found"));
    }

    @Test
    @DisplayName("Contract: Event service must deliver events to subscribers")
    void testEventDeliveryContract() {
        AsteroidSplitEvent testEvent = new AsteroidSplitEvent(new Entity());

        eventService.subscribe(AsteroidSplitEvent.class, mockListener);
        eventService.publish(testEvent);

        // Verify contract: subscriber must receive the event
        verify(mockListener, times(1)).onEvent(testEvent);
    }

    @Test
    @DisplayName("Contract: Unsubscribed listeners must not receive events")
    void testUnsubscriptionContract() {
        AsteroidSplitEvent testEvent = new AsteroidSplitEvent(new Entity());

        eventService.subscribe(AsteroidSplitEvent.class, mockListener);
        eventService.unsubscribe(AsteroidSplitEvent.class, mockListener);
        eventService.publish(testEvent);

        // Verify contract: unsubscribed listener should not receive event
        verify(mockListener, never()).onEvent(any(AsteroidSplitEvent.class));
    }

    @Test
    @DisplayName("Contract: Service must handle multiple subscribers")
    void testMultipleSubscribersContract() {
        @SuppressWarnings("unchecked")
        IEventListener<AsteroidSplitEvent> mockListener2 = mock(IEventListener.class);

        AsteroidSplitEvent testEvent = new AsteroidSplitEvent(new Entity());

        eventService.subscribe(AsteroidSplitEvent.class, mockListener);
        eventService.subscribe(AsteroidSplitEvent.class, mockListener2);
        eventService.publish(testEvent);

        // Both listeners should receive the event
        verify(mockListener, times(1)).onEvent(testEvent);
        verify(mockListener2, times(1)).onEvent(testEvent);
    }

    @Test
    @DisplayName("Contract: Service must handle null events gracefully")
    void testNullEventHandling() {
        eventService.subscribe(AsteroidSplitEvent.class, mockListener);

        // Should not crash on null event
        assertDoesNotThrow(() -> {
            eventService.publish(null);
        }, "Event service should handle null events gracefully");

        // Listener should not be called with null
        verify(mockListener, never()).onEvent(any());
    }
}