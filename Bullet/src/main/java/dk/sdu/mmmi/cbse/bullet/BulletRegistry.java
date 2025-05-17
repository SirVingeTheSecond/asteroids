package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.commonbullet.BulletType;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for different bullet types.
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
     * Register default bullet types
     */
    private void registerBulletTypes() {
        // Standard bullet - balanced speed and damage
        registerBulletType("standard", new BulletType.Builder()
                .speed(5.0f)
                .damage(10.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.YELLOW)
                .build());

        // Piercing bullet - penetrates enemies
        registerBulletType("piercing", new BulletType.Builder()
                .speed(6.0f)
                .damage(15.0f)
                .piercing(true)
                .pierceCount(2)
                .bouncing(false)
                .color(Color.BLUE)
                .build());

        // Bouncing bullet - bounces off walls
        registerBulletType("bouncing", new BulletType.Builder()
                .speed(4.5f)
                .damage(8.0f)
                .piercing(false)
                .bouncing(true)
                .bounceCount(3)
                .color(Color.GREEN)
                .build());

        // Heavy bullet - high damage, slow speed
        registerBulletType("heavy", new BulletType.Builder()
                .speed(3.5f)
                .damage(30.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.ORANGE)
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
        LOGGER.log(Level.FINE, "Registered bullet type: {0}", name);
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