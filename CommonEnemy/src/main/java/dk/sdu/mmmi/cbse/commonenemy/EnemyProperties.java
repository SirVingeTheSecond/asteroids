package dk.sdu.mmmi.cbse.commonenemy;

/**
 * Properties defining enemy capabilities
 */
public class EnemyProperties {
    private float health = 100;
    private float maxHealth = 100;
    private float damage;
    private float speed;
    private float shootingRange;
    private int scoreValue;
    private float detectionRange;

    // Getters and setters
    public float getHealth() { return health; }
    public void setHealth(float health) { this.health = health; }

    public float getMaxHealth() { return maxHealth; }
    public void setMaxHealth(float maxHealth) { this.maxHealth = maxHealth; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getSpeed() { return speed; }
    public void setSpeed(float speed) { this.speed = speed; }

    public float getShootingRange() { return shootingRange; }
    public void setShootingRange(float range) { this.shootingRange = range; }

    public int getScoreValue() { return scoreValue; }
    public void setScoreValue(int value) { this.scoreValue = value; }

    public float getDetectionRange() { return detectionRange; }
    public void setDetectionRange(float range) { this.detectionRange = range; }
}
