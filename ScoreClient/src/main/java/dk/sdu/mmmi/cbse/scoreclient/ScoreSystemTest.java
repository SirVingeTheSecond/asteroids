package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test class for verifying ScoreSystem RestTemplate integration.
 * Demonstrates CBSE principles through component testing and verification.
 */
public class ScoreSystemTest {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystemTest.class.getName());

    /**
     * Test the scoring system with RestTemplate integration.
     * This method can be called to verify the system works correctly.
     */
    public static void testScoreSystem() {
        LOGGER.log(Level.INFO, "Starting ScoreSystem RestTemplate integration test");

        // Create the scoring system (uses RestTemplate internally)
        ScoreSystem scoreSystem = new ScoreSystem();

        // Test 1: Check initial score
        int initialScore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Initial score: {0}", initialScore);

        // Test 2: Test microservice availability
        boolean serviceAvailable = scoreSystem.isMicroserviceAvailable();
        LOGGER.log(Level.INFO, "Microservice available: {0}", serviceAvailable);

        // Test 3: Simulate enemy destruction events
        testEnemyDestroyedEvents(scoreSystem);

        // Test 4: Test score reset
        scoreSystem.resetScore();
        int resetScore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after reset: {0}", resetScore);

        // Test 5: Verify configuration
        String serviceUrl = scoreSystem.getScoringServiceUrl();
        LOGGER.log(Level.INFO, "Using service URL: {0}", serviceUrl);

        // Cleanup
        scoreSystem.cleanup();
        LOGGER.log(Level.INFO, "ScoreSystem test completed successfully");
    }

    /**
     * Test enemy destroyed event handling.
     * Demonstrates how the event-driven architecture works with RestTemplate.
     */
    private static void testEnemyDestroyedEvents(ScoreSystem scoreSystem) {
        LOGGER.log(Level.INFO, "Testing enemy destroyed events");

        // Create a mock enemy entity
        Entity mockEnemy = new Entity();

        // Test different destruction causes
        EnemyDestroyedEvent bulletEvent = new EnemyDestroyedEvent(
                mockEnemy,
                EnemyDestroyedEvent.DestructionCause.PLAYER_BULLET,
                100
        );

        EnemyDestroyedEvent collisionEvent = new EnemyDestroyedEvent(
                mockEnemy,
                EnemyDestroyedEvent.DestructionCause.PLAYER_COLLISION,
                100
        );

        // Get score before events
        int scoreBefore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score before events: {0}", scoreBefore);

        // Simulate events
        scoreSystem.onEvent(bulletEvent);
        int scoreAfterBullet = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after bullet kill (+100): {0}", scoreAfterBullet);

        scoreSystem.onEvent(collisionEvent);
        int scoreAfterCollision = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after collision kill (+150): {0}", scoreAfterCollision);

        // Verify scoring logic
        int expectedScore = scoreBefore + 100 + 150; // 100 + (100 * 1.5)
        if (scoreAfterCollision == expectedScore) {
            LOGGER.log(Level.INFO, "Event handling test PASSED");
        } else {
            LOGGER.log(Level.WARNING, "Event handling test FAILED - Expected: {0}, Actual: {1}",
                    new Object[]{expectedScore, scoreAfterCollision});
        }
    }

    /**
     * Main method for standalone testing.
     * Can be used to verify the integration without running the full game.
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Running standalone ScoreSystem test");

        try {
            testScoreSystem();
            LOGGER.log(Level.INFO, "All tests completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed with exception", e);
        }
    }

    /**
     * Verify configuration helper methods work correctly.
     * Demonstrates the configuration abstraction benefits.
     */
    public static void testConfiguration() {
        LOGGER.log(Level.INFO, "Testing ScoreServiceConfig");

        // Test URL validation
        assert ScoreServiceConfig.isValidServiceUrl("http://localhost:8080");
        assert ScoreServiceConfig.isValidServiceUrl("https://api.example.com");
        assert !ScoreServiceConfig.isValidServiceUrl("invalid-url");
        assert !ScoreServiceConfig.isValidServiceUrl(null);

        // Test endpoint URL creation
        String testUrl = ScoreServiceConfig.createEndpointUrl("http://localhost:8080", "/score/get");
        assert testUrl.equals("http://localhost:8080/score/get");

        String testUrl2 = ScoreServiceConfig.createEndpointUrl("http://localhost:8080/", "score/get");
        assert testUrl2.equals("http://localhost:8080/score/get");

        LOGGER.log(Level.INFO, "Configuration tests passed");
    }
}