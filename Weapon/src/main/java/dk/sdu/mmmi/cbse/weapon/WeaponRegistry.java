package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.commonweapon.Weapon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for different weapons.
 */
public class WeaponRegistry {
    private static final Logger LOGGER = Logger.getLogger(WeaponRegistry.class.getName());
    private static final WeaponRegistry INSTANCE = new WeaponRegistry();

    private final Map<String, Weapon> weapon = new HashMap<>();

    /**
     * Create a new weapon registry and register weapons
     */
    private WeaponRegistry() {
        registerWeapons();

        LOGGER.log(Level.INFO, "WeaponRegistry initialized with {0} weapon types",
                weapon.size());
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
     * Register default weapons
     */
    private void registerWeapons() {
        // Automatic
        registerWeapon("automatic", new Weapon.Builder()
                .type(Weapon.FiringPattern.AUTOMATIC)
                .damage(10.0f)
                .projectileSpeed(8.0f)
                .cooldownTime(10)
                .defaultBulletType("standard")
                .build());

        // Burst
        registerWeapon("burst", new Weapon.Builder()
                .type(Weapon.FiringPattern.BURST)
                .damage(15.0f)
                .projectileSpeed(10.0f)
                .cooldownTime(30)
                .burstCount(3)
                .burstDelay(3)
                .defaultBulletType("standard")
                .build());

        // Heavy
        registerWeapon("heavy", new Weapon.Builder()
                .type(Weapon.FiringPattern.HEAVY)
                .damage(30.0f)
                .projectileSpeed(6.0f)
                .cooldownTime(60)
                .defaultBulletType("heavy")
                .build());

        // Shotgun
        registerWeapon("shotgun", new Weapon.Builder()
                .type(Weapon.FiringPattern.SHOTGUN)
                .damage(7.0f)
                .projectileSpeed(7.0f)
                .cooldownTime(25)
                .shotCount(5)
                .spreadAngle(30)
                .defaultBulletType("standard")
                .build());
    }

    /**
     * Register a weapon
     *
     * @param name Weapon name
     * @param type Weapon configuration
     */
    public void registerWeapon(String name, Weapon type) {
        weapon.put(name.toLowerCase(), type);
        LOGGER.log(Level.FINE, "Registered weapon type: {0}", name);
    }

    /**
     * Get a weapon by name
     *
     * @param name Weapon name
     * @return Weapon or null if not found
     */
    public Weapon getWeapon(String name) {
        return weapon.getOrDefault(name.toLowerCase(), weapon.get("automatic"));
    }

    /**
     * Get all available weapon type names
     *
     * @return Set of weapon type names
     */
    public Set<String> getAvailableWeapons() {
        return weapon.keySet();
    }

    /**
     * Check if a weapon exists
     *
     * @param name Weapon name
     * @return true if exists
     */
    public boolean hasWeapon(String name) {
        return weapon.containsKey(name.toLowerCase());
    }
}