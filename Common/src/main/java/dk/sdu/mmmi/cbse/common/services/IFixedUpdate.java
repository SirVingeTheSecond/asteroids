package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

public interface IFixedUpdate {
    /**
     * Process entity behavior for a fixed game interval.
     *
     * @param gameData Current game state data
     * @param world Game world containing entities to process
     */
    void process(GameData gameData, World world);

    int getPriority();
}
