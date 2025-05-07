package dk.sdu.mmmi.cbse.common.utils;

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class for service discovery using the ServiceLoader pattern.
 * Provides a consistent way to locate and load service implementations.
 */
public class ServiceLocator {
    private static final Logger LOGGER = Logger.getLogger(ServiceLocator.class.getName());

    /**
     * Get a single instance of a service implementation.
     * Throws exception if no implementation is found.
     *
     * @param <T> Service type
     * @param serviceClass Interface class of the service to locate
     * @return The service implementation
     * @throws ServiceNotFoundException If no implementation is found
     */
    public static <T> T getService(Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass)
                .findFirst()
                .orElseThrow(() -> {
                    LOGGER.log(Level.SEVERE, "No implementation found for service: {0}", serviceClass.getName());
                    return new ServiceNotFoundException("No implementation found for " + serviceClass.getName());
                });
    }

    /**
     * Get a single instance of a service implementation or null if not found.
     *
     * @param <T> Service type
     * @param serviceClass Interface class of the service to locate
     * @return The service implementation or null
     */
    public static <T> T getServiceOrNull(Class<T> serviceClass) {
        return ServiceLoader.load(serviceClass)
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all implementations of a service.
     *
     * @param <T> Service type
     * @param serviceClass Interface class of the service to locate
     * @return List of all service implementations
     */
    public static <T> List<T> locateAll(Class<T> serviceClass) {
        List<T> services = ServiceLoader.load(serviceClass)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(Collectors.toList());

        LOGGER.log(Level.INFO, "Found {0} implementations for service: {1}",
                new Object[]{services.size(), serviceClass.getName()});

        return services;
    }

    /**
     * Exception thrown when a required service is not found.
     */
    public static class ServiceNotFoundException extends RuntimeException {
        public ServiceNotFoundException(String message) {
            super(message);
        }
    }
}