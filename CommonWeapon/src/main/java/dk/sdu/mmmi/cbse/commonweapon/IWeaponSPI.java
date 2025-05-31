package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;

import java.util.List;

/**
 * Service Provider Interface for weapon functionality.
 */
public interface IWeaponSPI {
    /**
     * Create bullets and shoot them from the entity
     *
     * @param shooter Entity that is shooting
     * @param gameData Current game state
     * @param bulletType Type of bullet to create
     * @return List of bullet entities created (empty list if none created)
     */
    List<Entity> shoot(Entity shooter, GameData gameData, String bulletType);

    /**
     * Get the default weapon
     * @return Default weapon configuration
     */
    Weapon getWeapon();

    /**
     * Get a specific weapon by name
     * @param weaponName The name of the weapon to retrieve
     * @return Weapon configuration
     */
    default Weapon getWeapon(String weaponName) {
        return getWeapon();
    }
}