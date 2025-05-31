package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.services.IScoreSPI;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages the player's score using Spring RestTemplate to communicate with the Score MicroService.
 */
public class ScoreSystem implements IPluginService, IScoreSPI {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystem.class.getName());

    private IEventService eventService;
    private final ScoreService scoreService;
    private final EnemyScoreListener enemyScoreListener;
    private final AsteroidScoreListener asteroidScoreListener;

    public ScoreSystem() {
        this.scoreService = new ScoreService();

        this.enemyScoreListener = new EnemyScoreListener(scoreService);
        this.asteroidScoreListener = new AsteroidScoreListener(scoreService);

        LOGGER.log(Level.INFO, "ScoreSystem created - waiting for start() to subscribe to events");
    }

    // ===== IPluginService Implementation =====

    @Override
    public void start(GameData gameData, World world) {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        if (eventService != null) {
            eventService.subscribe(EnemyDestroyedEvent.class, enemyScoreListener);
            eventService.subscribe(AsteroidSplitEvent.class, asteroidScoreListener);

            LOGGER.log(Level.INFO, "ScoreSystem started - subscribed listeners for enemy and asteroid events");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available - scoring events will not be processed");
        }
    }

    @Override
    public void stop(GameData gameData, World world) {
        if (eventService != null) {
            eventService.unsubscribe(EnemyDestroyedEvent.class, enemyScoreListener);
            eventService.unsubscribe(AsteroidSplitEvent.class, asteroidScoreListener);
            LOGGER.log(Level.INFO, "ScoreSystem stopped - unsubscribed all listeners");
        }
    }

    @Override
    public int getCurrentScore() {
        return scoreService.getScore();
    }

    @Override
    public void addScore(int points) {
        scoreService.addScore(points);
        LOGGER.log(Level.FINE, "Score updated via IScoreSPI: +{0}, total: {1}",
                new Object[]{points, scoreService.getScore()});
    }

    @Override
    public void resetScore() {
        scoreService.resetScore();
        LOGGER.log(Level.INFO, "Score reset via IScoreSPI to: {0}", scoreService.getScore());
    }

    @Override
    public boolean isServiceAvailable() {
        return scoreService.isMicroserviceAvailable();
    }

    @Override
    public String getServiceInfo() {
        if (scoreService.isMicroserviceAvailable()) {
            return "Microservice: " + scoreService.getScoringServiceUrl();
        } else {
            return "Fallback (microservice unavailable)";
        }
    }

    // ===== for backward compatibility =====

    /**
     * Get current score (legacy method)
     * @deprecated Use IScoreSPI.getCurrentScore() instead
     */
    @Deprecated
    public int getScore() {
        return getCurrentScore();
    }

    /**
     * Get the microservice URL (legacy method)
     * @deprecated Use IScoreSPI.getServiceInfo() instead
     */
    @Deprecated
    public String getScoringServiceUrl() {
        return scoreService.getScoringServiceUrl();
    }

    /**
     * Check microservice availability (legacy method)
     * @deprecated Use IScoreSPI.isServiceAvailable() instead
     */
    @Deprecated
    public boolean isMicroserviceAvailable() {
        return isServiceAvailable();
    }

    // ===== for testing =====

    /**
     * Get reference to the enemy score listener (for testing)
     */
    public EnemyScoreListener getEnemyScoreListener() {
        return enemyScoreListener;
    }

    /**
     * Get reference to the asteroid score listener (for testing)
     */
    public AsteroidScoreListener getAsteroidScoreListener() {
        return asteroidScoreListener;
    }

    /**
     * Get reference to the score service (for testing)
     */
    public ScoreService getScoreService() {
        return scoreService;
    }
}