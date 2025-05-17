package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.commonenemy.EnemyType;

/**
 * Configuration class for enemy properties.
 */
public class EnemyConfig {
    private final EnemyType type;
    private final float health;
    private final int scoreValue;
    private final float speed;
    private final float rotationSpeed;
    private final float firingProbability;
    private final float fireDistance;
    private final String weaponType;
    private final String bulletType;

    /**
     * Private constructor as the purpose is to use Builder
     */
    private EnemyConfig(Builder builder) {
        this.type = builder.type;
        this.health = builder.health;
        this.scoreValue = builder.scoreValue;
        this.speed = builder.speed;
        this.rotationSpeed = builder.rotationSpeed;
        this.firingProbability = builder.firingProbability;
        this.fireDistance = builder.fireDistance;
        this.weaponType = builder.weaponType;
        this.bulletType = builder.bulletType;
    }

    /**
     * Get enemy type
     */
    public EnemyType getType() {
        return type;
    }

    /**
     * Get enemy health
     */
    public float getHealth() {
        return health;
    }

    /**
     * Get score value
     */
    public int getScoreValue() {
        return scoreValue;
    }

    /**
     * Get movement speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Get rotation speed
     */
    public float getRotationSpeed() {
        return rotationSpeed;
    }

    /**
     * Get firing probability
     */
    public float getFiringProbability() {
        return firingProbability;
    }

    /**
     * Get fire distance
     */
    public float getFireDistance() {
        return fireDistance;
    }

    /**
     * Get weapon type
     */
    public String getWeaponType() {
        return weaponType;
    }

    /**
     * Get bullet type
     */
    public String getBulletType() {
        return bulletType;
    }

    /**
     * Builder for EnemyConfig
     */
    public static class Builder {
        private EnemyType type = EnemyType.BASIC;
        private float health = 100.0f;
        private int scoreValue = 100;
        private float speed = 80.0f;
        private float rotationSpeed = 1.0f;
        private float firingProbability = 0.005f;
        private float fireDistance = 300.0f;
        private String weaponType = "automatic";
        private String bulletType = "standard";

        public Builder type(EnemyType type) {
            this.type = type;
            return this;
        }

        public Builder health(float health) {
            this.health = health;
            return this;
        }

        public Builder scoreValue(int scoreValue) {
            this.scoreValue = scoreValue;
            return this;
        }

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder rotationSpeed(float rotationSpeed) {
            this.rotationSpeed = rotationSpeed;
            return this;
        }

        public Builder firingProbability(float firingProbability) {
            this.firingProbability = firingProbability;
            return this;
        }

        public Builder fireDistance(float fireDistance) {
            this.fireDistance = fireDistance;
            return this;
        }

        public Builder weaponType(String weaponType) {
            this.weaponType = weaponType;
            return this;
        }

        public Builder bulletType(String bulletType) {
            this.bulletType = bulletType;
            return this;
        }

        public EnemyConfig build() {
            return new EnemyConfig(this);
        }
    }
}