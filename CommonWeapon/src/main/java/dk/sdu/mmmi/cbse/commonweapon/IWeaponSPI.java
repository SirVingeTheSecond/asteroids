package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;

/**
 * Service Provider Interface for weapon functionality.
 */
public interface IWeaponSPI {
    /**
     * Create a bullet and shoot it from the entity
     *
     * @param shooter Entity that is shooting
     * @param gameData Current game state
     * @param bulletType Type of bullet to create
     */
    void shoot(Entity shooter, GameData gameData, String bulletType);

    Weapon getWeapon();
}