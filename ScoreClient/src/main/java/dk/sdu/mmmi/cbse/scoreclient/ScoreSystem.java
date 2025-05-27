package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages the player's score using Spring RestTemplate to communicate with the Score MicroService.
 * Implements IPluginService to ensure proper CBSE lifecycle integration.
 */
public class ScoreSystem implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystem.class.getName());

    private IEventService eventService;
    private final ScoreService scoreService;
    private final EnemyScoreListener enemyScoreListener;
    private final AsteroidScoreListener asteroidScoreListener;

    public ScoreSystem() {
        this.scoreService = new ScoreService();

        // Create listeners
        this.enemyScoreListener = new EnemyScoreListener(scoreService);
        this.asteroidScoreListener = new AsteroidScoreListener(scoreService);

        LOGGER.log(Level.INFO, "ScoreSystem created - waiting for start() to subscribe to events");
    }

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

    /**
     * Get current score
     * Delegates to the score service
     *
     * @return Current score
     */
    public int getScore() {
        return scoreService.getScore();
    }

    /**
     * Reset score to zero
     * Delegates to the score service following composition pattern
     */
    public void resetScore() {
        scoreService.resetScore();
    }

    /**
     * Manually add score (for testing or special events)
     * Delegates to the score service following composition pattern
     *
     * @param points Points to add
     */
    public void addScore(int points) {
        scoreService.addScore(points);
    }

    /**
     * Get the microservice URL for configuration purposes
     *
     * @return The scoring service URL
     */
    public String getScoringServiceUrl() {
        return scoreService.getScoringServiceUrl();
    }

    /**
     * Check if the microservice is available
     *
     * @return true if the service responds to requests
     */
    public boolean isMicroserviceAvailable() {
        return scoreService.isMicroserviceAvailable();
    }

    /**
     * Get reference to the enemy score listener (for testing)
     *
     * @return The enemy score listener
     */
    public EnemyScoreListener getEnemyScoreListener() {
        return enemyScoreListener;
    }

    /**
     * Get reference to the asteroid score listener (for testing)
     *
     * @return The asteroid score listener
     */
    public AsteroidScoreListener getAsteroidScoreListener() {
        return asteroidScoreListener;
    }

    /**
     * Get reference to the score service (for testing)
     *
     * @return The score service
     */
    public ScoreService getScoreService() {
        return scoreService;
    }
}