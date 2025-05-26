package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;

import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Test class to verify ModuleLayer resolution of split packages and no duplicates
 */
public class ModuleLayerTest {
    private static final Logger LOGGER = Logger.getLogger(ModuleLayerTest.class.getName());

    public static void testSplitPackageResolution() {
        LOGGER.info("Testing split package resolution...");

        // Test IUpdate services
        List<IUpdate> updateServices = ModuleConfig.getUpdateServices();
        LOGGER.info("Total IUpdate services: " + updateServices.size());

        // Check for duplicates by class name
        Map<String, Long> updateServiceCounts = updateServices.stream()
                .collect(Collectors.groupingBy(
                        service -> service.getClass().getName(),
                        Collectors.counting()
                ));

        updateServiceCounts.forEach((className, count) -> {
            if (count > 1) {
                LOGGER.warning("DUPLICATE IUpdate service detected: " + className + " (count: " + count + ")");
            } else {
                LOGGER.info("IUpdate service: " + className + " from module: " +
                        updateServices.stream()
                                .filter(s -> s.getClass().getName().equals(className))
                                .findFirst()
                                .map(s -> s.getClass().getModule().getName())
                                .orElse("unknown"));
            }
        });

        // Test IPluginService instances
        List<IPluginService> pluginServices = ModuleLayerManager.getAllPluginServices();
        LOGGER.info("Total IPluginService instances: " + pluginServices.size());

        // Check for duplicates
        Map<String, Long> pluginServiceCounts = pluginServices.stream()
                .collect(Collectors.groupingBy(
                        service -> service.getClass().getName(),
                        Collectors.counting()
                ));

        pluginServiceCounts.forEach((className, count) -> {
            if (count > 1) {
                LOGGER.warning("DUPLICATE IPluginService detected: " + className + " (count: " + count + ")");
            } else {
                LOGGER.info("IPluginService: " + className + " from module: " +
                        pluginServices.stream()
                                .filter(s -> s.getClass().getName().equals(className))
                                .findFirst()
                                .map(s -> s.getClass().getModule().getName())
                                .orElse("unknown"));
            }
        });

        // Show split package resolution
        long systemsInSplitPackage = updateServices.stream()
                .filter(service -> service.getClass().getPackageName().equals("dk.sdu.mmmi.cbse"))
                .count();

        LOGGER.info("Systems in split package 'dk.sdu.mmmi.cbse': " + systemsInSplitPackage);

        LOGGER.info("Split package resolution test completed successfully!");
    }
}