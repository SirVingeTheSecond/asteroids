package dk.sdu.mmmi.cbse.difficulty;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commondifficulty.DifficultyParameters;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.commondifficulty.events.DifficultyChangedEvent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages difficulty progression over time.
 */
public class DifficultySystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(DifficultySystem.class.getName());

    private static final float DIFFICULTY_CHECK_INTERVAL = 5.0f;

    private IDifficultyService difficultyService;
    private IEventService eventService;
    private DifficultyParameters lastParameters;
    private float lastDifficultyCheck = 0.0f;

    public DifficultySystem() {
        this.difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        this.lastParameters = DifficultyParameters.initial();

        LOGGER.log(Level.INFO, "DifficultySystem initialized");
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public void update(GameData gameData, World world) {
        if (difficultyService == null || eventService == null) {
            return;
        }

        if (difficultyService instanceof DifficultyService service) {
            service.updateDifficulty(gameData);
        }

        float deltaTime = Time.getDeltaTimeF();
        lastDifficultyCheck += deltaTime;

        if (lastDifficultyCheck >= DIFFICULTY_CHECK_INTERVAL) {
            checkForDifficultyChange();
            lastDifficultyCheck = 0.0f;
        }
    }

    private void checkForDifficultyChange() {
        DifficultyParameters currentParameters = new DifficultyParameters(
                difficultyService.getCurrentDifficulty(),
                difficultyService.getMaxEnemyCount(),
                difficultyService.getMaxAsteroidCount(),
                difficultyService.getEnemySpawnMultiplier(),
                difficultyService.getHunterSpeedMultiplier(),
                difficultyService.getHunterFiringRateMultiplier()
        );

        // Check if any parameter changed
        if (!parametersEqual(lastParameters, currentParameters)) {
            publishDifficultyChange(lastParameters, currentParameters);
            lastParameters = currentParameters;
        }
    }

    private boolean parametersEqual(DifficultyParameters p1, DifficultyParameters p2) {
        return Math.abs(p1.difficultyLevel() - p2.difficultyLevel()) < 0.01f &&
                p1.maxEnemies() == p2.maxEnemies() &&
                p1.maxAsteroids() == p2.maxAsteroids() &&
                Math.abs(p1.enemySpawnMultiplier() - p2.enemySpawnMultiplier()) < 0.01f &&
                Math.abs(p1.hunterSpeedMultiplier() - p2.hunterSpeedMultiplier()) < 0.01f &&
                Math.abs(p1.hunterFiringRateMultiplier() - p2.hunterFiringRateMultiplier()) < 0.01f;
    }

    private void publishDifficultyChange(DifficultyParameters oldParams, DifficultyParameters newParams) {
        DifficultyChangedEvent event = new DifficultyChangedEvent(oldParams, newParams);
        eventService.publish(event);

        LOGGER.log(Level.INFO, "Difficulty changed: Level {0} -> {1}, Enemies {2} -> {3}, Asteroids {4} -> {5}",
                new Object[]{oldParams.difficultyLevel(), newParams.difficultyLevel(),
                        oldParams.maxEnemies(), newParams.maxEnemies(),
                        oldParams.maxAsteroids(), newParams.maxAsteroids()});
    }
}