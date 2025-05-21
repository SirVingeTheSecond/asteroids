package dk.sdu.mmmi.cbse.score;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that manages the player's score.
 */
public class ScoreSystem implements IUpdate, IEventListener<EnemyDestroyedEvent> {
    private static final Logger LOGGER = Logger.getLogger(ScoreSystem.class.getName());

    private final IEventService eventService;

    private int score = 0;

    public ScoreSystem() {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        if (eventService != null) {
            eventService.subscribe(EnemyDestroyedEvent.class, this);
            LOGGER.log(Level.INFO, "ScoreSystem subscribed to EnemyDestroyedEvent");
        } else {
            LOGGER.log(Level.WARNING, "EventService not available");
        }
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void process(GameData gameData, World world) {
        // ToDo: Update UI
    }

    @Override
    public void onEvent(EnemyDestroyedEvent event) {
        int pointsAwarded = event.scoreValue();

        if (event.cause() == EnemyDestroyedEvent.DestructionCause.PLAYER_BULLET) {
            score += pointsAwarded;
        } else if (event.cause() == EnemyDestroyedEvent.DestructionCause.PLAYER_COLLISION) {
            score += (int) (pointsAwarded * 1.5); // Unnecessary and costly cast?
        }

        LOGGER.log(Level.INFO, "Score updated: +{0}, total: {1}", new Object[]{pointsAwarded, score});
    }

    /**
     * Get current score
     *
     * @return Current score
     */
    public int getScore() {
        return score;
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