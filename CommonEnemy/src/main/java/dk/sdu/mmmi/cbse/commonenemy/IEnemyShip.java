package dk.sdu.mmmi.cbse.commonenemy;

/**
 * Identifies an entity as an enemy ship
 */
public interface IEnemyShip {
    /**
     * @return Current behavior type of the enemy
     */
    EnemyBehavior getBehavior();

    /**
     * @return Properties defining enemy capabilities
     */
    EnemyProperties getProperties();
}