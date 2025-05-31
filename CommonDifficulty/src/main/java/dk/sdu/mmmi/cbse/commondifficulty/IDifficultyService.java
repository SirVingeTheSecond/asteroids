package dk.sdu.mmmi.cbse.commondifficulty;

/**
 * Service Provider Interface for difficulty management.
 * Provides difficulty parameters that scale over time.
 */
public interface IDifficultyService {
    /**
     * Get current difficulty level (0.0 at start, increases over time)
     *
     * @return Current difficulty level
     */
    float getCurrentDifficulty();

    /**
     * Get maximum enemy count for current difficulty
     *
     * @return Maximum number of enemies that should exist
     */
    int getMaxEnemyCount();

    /**
     * Get maximum asteroid count for current difficulty
     *
     * @return Maximum number of asteroids that should exist
     */
    int getMaxAsteroidCount();

    /**
     * Get enemy spawn probability multiplier for current difficulty
     *
     * @return Spawn probability multiplier (1.0 = normal)
     */
    float getEnemySpawnMultiplier();

    /**
     * Get hunter speed multiplier for current difficulty
     * Higher difficulty = slower hunters for balance
     *
     * @return Speed multiplier (1.0 = normal, <1.0 = slower)
     */
    float getHunterSpeedMultiplier();

    /**
     * Get hunter firing rate multiplier for current difficulty
     * Higher difficulty = longer intervals between shots
     *
     * @return Firing rate multiplier (1.0 = normal, >1.0 = longer intervals)
     */
    float getHunterFiringRateMultiplier();

    /**
     * Reset difficulty to initial state
     */
    void reset();
}