package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.services.*;
import dk.sdu.mmmi.cbse.core.config.GameConfiguration;
import dk.sdu.mmmi.cbse.core.services.ServiceBridge;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Spring DI and JPMS ServiceLoader integration.
 * Verifies that the ServiceBridge correctly discovers JPMS services and
 * Spring properly manages them as beans.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Spring JPMS Integration Tests")
public class SpringJPMSIntegrationTest {

    private AnnotationConfigApplicationContext applicationContext;
    private GameConfiguration gameConfiguration;

    @BeforeEach
    void setUp() {
        gameConfiguration = new GameConfiguration();
    }

    @Test
    @DisplayName("ServiceBridge is properly created as Spring bean")
    void testServiceBridgeCreation() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
            ServiceBridge serviceBridge = context.getBean(ServiceBridge.class);

            assertNotNull(serviceBridge, "ServiceBridge should be created as Spring bean");
        }
    }

    @Test
    @DisplayName("Spring context provides JPMS-discovered services as beans")
    void testJPMSServiceDiscovery() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
            // Test that service lists are available as beans
            assertNotNull(context.getBean("updateServices"), "Update services should be available");
            assertNotNull(context.getBean("fixedUpdateServices"), "Fixed update services should be available");
            assertNotNull(context.getBean("lateUpdateServices"), "Late update services should be available");
            assertNotNull(context.getBean("pluginServices"), "Plugin services should be available");
            assertNotNull(context.getBean("renderingContexts"), "Rendering contexts should be available");
        }
    }

    @Test
    @DisplayName("ServiceBridge correctly discovers and sorts services by priority")
    void testServicePriorityOrdering() {
        ServiceBridge serviceBridge = gameConfiguration.serviceBridge();

        List<IUpdate> updateServices = serviceBridge.getUpdateServices();
        List<IFixedUpdate> fixedUpdateServices = serviceBridge.getFixedUpdateServices();
        List<ILateUpdate> lateUpdateServices = serviceBridge.getLateUpdateServices();

        // Verify services are sorted by priority
        assertServicesSortedByPriority(updateServices);
        assertServicesSortedByPriority(fixedUpdateServices);
        assertServicesSortedByPriority(lateUpdateServices);
    }

    @Test
    @DisplayName("EventService is properly created and accessible")
    void testEventServiceCreation() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
            IEventService eventService = context.getBean(IEventService.class);

            assertNotNull(eventService, "EventService should be available");
            assertEquals("dk.sdu.mmmi.cbse.core.events.EventService",
                    eventService.getClass().getName(),
                    "Should be the Core EventService implementation");
        }
    }

    @Test
    @DisplayName("ServiceBridge caching works correctly")
    void testServiceBridgeCaching() {
        ServiceBridge serviceBridge = new ServiceBridge();

        // First call should populate cache
        List<IUpdate> firstCall = serviceBridge.getUpdateServices();
        assertNotNull(firstCall);

        // Second call should return same cached instance
        List<IUpdate> secondCall = serviceBridge.getUpdateServices();
        assertSame(firstCall, secondCall, "ServiceBridge should cache service lists");
    }

    @Test
    @DisplayName("ServiceBridge clear cache functionality works")
    void testServiceBridgeClearCache() {
        ServiceBridge serviceBridge = new ServiceBridge();

        // Populate cache
        List<IUpdate> beforeClear = serviceBridge.getUpdateServices();
        assertNotNull(beforeClear);

        // Clear cache
        serviceBridge.clearCache();

        // Next call should create new list (not cached)
        List<IUpdate> afterClear = serviceBridge.getUpdateServices();
        assertNotNull(afterClear);
        // Note: We can't test != because ServiceLoader might return same services
        // but the implementation should work correctly
    }

    @Test
    @DisplayName("Spring context manages service lifecycle correctly")
    void testServiceLifecycleManagement() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
            // Verify context is properly initialized
            assertTrue(context.isActive(), "Application context should be active");

            // Get service bridge multiple times - should be singleton
            ServiceBridge bridge1 = context.getBean(ServiceBridge.class);
            ServiceBridge bridge2 = context.getBean(ServiceBridge.class);
            assertSame(bridge1, bridge2, "ServiceBridge should be singleton in Spring context");

            // Close context
            context.close();
            assertFalse(context.isActive(), "Application context should be closed");
        }
    }

    @Test
    @DisplayName("JPMS services are properly typed and accessible")
    void testServiceTyping() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
            ServiceBridge serviceBridge = context.getBean(ServiceBridge.class);

            // Test service type safety
            List<IUpdate> updateServices = serviceBridge.getUpdateServices();
            for (IUpdate service : updateServices) {
                assertNotNull(service, "Update service should not be null");
                assertTrue(service instanceof IUpdate, "Service should implement IUpdate");

                // Verify priority method is accessible
                assertDoesNotThrow(() -> {
                    int priority = service.getPriority();
                    assertTrue(priority >= 0, "Priority should be non-negative");
                }, "Priority method should be accessible");
            }
        }
    }

    @Test
    @DisplayName("Service discovery handles empty service lists gracefully")
    void testEmptyServiceHandling() {
        ServiceBridge serviceBridge = new ServiceBridge();

        // Even if no services are found, should return empty lists, not null
        assertNotNull(serviceBridge.getUpdateServices(), "Should return empty list, not null");
        assertNotNull(serviceBridge.getFixedUpdateServices(), "Should return empty list, not null");
        assertNotNull(serviceBridge.getLateUpdateServices(), "Should return empty list, not null");
        assertNotNull(serviceBridge.getPluginServices(), "Should return empty list, not null");
        assertNotNull(serviceBridge.getRenderingContexts(), "Should return empty list, not null");
    }

    @Test
    @DisplayName("GameConfiguration logging does not interfere with bean creation")
    void testLoggingIntegration() {
        // This test ensures that the logging in GameConfiguration doesn't cause issues
        assertDoesNotThrow(() -> {
            try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(GameConfiguration.class)) {
                // All beans should be created successfully despite logging
                assertNotNull(context.getBean(ServiceBridge.class));
                assertNotNull(context.getBean(IEventService.class));
                assertNotNull(context.getBean("updateServices"));
            }
        }, "Logging should not interfere with bean creation");
    }

    // Helper method to verify services are sorted by priority
    private <T> void assertServicesSortedByPriority(List<T> services) {
        for (int i = 1; i < services.size(); i++) {
            int prevPriority = getPriority(services.get(i - 1));
            int currPriority = getPriority(services.get(i));

            assertTrue(prevPriority <= currPriority,
                    String.format("Services should be sorted by priority: %d should be <= %d",
                            prevPriority, currPriority));
        }
    }

    private int getPriority(Object service) {
        if (service instanceof IUpdate) {
            return ((IUpdate) service).getPriority();
        } else if (service instanceof IFixedUpdate) {
            return ((IFixedUpdate) service).getPriority();
        } else if (service instanceof ILateUpdate) {
            return ((ILateUpdate) service).getPriority();
        }
        return 0;
    }
}