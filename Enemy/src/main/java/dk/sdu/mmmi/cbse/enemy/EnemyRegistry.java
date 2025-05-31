package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.commonenemy.EnemyType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for Enemy configurations.
 */
public class EnemyRegistry {
    private static final Logger LOGGER = Logger.getLogger(EnemyRegistry.class.getName());
    private static final EnemyRegistry INSTANCE = new EnemyRegistry();

    private final Map<String, EnemyConfig> enemyConfigs = new HashMap<>();

    private EnemyRegistry() {
        registerEnemyConfigurations();
        LOGGER.log(Level.INFO, "EnemyRegistry initialized with {0} enemy configurations", enemyConfigs.size());
    }

    public static EnemyRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register HUNTER and TURRET configurations
     */
    private void registerEnemyConfigurations() {
        // HUNTER
        registerEnemyConfig("hunter", new EnemyConfig.Builder()
                .type(EnemyType.HUNTER)
                .health(1f)
                .scoreValue(200)
                .speed(90f)
                .rotationSpeed(120f)
                .firingProbability(0.012f)
                .fireDistance(350f)
                .weaponType("burst")
                .bulletType("standard")
                .build());

        // TURRET
        registerEnemyConfig("turret", new EnemyConfig.Builder()
                .type(EnemyType.TURRET)
                .health(1f)
                .scoreValue(150)
                .speed(0f)
                .rotationSpeed(80f)
                .firingProbability(0.015f)
                .fireDistance(300f)
                .weaponType("shotgun")
                .bulletType("standard")
                .build());
    }

    public void registerEnemyConfig(String name, EnemyConfig config) {
        enemyConfigs.put(name.toLowerCase(), config);
        LOGGER.log(Level.FINE, "Registered enemy configuration: {0}", name);
    }

    public EnemyConfig getEnemyConfig(String name) {
        return enemyConfigs.getOrDefault(name.toLowerCase(), enemyConfigs.get("hunter"));
    }

    public EnemyConfig getEnemyConfigByType(EnemyType type) {
        return enemyConfigs.values().stream()
                .filter(config -> config.getType() == type)
                .findFirst()
                .orElse(enemyConfigs.get("hunter"));
    }
}