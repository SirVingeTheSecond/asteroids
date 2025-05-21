package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.function.ToIntFunction;

public class ModuleConfig {

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

    public static List<IPluginService> getPluginServices() {
        return loadServices(IPluginService.class);
    }

    public static List<IUpdate> getUpdateServices() {
        return loadSortedServices(IUpdate.class, IUpdate::getPriority);
    }

    public static List<IFixedUpdate> getFixedUpdateServices() {
        return loadSortedServices(IFixedUpdate.class, IFixedUpdate::getPriority);
    }

    public static List<ILateUpdate> getLateUpdateServices() {
        return loadSortedServices(ILateUpdate.class, ILateUpdate::getPriority);
    }
}