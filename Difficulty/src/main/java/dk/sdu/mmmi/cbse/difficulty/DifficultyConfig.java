package dk.sdu.mmmi.cbse.difficulty;

/**
 * Configuration for difficulty scaling curves.
 * Defines how each parameter scales with difficulty level.
 */
public class DifficultyConfig {

    // Base parameters (difficulty 0.0)
    private static final int BASE_MAX_ENEMIES = 3;
    private static final int BASE_MAX_ASTEROIDS = 4;
    private static final float BASE_ENEMY_SPAWN_MULTIPLIER = 1.0f;
    private static final float BASE_HUNTER_SPEED_MULTIPLIER = 1.0f;
    private static final float BASE_HUNTER_FIRING_RATE_MULTIPLIER = 1.0f;

    // Max parameters (difficulty 2.0+)
    private static final int MAX_ENEMIES_LIMIT = 8;
    private static final int MAX_ASTEROIDS_LIMIT = 10;
    private static final float MAX_ENEMY_SPAWN_MULTIPLIER = 2.0f;
    private static final float MIN_HUNTER_SPEED_MULTIPLIER = 0.6f; // 60% of original speed
    private static final float MAX_HUNTER_FIRING_RATE_MULTIPLIER = 2.5f; // 2.5x interval = 40% firing rate

    // Time scaling
    private static final float DIFFICULTY_TIME_SCALE = 120.0f; // 2 minutes to reach difficulty 1.0

    /**
     * Calculate difficulty level based on elapsed game time.
     * Uses logarithmic curve for gradual progression.
     */
    public float calculateDifficultyLevel(float elapsedTimeSeconds) {
        if (elapsedTimeSeconds <= 0) {
            return 0.0f;
        }

        // Logarithmic scaling: starts fast, then slows down
        float normalizedTime = elapsedTimeSeconds / DIFFICULTY_TIME_SCALE;
        return (float)(Math.log(1 + normalizedTime * 2) / Math.log(3)) * 2.0f; // Max difficulty ~2.0
    }

    /**
     * Calculate maximum enemy count for difficulty level.
     * Linear scaling from base to maximum.
     */
    public int calculateMaxEnemies(float difficulty) {
        float progress = Math.min(difficulty / 2.0f, 1.0f);
        return BASE_MAX_ENEMIES + Math.round(progress * (MAX_ENEMIES_LIMIT - BASE_MAX_ENEMIES));
    }

    /**
     * Calculate maximum asteroid count for difficulty level.
     * Linear scaling from base to maximum.
     */
    public int calculateMaxAsteroids(float difficulty) {
        float progress = Math.min(difficulty / 2.0f, 1.0f);
        return BASE_MAX_ASTEROIDS + Math.round(progress * (MAX_ASTEROIDS_LIMIT - BASE_MAX_ASTEROIDS));
    }

    /**
     * Calculate enemy spawn probability multiplier.
     * Higher difficulty = more frequent spawning.
     */
    public float calculateEnemySpawnMultiplier(float difficulty) {
        float progress = Math.min(difficulty / 2.0f, 1.0f);
        return BASE_ENEMY_SPAWN_MULTIPLIER + (progress * (MAX_ENEMY_SPAWN_MULTIPLIER - BASE_ENEMY_SPAWN_MULTIPLIER));
    }

    /**
     * Calculate hunter speed multiplier.
     * Higher difficulty = slower hunters for balance.
     */
    public float calculateHunterSpeedMultiplier(float difficulty) {
        float progress = Math.min(difficulty / 2.0f, 1.0f);
        return BASE_HUNTER_SPEED_MULTIPLIER - (progress * (BASE_HUNTER_SPEED_MULTIPLIER - MIN_HUNTER_SPEED_MULTIPLIER));
    }

    /**
     * Calculate hunter firing rate multiplier.
     * Higher difficulty = longer intervals between shots.
     */
    public float calculateHunterFiringRateMultiplier(float difficulty) {
        float progress = Math.min(difficulty / 2.0f, 1.0f);
        return BASE_HUNTER_FIRING_RATE_MULTIPLIER + (progress * (MAX_HUNTER_FIRING_RATE_MULTIPLIER - BASE_HUNTER_FIRING_RATE_MULTIPLIER));
    }
}