package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.commonbullet.BulletType;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for different bullet types with correct damage values.
 */
public class BulletRegistry {
    private static final Logger LOGGER = Logger.getLogger(BulletRegistry.class.getName());

    private static final BulletRegistry INSTANCE = new BulletRegistry();

    private final Map<String, BulletType> bulletTypes = new HashMap<>();

    /**
     * Create a new bullet registry and register bullet types
     */
    private BulletRegistry() {
        // Register bullet types
        registerBulletTypes();

        LOGGER.log(Level.INFO, "BulletRegistry initialized with {0} bullet types",
                bulletTypes.size());
    }

    /**
     * Get the singleton instance
     *
     * @return The BulletRegistry instance
     */
    public static BulletRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register default bullet types with CORRECT damage values.
     */
    private void registerBulletTypes() {
        // Standard bullet
        registerBulletType("standard", new BulletType.Builder()
                .speed(300.0f)
                .damage(1.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.GOLD)
                .build());

        // Piercing bullet
        registerBulletType("piercing", new BulletType.Builder()
                .speed(320.0f)
                .damage(1.0f)
                .piercing(true)
                .pierceCount(2)
                .bouncing(false)
                .color(Color.DODGERBLUE)
                .build());

        // Bouncing bullet
        registerBulletType("bouncing", new BulletType.Builder()
                .speed(280.0f)
                .damage(1.0f)
                .piercing(false)
                .bouncing(true)
                .bounceCount(3)
                .color(Color.LIME)
                .build());

        // Heavy bullet
        registerBulletType("heavy", new BulletType.Builder()
                .speed(250.0f)
                .damage(2.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.ORANGERED)
                .build());
    }

    /**
     * Register a bullet type
     *
     * @param name Bullet type name
     * @param type Bullet type configuration
     */
    public void registerBulletType(String name, BulletType type) {
        bulletTypes.put(name.toLowerCase(), type);
        LOGGER.log(Level.FINE, "Registered bullet type: {0} with damage: {1}",
                new Object[]{name, type.getDamage()});
    }

    /**
     * Get a bullet type by name
     *
     * @param name Bullet type name
     * @return Bullet type or null if not found
     */
    public BulletType getBulletType(String name) {
        return bulletTypes.getOrDefault(name.toLowerCase(),
                bulletTypes.get("standard")); // Default to standard if not found
    }

    /**
     * Get all available bullet type names
     *
     * @return Set of bullet type names
     */
    public Set<String> getAvailableBulletTypes() {
        return bulletTypes.keySet();
    }

    /**
     * Check if a bullet type exists
     *
     * @param name Bullet type name
     * @return true if exists
     */
    public boolean hasBulletType(String name) {
        return bulletTypes.containsKey(name.toLowerCase());
    }
}