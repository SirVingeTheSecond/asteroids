package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages the player's score using Spring RestTemplate to communicate with the Score MicroService.
 * Follows CBSE principles by implementing standard service interfaces and maintaining loose coupling.
 */
public class ScoreSystem implements IUpdate, IEventListener<EnemyDestroyedEvent> {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystem.class.getName());

    private final IEventService eventService;
    private final RestTemplate restTemplate;
    private final String scoringServiceUrl;

    // Fallback local score if microservice is unavailable
    private int fallbackScore = 0;

    /**
     * Constructor initializes the scoring system with Spring RestTemplate.
     * Maintains CBSE principles through dependency injection and service discovery.
     */
    public ScoreSystem() {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        this.restTemplate = ScoreServiceConfig.createScoringRestTemplate();
        this.scoringServiceUrl = ScoreServiceConfig.getDefaultServiceUrl();

        if (eventService != null) {
            eventService.subscribe(EnemyDestroyedEvent.class, this);
            LOGGER.log(Level.INFO, "ScoreSystem subscribed to EnemyDestroyedEvent using Spring RestTemplate");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available");
        }

        // Initialize score on startup
        initializeScore();
    }

    /**
     * Initialize score on startup using RestTemplate
     */
    private void initializeScore() {
        try {
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score/set/0");
            restTemplate.put(url, null);

            // Get the initialized score to confirm
            Integer initializedScore = getScoreFromService();
            if (initializedScore != null) {
                fallbackScore = initializedScore;
                LOGGER.log(Level.INFO, "Initialized score with microservice using RestTemplate: {0}", fallbackScore);
            }
        } catch (RestClientException e) {
            LOGGER.log(Level.WARNING, "Failed to initialize score with microservice using RestTemplate, using fallback: {0}",
                    e.getMessage());
            fallbackScore = 0;
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        // No per-frame updates needed for scoring
        // This system responds to events rather than polling
    }

    @Override
    public void onEvent(EnemyDestroyedEvent event) {
        int pointsAwarded = event.scoreValue();

        // Apply multiplier based on destruction cause
        switch (event.cause()) {
            case PLAYER_BULLET:
                addScore(pointsAwarded);
                break;
            case PLAYER_COLLISION:
                addScore((int) (pointsAwarded * 1.5)); // Bonus for collision
                break;
            default:
                // No points for other destruction causes
                break;
        }
    }

    /**
     * Add points to the score using Spring RestTemplate
     *
     * @param points Points to add
     */
    private void addScore(int points) {
        try {
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score/add/" + points);
            restTemplate.put(url, null);

            // Get updated score to confirm the operation
            Integer updatedScore = getScoreFromService();
            if (updatedScore != null) {
                fallbackScore = updatedScore;
                LOGGER.log(Level.INFO, "Score updated via RestTemplate: +{0}, total: {1}",
                        new Object[]{points, fallbackScore});
            }
        } catch (RestClientException e) {
            // Fallback to local scoring if microservice is unavailable
            fallbackScore += points;
            LOGGER.log(Level.WARNING, "Microservice unavailable, using fallback score: +{0}, total: {1}. Error: {2}",
                    new Object[]{points, fallbackScore, e.getMessage()});
        }
    }

    /**
     * Get current score using Spring RestTemplate
     *
     * @return Current score or fallback score if service unavailable
     */
    public int getScore() {
        Integer serviceScore = getScoreFromService();
        if (serviceScore != null) {
            fallbackScore = serviceScore;
            return serviceScore;
        }
        return fallbackScore;
    }

    /**
     * Helper method to get score from service using RestTemplate
     *
     * @return Score from service or null if unavailable
     */
    private Integer getScoreFromService() {
        try {
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score/get");
            Integer score = restTemplate.getForObject(url, Integer.class);
            return score;
        } catch (RestClientException e) {
            LOGGER.log(Level.FINE, "Failed to get score from microservice using RestTemplate: {0}",
                    e.getMessage());
            return null;
        }
    }

    /**
     * Reset score using Spring RestTemplate
     */
    public void resetScore() {
        try {
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score/set/0");
            restTemplate.put(url, null);

            // Confirm reset by getting the score
            Integer resetScore = getScoreFromService();
            if (resetScore != null) {
                fallbackScore = resetScore;
                LOGGER.log(Level.INFO, "Score reset via RestTemplate to: {0}", fallbackScore);
            }
        } catch (RestClientException e) {
            fallbackScore = 0;
            LOGGER.log(Level.WARNING, "Failed to reset score via RestTemplate, using fallback: {0}",
                    e.getMessage());
        }
    }

    /**
     * Get the microservice URL for configuration purposes
     *
     * @return The scoring service URL
     */
    public String getScoringServiceUrl() {
        return scoringServiceUrl;
    }

    /**
     * Check if the microservice is available
     *
     * @return true if the service responds to requests
     */
    public boolean isMicroserviceAvailable() {
        try {
            getScoreFromService();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Clean up resources and unsubscribe from events
     * Maintains proper resource management in CBSE architecture
     */
    public void cleanup() {
        if (eventService != null) {
            eventService.unsubscribe(EnemyDestroyedEvent.class, this);
            LOGGER.log(Level.INFO, "ScoreSystem unsubscribed from events and cleaned up");
        }
    }
}