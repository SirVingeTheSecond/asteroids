package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Base interface for all processing services in the game.
 */
public interface IUpdate {
    /**
     * Process entity behavior for a game update.
     *
     * @param gameData Current game state data
     * @param world Game world containing entities to process
     */
    void update(GameData gameData, World world);

    int getPriority();
}