package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for collision system.
 */
public class CollisionPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(CollisionPlugin.class.getName());

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "CollisionPlugin starting");
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "CollisionPlugin stopping");
    }
}