package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service Provider Interface for enemy functionality.
 * Provides operations for enemy creation and management.
 */
public interface IEnemySPI {
    /**
     * Create a new enemy entity
     *
     * @param type Type of enemy to create
     * @param gameData Current game state
     * @param world Game world
     * @return New enemy entity
     */
    Entity createEnemy(EnemyType type, GameData gameData, World world);

    /**
     * Spawn enemies
     *
     * @param gameData Current game state
     * @param world Game world
     */
    void spawnEnemies(GameData gameData, World world);

    /**
     * Check if an enemy should fire its weapon
     *
     * @param enemy Enemy entity
     * @param playerPosition Position of the player
     * @return true if the enemy should fire
     */
    boolean shouldFire(Entity enemy, float[] playerPosition);
}