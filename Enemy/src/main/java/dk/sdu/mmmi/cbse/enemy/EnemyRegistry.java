package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.commonenemy.EnemyType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Registry for different enemy configurations.
 */
public class EnemyRegistry {
    private static final Logger LOGGER = Logger.getLogger(EnemyRegistry.class.getName());

    private static final EnemyRegistry INSTANCE = new EnemyRegistry();

    private final Map<String, EnemyConfig> enemyConfigs = new HashMap<>();

    /**
     * Create a new enemy registry and register enemy types
     */
    private EnemyRegistry() {
        registerEnemyConfigurations();

        LOGGER.log(Level.INFO, "EnemyRegistry initialized with {0} enemy configurations",
                enemyConfigs.size());
    }

    /**
     * Get the singleton instance
     *
     * @return The EnemyRegistry instance
     */
    public static EnemyRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Register default enemy configurations
     */
    private void registerEnemyConfigurations() {
        // Basic enemy
        registerEnemyConfig("basic", new EnemyConfig.Builder()
                .type(EnemyType.BASIC)
                .health(100.0f)
                .scoreValue(100)
                .speed(80.0f)
                .rotationSpeed(1.0f)
                .firingProbability(0.005f)
                .fireDistance(300.0f)
                .weaponType("automatic")
                .bulletType("standard")
                .build());

        // Hunter enemy
        registerEnemyConfig("hunter", new EnemyConfig.Builder()
                .type(EnemyType.HUNTER)
                .health(150.0f)
                .scoreValue(200)
                .speed(120.0f)
                .rotationSpeed(2.0f)
                .firingProbability(0.01f)
                .fireDistance(400.0f)
                .weaponType("burst")
                .bulletType("standard")
                .build());

        // Turret enemy
        registerEnemyConfig("turret", new EnemyConfig.Builder()
                .type(EnemyType.TURRET)
                .health(80.0f)
                .scoreValue(150)
                .speed(0.0f)
                .rotationSpeed(1.0f)
                .firingProbability(0.02f)
                .fireDistance(350.0f)
                .weaponType("shotgun")
                .bulletType("standard")
                .build());
    }

    /**
     * Register an enemy configuration
     *
     * @param name Configuration name
     * @param config Enemy configuration
     */
    public void registerEnemyConfig(String name, EnemyConfig config) {
        enemyConfigs.put(name.toLowerCase(), config);
        LOGGER.log(Level.FINE, "Registered enemy configuration: {0}", name);
    }

    /**
     * Get an enemy configuration by name
     *
     * @param name Configuration name
     * @return Enemy configuration or default if not found
     */
    public EnemyConfig getEnemyConfig(String name) {
        return enemyConfigs.getOrDefault(name.toLowerCase(),
                enemyConfigs.get("basic")); // Default to basic if not found
    }

    /**
     * Get an enemy configuration by type
     *
     * @param type Enemy type
     * @return Enemy configuration
     */
    public EnemyConfig getEnemyConfigByType(EnemyType type) {
        // Find the first configuration matching the type
        return enemyConfigs.values().stream()
                .filter(config -> config.getType() == type)
                .findFirst()
                .orElse(enemyConfigs.get("basic")); // Default to basic if not found
    }

    /**
     * Get all available enemy configuration names
     *
     * @return Set of configuration names
     */
    public Set<String> getAvailableEnemyConfigs() {
        return enemyConfigs.keySet();
    }

    /**
     * Check if an enemy configuration exists
     *
     * @param name Configuration name
     * @return true if exists
     */
    public boolean hasEnemyConfig(String name) {
        return enemyConfigs.containsKey(name.toLowerCase());
    }
}