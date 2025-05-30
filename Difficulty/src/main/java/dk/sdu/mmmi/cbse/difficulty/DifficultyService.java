package dk.sdu.mmmi.cbse.difficulty;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for difficulty management.
 * Calculates difficulty parameters based on elapsed game time.
 */
public class DifficultyService implements IDifficultyService {
    private static final Logger LOGGER = Logger.getLogger(DifficultyService.class.getName());

    private final DifficultyConfig config;
    private double gameStartTime;
    private float currentDifficulty = 0.0f;

    public DifficultyService() {
        this.config = new DifficultyConfig();
        this.gameStartTime = Time.getTime();

        LOGGER.log(Level.INFO, "DifficultyService initialized at time {0}", gameStartTime);
    }

    @Override
    public float getCurrentDifficulty() {
        return currentDifficulty;
    }

    @Override
    public int getMaxEnemyCount() {
        return config.calculateMaxEnemies(currentDifficulty);
    }

    @Override
    public int getMaxAsteroidCount() {
        return config.calculateMaxAsteroids(currentDifficulty);
    }

    @Override
    public float getEnemySpawnMultiplier() {
        return config.calculateEnemySpawnMultiplier(currentDifficulty);
    }

    @Override
    public float getHunterSpeedMultiplier() {
        return config.calculateHunterSpeedMultiplier(currentDifficulty);
    }

    @Override
    public float getHunterFiringRateMultiplier() {
        return config.calculateHunterFiringRateMultiplier(currentDifficulty);
    }

    @Override
    public void reset() {
        gameStartTime = Time.getTime();
        currentDifficulty = 0.0f;

        LOGGER.log(Level.INFO, "Difficulty reset at time {0}", gameStartTime);
    }

    /**
     * Update difficulty based on elapsed game time.
     * Called by DifficultySystem to recalculate current difficulty.
     */
    public void updateDifficulty(GameData gameData) {
        double currentTime = Time.getTime();
        float elapsedTime = (float)(currentTime - gameStartTime);
        currentDifficulty = config.calculateDifficultyLevel(elapsedTime);
    }
}