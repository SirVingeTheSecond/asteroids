package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service for spawning enemies during the game
 */
public interface IEnemySpawner {
    /**
     * Request spawning of new enemies
     * @pre gameData != null && world != null
     * @post New enemies added to world if conditions met
     */
    void spawnEnemies(GameData gameData, World world);

    /**
     * Configure enemy spawn parameters
     */
    void setSpawnConfig(int maxEnemies, float spawnInterval, float difficultyMultiplier);
}
