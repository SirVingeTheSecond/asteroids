package dk.sdu.mmmi.cbse.enemy.events;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.events.BaseGameEvent;

public class EnemyDestroyedEvent extends BaseGameEvent {
    private final int scoreValue;
    private final String destroyedBy;

    public EnemyDestroyedEvent(Entity source, int scoreValue, String destroyedBy) {
        super(source);
        this.scoreValue = scoreValue;
        this.destroyedBy = destroyedBy;
    }

    public int getScoreValue() { return scoreValue; }
    public String getDestroyedBy() { return destroyedBy; }
}
