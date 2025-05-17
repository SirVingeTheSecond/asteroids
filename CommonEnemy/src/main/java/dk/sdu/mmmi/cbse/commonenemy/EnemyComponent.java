package dk.sdu.mmmi.cbse.commonenemy;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for enemy properties.
 */
public class EnemyComponent implements IComponent {
    private float health = 100.0f;
    private int scoreValue = 100;
    private float firingProbability = 0.005f;
    private float fireDistance = 300.0f;
    private boolean canFire = true;

    private EnemyType type = EnemyType.BASIC;

    public EnemyComponent() {

    }

    /**
     * Create a new enemy component with specific type
     *
     * @param type The enemy type
     */
    public EnemyComponent(EnemyType type) {
        this.type = type;

        // Configure based on type
        switch (type) {
            case BASIC:
                health = 100.0f;
                scoreValue = 100;
                firingProbability = 0.005f;
                break;
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

    /**
     * Apply damage to the enemy
     *
     * @param amount Damage amount
     * @return true if enemy died
     */
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