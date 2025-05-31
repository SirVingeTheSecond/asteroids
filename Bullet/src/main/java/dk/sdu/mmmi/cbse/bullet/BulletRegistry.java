package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.commonbullet.BulletType;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for bullet types.
 */
public class BulletRegistry {
    private static final Logger LOGGER = Logger.getLogger(BulletRegistry.class.getName());
    private static final BulletRegistry INSTANCE = new BulletRegistry();

    private final Map<String, BulletType> bulletTypes = new HashMap<>();

    private BulletRegistry() {
        registerBulletTypes();
        LOGGER.log(Level.INFO, "BulletRegistry initialized with {0} bullet types", bulletTypes.size());
    }

    public static BulletRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register bullet types
     */
    private void registerBulletTypes() {
        // Tiny bullets
        registerBulletType("tiny", new BulletType.Builder()
                .speed(400.0f)
                .damage(1.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.LIGHTBLUE)
                .build());

        // Standard bullets
        registerBulletType("standard", new BulletType.Builder()
                .speed(350.0f)
                .damage(1.0f)
                .piercing(false)
                .bouncing(false)
                .color(Color.GOLD)
                .build());

        // Heavy bullets
        registerBulletType("heavy", new BulletType.Builder()
                .speed(280.0f)
                .damage(2f)
                .piercing(false)
                .bouncing(false)
                .color(Color.ORANGERED)
                .build());

        // Piercing bullets
        registerBulletType("piercing", new BulletType.Builder()
                .speed(320.0f)
                .damage(1.0f)
                .piercing(true)
                .pierceCount(2)
                .bouncing(false)
                .color(Color.DODGERBLUE)
                .build());

        // Bouncing bullets
        registerBulletType("bouncing", new BulletType.Builder()
                .speed(280.0f)
                .damage(1f)
                .piercing(false)
                .bouncing(true)
                .bounceCount(3)
                .color(Color.LIME)
                .build());
    }

    public void registerBulletType(String name, BulletType type) {
        bulletTypes.put(name.toLowerCase(), type);
        LOGGER.log(Level.FINE, "Registered bullet type: {0} with damage: {1}",
                new Object[]{name, type.getDamage()});
    }

    public BulletType getBulletType(String name) {
        return bulletTypes.getOrDefault(name.toLowerCase(), bulletTypes.get("standard"));
    }
}