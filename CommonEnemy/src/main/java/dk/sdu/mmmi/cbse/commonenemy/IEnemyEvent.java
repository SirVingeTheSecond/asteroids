package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Events that can be emitted by enemies
 */
public interface IEnemyEvent {
    void onPlayerSpotted(Entity player);
    void onDamageTaken(float amount);
    void onDestroyed();
}