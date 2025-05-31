package dk.sdu.mmmi.cbse.core.services;

import dk.sdu.mmmi.cbse.common.services.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.ToIntFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Bridge service that encapsulates JPMS ServiceLoader functionality.
 * Acts as an adapter between JPMS service discovery and Spring DI.
 */
public class ServiceBridge {
    private static final Logger LOGGER = Logger.getLogger(ServiceBridge.class.getName());

    // Cached service lists
    private List<IRenderingContext> renderingContexts;
    private List<IPluginService> pluginServices;
    private List<IUpdate> updateServices;
    private List<IFixedUpdate> fixedUpdateServices;
    private List<ILateUpdate> lateUpdateServices;

    /**
     * Load services of a specific type using ServiceLoader
     * @param serviceType The service interface class
     * @param <T> The service type
     * @return List of discovered services
     */
    private <T> List<T> loadServices(Class<T> serviceType) {
        LOGGER.log(Level.FINE, "Loading services for type: {0}", serviceType.getName());

        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        List<T> services = new ArrayList<>();
        loader.iterator().forEachRemaining(services::add);

        LOGGER.log(Level.FINE, "Discovered {0} services of type {1}",
                new Object[]{services.size(), serviceType.getSimpleName()});

        return services;
    }

    /**
     * Load and sort services by priority
     * @param serviceType The service interface class
     * @param priorityFunction Function to extract priority from service
     * @param <T> The service type
     * @return Sorted list of services by priority
     */
    private <T> List<T> loadSortedServices(Class<T> serviceType, ToIntFunction<T> priorityFunction) {
        List<T> services = loadServices(serviceType);
        services.sort(Comparator.comparingInt(priorityFunction));

        LOGGER.log(Level.INFO, "Loaded and sorted {0} services of type {1} by priority",
                new Object[]{services.size(), serviceType.getSimpleName()});

        return services;
    }

    /**
     * Get all rendering context services
     * @return List of IRenderingContext implementations
     */
    public List<IRenderingContext> getRenderingContexts() {
        if (renderingContexts == null) {
            renderingContexts = loadServices(IRenderingContext.class);
            LOGGER.log(Level.INFO, "Cached {0} rendering contexts", renderingContexts.size());
        }
        return renderingContexts;
    }

    /**
     * Get all plugin services
     * @return List of IPluginService implementations
     */
    public List<IPluginService> getPluginServices() {
        if (pluginServices == null) {
            pluginServices = loadServices(IPluginService.class);
            LOGGER.log(Level.INFO, "Cached {0} plugin services", pluginServices.size());
        }
        return pluginServices;
    }

    /**
     * Get all update services sorted by priority
     * @return List of IUpdate implementations sorted by priority
     */
    public List<IUpdate> getUpdateServices() {
        if (updateServices == null) {
            updateServices = loadSortedServices(IUpdate.class, IUpdate::getPriority);
            LOGGER.log(Level.INFO, "Cached {0} update services sorted by priority", updateServices.size());
        }
        return updateServices;
    }

    /**
     * Get all fixed update services sorted by priority
     * @return List of IFixedUpdate implementations sorted by priority
     */
    public List<IFixedUpdate> getFixedUpdateServices() {
        if (fixedUpdateServices == null) {
            fixedUpdateServices = loadSortedServices(IFixedUpdate.class, IFixedUpdate::getPriority);
            LOGGER.log(Level.INFO, "Cached {0} fixed update services sorted by priority", fixedUpdateServices.size());
        }
        return fixedUpdateServices;
    }

    /**
     * Get all late update services sorted by priority
     * @return List of ILateUpdate implementations sorted by priority
     */
    public List<ILateUpdate> getLateUpdateServices() {
        if (lateUpdateServices == null) {
            lateUpdateServices = loadSortedServices(ILateUpdate.class, ILateUpdate::getPriority);
            LOGGER.log(Level.INFO, "Cached {0} late update services sorted by priority", lateUpdateServices.size());
        }
        return lateUpdateServices;
    }

    /**
     * Clear all cached services - useful for testing or reconfiguration
     */
    public void clearCache() {
        LOGGER.log(Level.INFO, "Clearing ServiceBridge cache");
        renderingContexts = null;
        pluginServices = null;
        updateServices = null;
        fixedUpdateServices = null;
        lateUpdateServices = null;
    }
}