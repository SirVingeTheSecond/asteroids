package dk.sdu.mmmi.cbse.common.events;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Event triggered when the game should end.
 */
public record GameOverEvent(
        Entity player,
        int finalScore,
        String scoreSource,
        GameOverReason reason
) implements IEvent {

    /**
     * Reason for game over
     */
    public enum GameOverReason {
        PLAYER_DEATH,
    }

    /**
     * Create a game over event for player death
     *
     * @param player The player entity
     * @param finalScore Final score achieved
     * @param scoreSource Source of the score (microservice/fallback)
     */
    public static GameOverEvent playerDeath(Entity player, int finalScore, String scoreSource) {
        return new GameOverEvent(player, finalScore, scoreSource, GameOverReason.PLAYER_DEATH);
    }

    @Override
    public Entity source() {
        return player;
    }
}