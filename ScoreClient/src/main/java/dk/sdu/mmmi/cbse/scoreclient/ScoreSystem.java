package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages the player's score using HTTP calls to the Score MicroService.
 */
public class ScoreSystem implements IUpdate, IEventListener<EnemyDestroyedEvent> {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystem.class.getName());

    private final IEventService eventService;
    private final HttpClient httpClient;
    private final String scoringServiceUrl;

    // Fallback local score if microservice is unavailable
    private int fallbackScore = 0;

    public ScoreSystem() {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        this.httpClient = HttpClient.newHttpClient();
        this.scoringServiceUrl = "http://localhost:8080";

        if (eventService != null) {
            eventService.subscribe(EnemyDestroyedEvent.class, this);
            LOGGER.log(Level.INFO, "ScoreSystem subscribed to EnemyDestroyedEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available");
        }

        // Initialize score
        initializeScore();
    }

    /**
     * Initialize score on startup
     */
    private void initializeScore() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scoringServiceUrl + "/score/set/0"))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() == 200) {
                fallbackScore = Integer.parseInt(response.body());
                LOGGER.log(Level.INFO, "Initialized score with microservice: {0}", fallbackScore);
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            LOGGER.log(Level.WARNING, "Failed to initialize score with microservice, using fallback: {0}",
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
    }

    @Override
    public void onEvent(EnemyDestroyedEvent event) {
        int pointsAwarded = event.scoreValue();

        // Apply multiplier based on destruction cause
        if (event.cause() == EnemyDestroyedEvent.DestructionCause.PLAYER_BULLET) {
            addScore(pointsAwarded);
        } else if (event.cause() == EnemyDestroyedEvent.DestructionCause.PLAYER_COLLISION) {
            addScore((int) (pointsAwarded * 1.5)); // Bonus for collision
        }
    }

    /**
     * Add points to the score using the microservice
     */
    private void addScore(int points) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scoringServiceUrl + "/score/add/" + points))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                fallbackScore = Integer.parseInt(response.body());
                LOGGER.log(Level.INFO, "Score updated via microservice: +{0}, total: {1}",
                        new Object[]{points, fallbackScore});
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            // Fallback to local scoring if microservice is unavailable
            fallbackScore += points;
            LOGGER.log(Level.WARNING, "Microservice unavailable, using fallback score: +{0}, total: {1}. Error: {2}",
                    new Object[]{points, fallbackScore, e.getMessage()});
        }
    }

    /**
     * Get current score using HTTP call
     */
    public int getScore() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scoringServiceUrl + "/score/get"))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                fallbackScore = Integer.parseInt(response.body());
                return fallbackScore;
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            LOGGER.log(Level.FINE, "Failed to get score from microservice, using fallback: {0}",
                    e.getMessage());
        }

        return fallbackScore;
    }

    /**
     * Reset score using HTTP call
     */
    public void resetScore() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(scoringServiceUrl + "/score/set/0"))
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                fallbackScore = Integer.parseInt(response.body());
                LOGGER.log(Level.INFO, "Score reset via microservice");
            }
        } catch (IOException | InterruptedException | NumberFormatException e) {
            fallbackScore = 0;
            LOGGER.log(Level.WARNING, "Failed to reset score via microservice, using fallback: {0}",
                    e.getMessage());
        }
    }

    /**
     * Clean up resources
     */
    public void cleanup() {
        if (eventService != null) {
            eventService.unsubscribe(EnemyDestroyedEvent.class, this);
        }
    }
}