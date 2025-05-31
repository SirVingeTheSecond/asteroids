package dk.sdu.mmmi.cbse.core.config;

import dk.sdu.mmmi.cbse.common.services.*;
import dk.sdu.mmmi.cbse.core.services.ServiceBridge;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spring configuration for the Core module.
 * Provides beans for services discovered via JPMS ServiceLoader mechanism.
 * This bridges the gap between JPMS service providers and Spring DI.
 */
@Configuration
public class GameConfiguration {
    private static final Logger LOGGER = Logger.getLogger(GameConfiguration.class.getName());

    /**
     * Create a bridge service that encapsulates ServiceLoader functionality
     * @return ServiceBridge for accessing JPMS services
     */
    @Bean
    public ServiceBridge serviceBridge() {
        LOGGER.log(Level.INFO, "Creating ServiceBridge bean for JPMS service discovery");
        return new ServiceBridge();
    }

    /**
     * Provide list of IUpdate services discovered via JPMS
     * @param serviceBridge The service bridge for accessing services
     * @return List of IUpdate implementations
     */
    @Bean
    public List<IUpdate> updateServices(ServiceBridge serviceBridge) {
        List<IUpdate> services = serviceBridge.getUpdateServices();
        LOGGER.log(Level.INFO, "Discovered {0} IUpdate services via ServiceBridge", services.size());
        return services;
    }

    /**
     * Provide list of IFixedUpdate services discovered via JPMS
     * @param serviceBridge The service bridge for accessing services
     * @return List of IFixedUpdate implementations
     */
    @Bean
    public List<IFixedUpdate> fixedUpdateServices(ServiceBridge serviceBridge) {
        List<IFixedUpdate> services = serviceBridge.getFixedUpdateServices();
        LOGGER.log(Level.INFO, "Discovered {0} IFixedUpdate services via ServiceBridge", services.size());
        return services;
    }

    /**
     * Provide list of ILateUpdate services discovered via JPMS
     * @param serviceBridge The service bridge for accessing services
     * @return List of ILateUpdate implementations
     */
    @Bean
    public List<ILateUpdate> lateUpdateServices(ServiceBridge serviceBridge) {
        List<ILateUpdate> services = serviceBridge.getLateUpdateServices();
        LOGGER.log(Level.INFO, "Discovered {0} ILateUpdate services via ServiceBridge", services.size());
        return services;
    }

    /**
     * Provide list of IPluginService implementations discovered via JPMS
     * @param serviceBridge The service bridge for accessing services
     * @return List of IPluginService implementations
     */
    @Bean
    public List<IPluginService> pluginServices(ServiceBridge serviceBridge) {
        List<IPluginService> services = serviceBridge.getPluginServices();
        LOGGER.log(Level.INFO, "Discovered {0} IPluginService implementations via ServiceBridge", services.size());
        return services;
    }

    /**
     * Provide list of IRenderingContext services discovered via JPMS
     * @param serviceBridge The service bridge for accessing services
     * @return List of IRenderingContext implementations
     */
    @Bean
    public List<IRenderingContext> renderingContexts(ServiceBridge serviceBridge) {
        List<IRenderingContext> services = serviceBridge.getRenderingContexts();
        LOGGER.log(Level.INFO, "Discovered {0} IRenderingContext services via ServiceBridge", services.size());
        return services;
    }

    /**
     * Provide the EventService bean
     * @return EventService implementation
     */
    @Bean
    public IEventService eventService() {
        LOGGER.log(Level.INFO, "Creating EventService bean");
        return new dk.sdu.mmmi.cbse.core.events.EventService();
    }
}