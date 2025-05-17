package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service Provider Interface for weapon functionality.
 */
public interface IWeaponSPI {
    /**
     * Create a new bullet entity based on weapon configuration
     *
     * @param shooter The entity that is shooting
     * @param gameData Current game state
     * @param bulletType Type of bullet to create
     * @return New bullet entity
     */
    Entity createBullet(Entity shooter, GameData gameData, String bulletType);

    /**
     * Process weapon firing behavior
     *
     * @param entity Entity with weapon component
     * @param gameData Current game state
     * @param world Game world containing entities
     */
    void processFiring(Entity entity, GameData gameData, World world);

    /**
     * Get information about available weapon types
     *
     * @return Array of available weapon type names
     */
    String[] getAvailableWeaponTypes();
}