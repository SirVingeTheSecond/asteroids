package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;

import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages module loading to resolve split package conflicts
 */
public class ModuleLayerManager {
    private static final Logger LOGGER = Logger.getLogger(ModuleLayerManager.class.getName());

    private static ModuleLayer pluginLayer;
    private static Set<String> pluginLayerModules = Set.of();

    /**
     * Load plugins from the plugins directory using a separate ModuleLayer
     */
    public static void loadPluginModules() {
        try {
            Path pluginsDir = Paths.get("plugins");
            if (!pluginsDir.toFile().exists()) {
                LOGGER.log(Level.INFO, "No plugins directory found at: {0}", pluginsDir);
                return;
            }

            ModuleFinder pluginFinder = ModuleFinder.of(pluginsDir);
            List<String> pluginModuleNames = pluginFinder.findAll()
                    .stream()
                    .map(ref -> ref.descriptor().name())
                    .toList();

            if (pluginModuleNames.isEmpty()) {
                LOGGER.log(Level.INFO, "No plugin modules found in plugins directory");
                return;
            }

            pluginLayerModules = Set.copyOf(pluginModuleNames);
            LOGGER.log(Level.INFO, "Found plugin modules: {0}", pluginModuleNames);

            Configuration pluginConfig = Configuration.resolve(
                    pluginFinder,
                    List.of(ModuleLayer.boot().configuration()),
                    ModuleFinder.of(),
                    pluginModuleNames
            );

            pluginLayer = ModuleLayer.defineModulesWithOneLoader(
                    pluginConfig,
                    List.of(ModuleLayer.boot()),
                    ClassLoader.getSystemClassLoader()
            ).layer();

            LOGGER.log(Level.INFO, "Successfully created plugin ModuleLayer with {0} modules",
                    pluginLayer.modules().size());

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to load plugin modules", e);
        }
    }

    /**
     * Get all IUpdate services by only loading plugin services from plugin layer
     */
    public static List<IUpdate> getAllUpdateServices() {
        List<IUpdate> allServices = new ArrayList<>();

        // Load ALL services from boot layer
        ServiceLoader.load(IUpdate.class).forEach(service -> {
            allServices.add(service);
            LOGGER.log(Level.FINE, "Boot layer: {0} from {1}",
                    new Object[]{service.getClass().getName(), service.getClass().getModule().getName()});
        });

        // Load ONLY services from modules that are exclusively in plugin layer
        if (pluginLayer != null) {
            ServiceLoader.load(pluginLayer, IUpdate.class).forEach(service -> {
                String moduleName = service.getClass().getModule().getName();
                String className = service.getClass().getName();

                // Only add if this service class doesn't already exist from boot layer
                boolean existsInBoot = allServices.stream()
                        .anyMatch(bootService -> bootService.getClass().getName().equals(className));

                if (!existsInBoot && pluginLayerModules.contains(moduleName)) {
                    allServices.add(service);
                    LOGGER.log(Level.INFO, "Plugin layer: {0} from {1}",
                            new Object[]{className, moduleName});
                } else {
                    LOGGER.log(Level.FINE, "Skipping duplicate: {0} from {1} (exists in boot: {2})",
                            new Object[]{className, moduleName, existsInBoot});
                }
            });
        }

        LOGGER.log(Level.INFO, "Loaded {0} IUpdate services total (deduplicated)", allServices.size());
        return allServices;
    }

    /**
     * Get all IPluginService services by only loading plugin services from plugin layer
     */
    public static List<IPluginService> getAllPluginServices() {
        List<IPluginService> allServices = new java.util.ArrayList<>();

        // Load ALL services from boot layer
        ServiceLoader.load(IPluginService.class).forEach(service -> {
            allServices.add(service);
            LOGGER.log(Level.FINE, "Boot layer plugin: {0} from {1}",
                    new Object[]{service.getClass().getName(), service.getClass().getModule().getName()});
        });

        // Load only services from modules that are exclusively in plugin layer
        if (pluginLayer != null) {
            ServiceLoader.load(pluginLayer, IPluginService.class).forEach(service -> {
                String moduleName = service.getClass().getModule().getName();
                String className = service.getClass().getName();

                // Only add if this service class doesn't already exist from boot layer
                boolean existsInBoot = allServices.stream()
                        .anyMatch(bootService -> bootService.getClass().getName().equals(className));

                if (!existsInBoot && pluginLayerModules.contains(moduleName)) {
                    allServices.add(service);
                    LOGGER.log(Level.INFO, "Plugin layer plugin: {0} from {1}",
                            new Object[]{className, moduleName});
                } else {
                    LOGGER.log(Level.FINE, "Skipping duplicate plugin: {0} from {1} (exists in boot: {2})",
                            new Object[]{className, moduleName, existsInBoot});
                }
            });
        }

        LOGGER.log(Level.INFO, "Loaded {0} IPluginService instances total (deduplicated)", allServices.size());
        return allServices;
    }
}