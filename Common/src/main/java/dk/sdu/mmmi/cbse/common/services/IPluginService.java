package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Interface for plugin lifecycle management.
 * Handles loading and unloading of game features.
 */
public interface IPluginService {
    /**
     * Start the plugin and initialize resources
     *
     * @param gameData Current game state
     * @param world Game world to populate
     */
    void start(GameData gameData, World world);

    /**
     * Stop the plugin and clean up resources
     *
     * @param gameData Current game state
     * @param world Game world to clean up
     */
    void stop(GameData gameData, World world);
}