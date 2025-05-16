package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.commonweapon.WeaponType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for different weapon types.
 * Manages weapon configurations and properties.
 */
public class WeaponRegistry {
    private static final Logger LOGGER = Logger.getLogger(WeaponRegistry.class.getName());
    private static final WeaponRegistry INSTANCE = new WeaponRegistry();

    private final Map<String, WeaponType> weaponTypes = new HashMap<>();

    /**
     * Create a new weapon registry and register weapon types
     */
    private WeaponRegistry() {
        // Register weapon types
        registerWeaponTypes();

        LOGGER.log(Level.INFO, "WeaponRegistry initialized with {0} weapon types",
                weaponTypes.size());
    }

    /**
     * Get the singleton instance
     *
     * @return The WeaponRegistry instance
     */
    public static WeaponRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register default weapon types
     */
    private void registerWeaponTypes() {
        // Automatic weapon
        registerWeaponType("automatic", new WeaponType.Builder()
                .type(WeaponType.FiringPattern.AUTOMATIC)
                .damage(10.0f)
                .projectileSpeed(8.0f)
                .cooldownTime(10)
                .defaultBulletType("standard")
                .build());

        // Burst weapon
        registerWeaponType("burst", new WeaponType.Builder()
                .type(WeaponType.FiringPattern.BURST)
                .damage(15.0f)
                .projectileSpeed(10.0f)
                .cooldownTime(30)
                .burstCount(3)
                .burstDelay(3)
                .defaultBulletType("standard")
                .build());

        // Heavy weapon
        registerWeaponType("heavy", new WeaponType.Builder()
                .type(WeaponType.FiringPattern.HEAVY)
                .damage(30.0f)
                .projectileSpeed(6.0f)
                .cooldownTime(60)
                .defaultBulletType("heavy")
                .build());

        // Shotgun
        registerWeaponType("shotgun", new WeaponType.Builder()
                .type(WeaponType.FiringPattern.SHOTGUN)
                .damage(7.0f)
                .projectileSpeed(7.0f)
                .cooldownTime(25)
                .shotCount(5)
                .spreadAngle(30)
                .defaultBulletType("standard")
                .build());
    }

    /**
     * Register a weapon type
     *
     * @param name Weapon type name
     * @param type Weapon type configuration
     */
    public void registerWeaponType(String name, WeaponType type) {
        weaponTypes.put(name.toLowerCase(), type);
        LOGGER.log(Level.FINE, "Registered weapon type: {0}", name);
    }

    /**
     * Get a weapon type by name
     *
     * @param name Weapon type name
     * @return Weapon type or null if not found
     */
    public WeaponType getWeaponType(String name) {
        return weaponTypes.getOrDefault(name.toLowerCase(), weaponTypes.get("automatic"));
    }

    /**
     * Get all available weapon type names
     *
     * @return Set of weapon type names
     */
    public Set<String> getAvailableWeaponTypes() {
        return weaponTypes.keySet();
    }

    /**
     * Check if a weapon type exists
     *
     * @param name Weapon type name
     * @return true if exists
     */
    public boolean hasWeaponType(String name) {
        return weaponTypes.containsKey(name.toLowerCase());
    }
}