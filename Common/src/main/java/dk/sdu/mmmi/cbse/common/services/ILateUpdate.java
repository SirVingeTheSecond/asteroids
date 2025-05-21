package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Interface for systems that run after entity processing.
 * Used for collision detection, cleanup, etc.
 */
public interface ILateUpdate {
    /**
     * Process operations after main entity processing
     *
     * @param gameData Current game state
     * @param world Game world containing entities
     */
    void process(GameData gameData, World world);

    int getPriority();
}