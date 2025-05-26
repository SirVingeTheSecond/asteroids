package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.services.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.ToIntFunction;

public class ModuleConfig {

    // This might be somewhat against pure JPMS but this is a specific design choice
    private static List<IRenderingContext> renderingContexts;
    private static List<IPluginService> pluginServices;
    private static List<IUpdate> updateServices;
    private static List<IFixedUpdate> fixedUpdateServices;
    private static List<ILateUpdate> lateUpdateServices;

    private static <T> List<T> loadServices(Class<T> serviceType) {
        ServiceLoader<T> loader = ServiceLoader.load(serviceType);
        List<T> services = new ArrayList<>();
        loader.iterator().forEachRemaining(services::add);
        return services;
    }

    private static <T> List<T> loadSortedServices(Class<T> serviceType, ToIntFunction<T> priority) {
        List<T> services = loadServices(serviceType);
        services.sort(Comparator.comparingInt(priority));
        return services;
    }

    public static List<IRenderingContext> getRenderingContexts() {
        if (renderingContexts == null) {
            renderingContexts = loadServices(IRenderingContext.class);
        }
        return renderingContexts;
    }

    public static List<IPluginService> getPluginServices() {
        if (pluginServices == null) {
            pluginServices = loadServices(IPluginService.class);
        }
        return pluginServices;
    }

    public static List<IUpdate> getUpdateServices() {
        if (updateServices == null) {
            updateServices = ModuleLayerManager.getAllUpdateServices();
            updateServices.sort(Comparator.comparingInt(IUpdate::getPriority));
        }
        return updateServices;
    }

    public static List<IFixedUpdate> getFixedUpdateServices() {
        if (fixedUpdateServices == null) {
            fixedUpdateServices = loadSortedServices(IFixedUpdate.class, IFixedUpdate::getPriority);
        }
        return fixedUpdateServices;
    }

    public static List<ILateUpdate> getLateUpdateServices() {
        if (lateUpdateServices == null) {
            lateUpdateServices = loadSortedServices(ILateUpdate.class, ILateUpdate::getPriority);
        }
        return lateUpdateServices;
    }
}