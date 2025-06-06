package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for Enemy properties.
 */
public class EnemyComponent implements IComponent {
    private float health = 100.0f;
    private int scoreValue = 100;
    private float firingProbability = 0.005f;
    private float fireDistance = 300.0f;
    private boolean canFire = true;

    private EnemyType type;

    public EnemyComponent() {

    }

    public EnemyComponent(EnemyType type) {
        this.type = type;

        switch (type) {
            case HUNTER:
                health = 150.0f;
                scoreValue = 200;
                firingProbability = 0.01f;
                fireDistance = 400.0f;
                break;
            case TURRET:
                health = 80.0f;
                scoreValue = 150;
                firingProbability = 0.02f;
                fireDistance = 350.0f;
                break;
        }
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public boolean damage(float amount) {
        health -= amount;
        return health <= 0;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public boolean canFire() {
        return canFire;
    }

    public void setCanFire(boolean canFire) {
        this.canFire = canFire;
    }

    public float getFiringProbability() {
        return firingProbability;
    }

    public void setFiringProbability(float firingProbability) {
        this.firingProbability = firingProbability;
    }

    public float getFireDistance() {
        return fireDistance;
    }

    public void setFireDistance(float fireDistance) {
        this.fireDistance = fireDistance;
    }

    public EnemyType getType() {
        return type;
    }

    public void setType(EnemyType type) {
        this.type = type;
    }
}