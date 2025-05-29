package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Test class for testing ScoreSystem using MicroService.
 */
public class ScoreSystemTest {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystemTest.class.getName());

    /**
     * Test the scoring system with RestTemplate integration.
     * This method can be called to verify the system works correctly.
     */
    public static void testScoreSystem() {
        LOGGER.log(Level.INFO, "Starting ScoreSystem composition-based RestTemplate integration test");

        // Create the scoring system
        ScoreSystem scoreSystem = new ScoreSystem();

        // Test 1: Check initial score
        int initialScore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Initial score: {0}", initialScore);

        // Test 2: Test microservice availability
        boolean serviceAvailable = scoreSystem.isMicroserviceAvailable();
        LOGGER.log(Level.INFO, "Microservice available: {0}", serviceAvailable);

        // Test 3: Test direct score addition
        testDirectScoreAddition(scoreSystem);

        // Test 4: Test enemy destroyed events through specialized listener
        testEnemyDestroyedEvents(scoreSystem);

        // Test 5: Test asteroid split events through specialized listener
        testAsteroidSplitEvents(scoreSystem);

        // Test 6: Test score reset
        scoreSystem.resetScore();
        int resetScore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after reset: {0}", resetScore);

        // Test 7: Verify configuration
        String serviceUrl = scoreSystem.getScoringServiceUrl();
        LOGGER.log(Level.INFO, "Using service URL: {0}", serviceUrl);

        // Test 8: Test composition structure
        testCompositionStructure(scoreSystem);

        // Cleanup
        LOGGER.log(Level.INFO, "ScoreSystem composition test completed successfully");
    }

    /**
     * Test direct score addition functionality.
     */
    private static void testDirectScoreAddition(ScoreSystem scoreSystem) {
        LOGGER.log(Level.INFO, "Testing direct score addition");

        int scoreBefore = scoreSystem.getScore();
        scoreSystem.addScore(25);
        int scoreAfter = scoreSystem.getScore();

        LOGGER.log(Level.INFO, "Score before direct addition: {0}, after (+25): {1}",
                new Object[]{scoreBefore, scoreAfter});

        if (scoreAfter == scoreBefore + 25) {
            LOGGER.log(Level.INFO, "Direct score addition test PASSED");
        } else {
            LOGGER.log(Level.WARNING, "Direct score addition test FAILED");
        }
    }

    /**
     * Test enemy destroyed event handling through composition.
     * Demonstrates how the specialized listener handles enemy events.
     */
    private static void testEnemyDestroyedEvents(ScoreSystem scoreSystem) {
        LOGGER.log(Level.INFO, "Testing enemy destroyed events through composition");

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
        LOGGER.log(Level.INFO, "Score before enemy events: {0}", scoreBefore);

        // Simulate events through the specialized listener
        EnemyScoreListener enemyListener = scoreSystem.getEnemyScoreListener();
        enemyListener.onEvent(bulletEvent);
        int scoreAfterBullet = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after bullet kill (+100): {0}", scoreAfterBullet);

        enemyListener.onEvent(collisionEvent);
        int scoreAfterCollision = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after collision kill (+150): {0}", scoreAfterCollision);

        // Verify scoring logic
        int expectedScore = scoreBefore + 100 + 150; // 100 + (100 * 1.5)
        if (scoreAfterCollision == expectedScore) {
            LOGGER.log(Level.INFO, "Enemy event handling test PASSED");
        } else {
            LOGGER.log(Level.WARNING, "Enemy event handling test FAILED - Expected: {0}, Actual: {1}",
                    new Object[]{expectedScore, scoreAfterCollision});
        }
    }

    /**
     * Test asteroid split event handling through composition.
     * Demonstrates scoring for different asteroid sizes: Large=50, Medium=100, Small=200.
     */
    private static void testAsteroidSplitEvents(ScoreSystem scoreSystem) {
        LOGGER.log(Level.INFO, "Testing asteroid split events through composition");

        // Get score before asteroid events
        int scoreBefore = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score before asteroid events: {0}", scoreBefore);

        // Get the specialized asteroid listener
        AsteroidScoreListener asteroidListener = scoreSystem.getAsteroidScoreListener();

        // Test Large asteroid split (minimum score = 50)
        Entity largeAsteroid = createMockAsteroid(AsteroidSize.LARGE);
        AsteroidSplitEvent largeEvent = new AsteroidSplitEvent(largeAsteroid);
        asteroidListener.onEvent(largeEvent);
        int scoreAfterLarge = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after large asteroid split (+50): {0}", scoreAfterLarge);

        // Test Medium asteroid split (medium score = 100)
        Entity mediumAsteroid = createMockAsteroid(AsteroidSize.MEDIUM);
        AsteroidSplitEvent mediumEvent = new AsteroidSplitEvent(mediumAsteroid);
        asteroidListener.onEvent(mediumEvent);
        int scoreAfterMedium = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after medium asteroid split (+100): {0}", scoreAfterMedium);

        // Test Small asteroid split (maximum score = 200)
        Entity smallAsteroid = createMockAsteroid(AsteroidSize.SMALL);
        AsteroidSplitEvent smallEvent = new AsteroidSplitEvent(smallAsteroid);
        asteroidListener.onEvent(smallEvent);
        int scoreAfterSmall = scoreSystem.getScore();
        LOGGER.log(Level.INFO, "Score after small asteroid split (+200): {0}", scoreAfterSmall);

        // Verify asteroid scoring logic
        int expectedScore = scoreBefore + 50 + 100 + 200; // Total asteroid points
        if (scoreAfterSmall == expectedScore) {
            LOGGER.log(Level.INFO, "Asteroid event handling test PASSED");
        } else {
            LOGGER.log(Level.WARNING, "Asteroid event handling test FAILED - Expected: {0}, Actual: {1}",
                    new Object[]{expectedScore, scoreAfterSmall});
        }
    }

    /**
     * Test the composition structure to ensure proper component relationships.
     */
    private static void testCompositionStructure(ScoreSystem scoreSystem) {
        LOGGER.log(Level.INFO, "Testing composition structure");

        // Verify all composed components are properly initialized
        EnemyScoreListener enemyListener = scoreSystem.getEnemyScoreListener();
        AsteroidScoreListener asteroidListener = scoreSystem.getAsteroidScoreListener();
        ScoreService scoreService = scoreSystem.getScoreService();

        boolean structureValid = enemyListener != null && asteroidListener != null && scoreService != null;

        if (structureValid) {
            LOGGER.log(Level.INFO, "Composition structure test PASSED - All components properly initialized");

            // Test scoring values configuration
            int[] asteroidScoringValues = AsteroidScoreListener.getScoringValues();
            LOGGER.log(Level.INFO, "Asteroid scoring values: Large={0}, Medium={1}, Small={2}",
                    new Object[]{asteroidScoringValues[0], asteroidScoringValues[1], asteroidScoringValues[2]});
        } else {
            LOGGER.log(Level.WARNING, "Composition structure test FAILED - Some components not initialized");
        }
    }

    /**
     * Create a mock asteroid entity with the specified size.
     * Helper method for testing asteroid scoring.
     */
    private static Entity createMockAsteroid(AsteroidSize size) {
        Entity asteroid = new Entity();
        AsteroidComponent asteroidComponent = new AsteroidComponent(size);
        asteroid.addComponent(asteroidComponent);
        return asteroid;
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

    /**
     * Main method for standalone testing.
     * Can be used to verify the integration without running the full game.
     */
    public static void main(String[] args) {
        LOGGER.log(Level.INFO, "Running standalone ScoreSystem composition test");

        try {
            testScoreSystem();
            testConfiguration();
            LOGGER.log(Level.INFO, "All composition-based tests completed successfully");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Test failed with exception", e);
        }
    }
}