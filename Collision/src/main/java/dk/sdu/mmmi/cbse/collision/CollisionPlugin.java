package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IGamePluginService;

/**
 * Plugin for collision system.
 * Manages collision system lifecycle.
 */
public class CollisionPlugin implements IGamePluginService {

    @Override
    public void start(GameData gameData, World world) {
        // System is instantiated through ServiceLoader
        // Nothing to do here
    }

    @Override
    public void stop(GameData gameData, World world) {
        // Nothing to clean up
    }
}