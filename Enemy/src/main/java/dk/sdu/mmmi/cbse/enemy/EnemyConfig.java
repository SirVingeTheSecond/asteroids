package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.commonenemy.EnemyType;

/**
 * Configuration class for Enemy properties.
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

    public EnemyType getType() {
        return type;
    }

    public float getHealth() {
        return health;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public float getSpeed() {
        return speed;
    }

    public float getRotationSpeed() {
        return rotationSpeed;
    }

    public float getFiringProbability() {
        return firingProbability;
    }

    public float getFireDistance() {
        return fireDistance;
    }

    public String getWeaponType() {
        return weaponType;
    }

    public String getBulletType() {
        return bulletType;
    }

    /**
     * Builder for EnemyConfig
     */
    public static class Builder {
        private EnemyType type = EnemyType.HUNTER;
        private float health = 1f;
        private int scoreValue = 100;
        private float speed = 80f;
        private float rotationSpeed = 1f;
        private float firingProbability = 0.005f;
        private float fireDistance = 300f;
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