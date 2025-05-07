package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Base interface for all processing services in the game.
 */
public interface IProcessingService {
    /**
     * Process entity behavior for a game update.
     *
     * @param gameData Current game state data
     * @param world Game world containing entities to process
     */
    void process(GameData gameData, World world);
}