package dk.sdu.mmmi.cbse.ui;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for UI system lifecycle management.
 */
//ToDo: Could be used
public class UIPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(UIPlugin.class.getName());

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "UIPlugin started");
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "UIPlugin stopped");
    }
}