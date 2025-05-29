package dk.sdu.mmmi.cbse.scoreclient;

import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Dedicated event listener for enemy destruction events.
 * Follows CBSE single responsibility principle by handling only enemy-related scoring.
 */
public class EnemyScoreListener implements IEventListener<EnemyDestroyedEvent> {
    private static final Logger LOGGER = Logger.getLogger(EnemyScoreListener.class.getName());

    private final ScoreService scoreService;

    /**
     * Create enemy score listener with reference to score service
     * @param scoreService The score service to use for adding points
     */
    public EnemyScoreListener(ScoreService scoreService) {
        this.scoreService = scoreService;
    }

    @Override
    public void onEvent(EnemyDestroyedEvent event) {
        int pointsAwarded = event.scoreValue();

        switch (event.cause()) {
            case PLAYER_BULLET:
                scoreService.addScore(pointsAwarded);
                LOGGER.log(Level.INFO, "Enemy destroyed by bullet: +{0} points", pointsAwarded);
                break;
            case PLAYER_COLLISION:
                int collisionBonus = (int) (pointsAwarded * 1.5); // Bonus for collision
                scoreService.addScore(collisionBonus);
                LOGGER.log(Level.INFO, "Enemy destroyed by collision: +{0} points (bonus applied)", collisionBonus);
                break;
            default:
                LOGGER.log(Level.FINE, "Enemy destroyed by {0} - no points awarded", event.cause());
                break;
        }
    }
}