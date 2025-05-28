package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.commonweapon.Weapon;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for weapons with tactical balance and distinct roles.
 */
public class WeaponRegistry {
    private static final Logger LOGGER = Logger.getLogger(WeaponRegistry.class.getName());
    private static final WeaponRegistry INSTANCE = new WeaponRegistry();

    private final Map<String, Weapon> weapons = new HashMap<>();

    private WeaponRegistry() {
        registerWeapons();
        LOGGER.log(Level.INFO, "WeaponRegistry initialized with {0} weapon types", weapons.size());
    }

    public static WeaponRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register all weapon configurations with exact tactical specifications
     */
    private void registerWeapons() {
        // AUTOMATIC
        registerWeapon("automatic", new Weapon.Builder()
                .type(Weapon.FiringPattern.AUTOMATIC)
                .damage(1.0f)                    // 1 damage per bullet
                .projectileSpeed(400.0f)         // Fast projectiles
                .cooldownTime(0.15f)             // ~6.7 shots per second
                .defaultBulletType("tiny")       // Tiny bullets (2.5f radius)
                .build());

        // BURST
        registerWeapon("burst", new Weapon.Builder()
                .type(Weapon.FiringPattern.BURST)
                .damage(1.0f)                    // 1 damage per bullet
                .projectileSpeed(350.0f)         // Medium speed
                .cooldownTime(0.8f)              // Time between bursts
                .burstCount(3)                   // 3-shot bursts
                .burstDelay(0.08f)               // Fast burst shots
                .defaultBulletType("standard")   // Standard bullets (4.0f radius)
                .build());

        // HEAVY
        registerWeapon("heavy", new Weapon.Builder()
                .type(Weapon.FiringPattern.HEAVY)
                .damage(2.0f)                    // 2 damage per bullet (double!)
                .projectileSpeed(280.0f)         // Slower bullets
                .cooldownTime(1.2f)              // Slow firing
                .defaultBulletType("heavy")      // Large bullets (6.5f radius)
                .build());

        // SHOTGUN
        registerWeapon("shotgun", new Weapon.Builder()
                .type(Weapon.FiringPattern.SHOTGUN)
                .damage(1.0f)                    // 1 damage per pellet
                .projectileSpeed(320.0f)         // Medium speed
                .cooldownTime(0.9f)              // Moderate firing rate
                .shotCount(5)                    // 5 pellets
                .spreadAngle(35.0f)              // Wide spread
                .defaultBulletType("standard")   // Standard size pellets
                .build());
    }

    public void registerWeapon(String name, Weapon weapon) {
        weapons.put(name.toLowerCase(), weapon);
        LOGGER.log(Level.FINE, "Registered weapon: {0} with damage: {1}",
                new Object[]{name, weapon.getDamage()});
    }

    public Weapon getWeapon(String name) {
        return weapons.getOrDefault(name.toLowerCase(), weapons.get("automatic"));
    }

    public Set<String> getAvailableWeapons() {
        return weapons.keySet();
    }
}