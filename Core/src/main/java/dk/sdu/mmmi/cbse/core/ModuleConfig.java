package dk.sdu.mmmi.cbse.core;

import dk.sdu.mmmi.cbse.common.services.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.ServiceLoader;

import static java.util.stream.Collectors.toList;

@Configuration
class ModuleConfig {

    public ModuleConfig() {
    }

    @Bean
    public GameLoop game() {
        return new GameLoop(
                pluginLifecycles(),
                entityProcessors(),
                postProcessors(),
                gameEventService()
        );
    }

    @Bean
    public List<IPluginLifecycle> pluginLifecycles() {
        List<IPluginLifecycle> plugins = ServiceLoader.load(IPluginLifecycle.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(toList());

        System.out.println("Loaded " + plugins.size() + " plugins:");
        for (IPluginLifecycle plugin : plugins) {
            System.out.println(" - " + plugin.getClass().getName());
        }

        return plugins;
    }

    @Bean
    public List<IEntityProcessingService> entityProcessors() {
        List<IEntityProcessingService> processors = ServiceLoader.load(IEntityProcessingService.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(toList());

        System.out.println("Loaded " + processors.size() + " entity processors:");
        for (IEntityProcessingService processor : processors) {
            System.out.println(" - " + processor.getClass().getName());
        }

        return processors;
    }

    @Bean
    public List<IPostEntityProcessingService> postProcessors() {
        return ServiceLoader.load(IPostEntityProcessingService.class)
                .stream()
                .map(ServiceLoader.Provider::get)
                .collect(toList());
    }

    @Bean
    public IGameEventService gameEventService() {
        return ServiceLoader.load(IGameEventService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IGameEventService implementation found"));
    }
}