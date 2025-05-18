package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
import dk.sdu.mmmi.cbse.common.services.IPluginService;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for render system.
 */
public class RenderPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(RenderPlugin.class.getName());

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "RenderPlugin starting");

        IDebugRendererSPI debugRenderer = ServiceLoader.load(IDebugRendererSPI.class).findFirst().orElse(null);

        if (debugRenderer != null) {
            debugRenderer.setEnabled(gameData.isDebugMode());
        }

        LOGGER.log(Level.INFO, "RenderPlugin started");
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "RenderPlugin stopping");
    }
}