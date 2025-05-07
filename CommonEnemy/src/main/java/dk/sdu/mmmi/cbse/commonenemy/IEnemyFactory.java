package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;

/**
 * Factory for creating enemy ships
 */
public interface IEnemyFactory {
    /**
     * Creates an enemy with specified behavior and properties
     * @pre gameData != null
     * @post Created enemy has valid properties and behavior
     */
    Entity createEnemy(GameData gameData, EnemyBehavior behavior, EnemyProperties properties);
}
