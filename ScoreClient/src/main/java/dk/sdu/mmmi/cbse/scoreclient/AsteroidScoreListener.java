package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dedicated event listener for asteroid split events.
 * Follows CBSE single responsibility principle by handling only asteroid-related scoring.
 */
public class AsteroidScoreListener implements IEventListener<AsteroidSplitEvent> {
    private static final Logger LOGGER = Logger.getLogger(AsteroidScoreListener.class.getName());

    private final ScoreService scoreService;

    // Asteroid scoring values - Large gives minimum, Small gives maximum
    private static final int LARGE_ASTEROID_SCORE = 50;   // Minimum score
    private static final int MEDIUM_ASTEROID_SCORE = 100; // Medium score
    private static final int SMALL_ASTEROID_SCORE = 200;  // Highest score

    /**
     * Create asteroid score listener with reference to score service
     * @param scoreService The score service to use for adding points
     */
    public AsteroidScoreListener(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Override
    public void onEvent(AsteroidSplitEvent event) {
        // Get asteroid component to determine size
        AsteroidComponent asteroidComponent = event.source().getComponent(AsteroidComponent.class);

        if (asteroidComponent == null) {
            LOGGER.log(Level.WARNING, "AsteroidSplitEvent source missing AsteroidComponent");
            return;
        }

        AsteroidSize size = asteroidComponent.getSize();
        int pointsAwarded = getPointsForAsteroidSize(size);

        if (pointsAwarded > 0) {
            scoreService.addScore(pointsAwarded);
            LOGGER.log(Level.INFO, "Asteroid split score awarded: {0} points for {1} asteroid",
                    new Object[]{pointsAwarded, size});
        }
    }

    /**
     * Get points awarded for splitting an asteroid of the given size.
     * Follows the scoring strategy: larger asteroids give fewer points when split.
     *
     * @param size The size of the asteroid that was split
     * @return Points to award
     */
    private int getPointsForAsteroidSize(AsteroidSize size) {
        switch (size) {
            case LARGE:
                return LARGE_ASTEROID_SCORE;   // 50 points - minimum
            case MEDIUM:
                return MEDIUM_ASTEROID_SCORE;  // 100 points - medium
            case SMALL:
                return SMALL_ASTEROID_SCORE;   // 200 points - maximum
            default:
                LOGGER.log(Level.WARNING, "Unknown asteroid size: {0}", size);
                return 0;
        }
    }

    /**
     * Get the scoring values for different asteroid sizes.
     * Useful for testing and configuration.
     *
     * @return Array of [large, medium, small] scoring values
     */
    public static int[] getScoringValues() {
        return new int[]{LARGE_ASTEROID_SCORE, MEDIUM_ASTEROID_SCORE, SMALL_ASTEROID_SCORE};
    }
}