package dk.sdu.mmmi.cbse.commondifficulty;

/**
 * Immutable container for difficulty parameters.
 * Used to pass difficulty state between systems.
 */
public record DifficultyParameters(
        float difficultyLevel,
        int maxEnemies,
        int maxAsteroids,
        float enemySpawnMultiplier,
        float hunterSpeedMultiplier,
        float hunterFiringRateMultiplier
) {
    /**
     * Create initial difficulty parameters
     */
    public static DifficultyParameters initial() {
        return new DifficultyParameters(0.0f, 3, 4, 1.0f, 1.0f, 1.0f);
    }
}