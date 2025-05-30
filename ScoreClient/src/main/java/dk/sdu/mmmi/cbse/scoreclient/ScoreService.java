package dk.sdu.mmmi.cbse.scoreclient;

import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service class that handles REST communication with the Score MicroService.
 */
public class ScoreService {
    private static final Logger LOGGER = Logger.getLogger(ScoreService.class.getName());

    private final RestTemplate restTemplate;
    private final String scoringServiceUrl;

    // Fallback local score if microservice is unavailable
    private int fallbackScore = 0;

    /**
     * Create score service with RestTemplate configuration
     */
    public ScoreService() {
        this.restTemplate = ScoreServiceConfig.createScoringRestTemplate();
        this.scoringServiceUrl = ScoreServiceConfig.getDefaultServiceUrl();

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
                LOGGER.log(Level.INFO, "Initialized score with microservice: {0}", fallbackScore);
            }
        } catch (RestClientException e) {
            LOGGER.log(Level.WARNING, "Failed to initialize score with microservice, using fallback: {0}",
                    e.getMessage());
            fallbackScore = 0;
        }
    }

    /**
     * Add points to the score using Spring RestTemplate
     *
     * @param points Points to add
     */
    public void addScore(int points) {
        try {
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score/add/" + points);
            restTemplate.put(url, null);

            // Get updated score to confirm the operation
            Integer updatedScore = getScoreFromService();
            if (updatedScore != null) {
                fallbackScore = updatedScore;
                LOGGER.log(Level.INFO, "Score updated via microservice: +{0}, total: {1}",
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
            String url = ScoreServiceConfig.createEndpointUrl(scoringServiceUrl, "/score");
            Integer score = restTemplate.getForObject(url, Integer.class);
            return score;
        } catch (RestClientException e) {
            LOGGER.log(Level.FINE, "Failed to get score from microservice: {0}",
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
                LOGGER.log(Level.INFO, "Score reset via microservice to: {0}", fallbackScore);
            }
        } catch (RestClientException e) {
            fallbackScore = 0;
            LOGGER.log(Level.WARNING, "Failed to reset score via microservice, using fallback: {0}",
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
     * Get the current fallback score (useful for testing)
     *
     * @return The fallback score value
     */
    public int getFallbackScore() {
        return fallbackScore;
    }
}