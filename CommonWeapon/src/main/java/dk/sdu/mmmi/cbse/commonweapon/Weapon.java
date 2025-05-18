package dk.sdu.mmmi.cbse.commonweapon;

/**
 * Configuration class for weapon.
 */
public class Weapon {
    public enum FiringPattern {
        AUTOMATIC,
        BURST,
        HEAVY,
        SHOTGUN
    }

    private final FiringPattern firingPattern;
    private final float damage;
    private final float projectileSpeed;
    private final int cooldownTime;

    // Burst
    private final int burstCount;
    private final int burstDelay;

    // Shotgun
    private final int shotCount;
    private final float spreadAngle;

    private final String defaultBulletType;

    /**
     * Private constructor - use Builder instead
     */
    private Weapon(Builder builder) {
        this.firingPattern = builder.firingPattern;
        this.damage = builder.damage;
        this.projectileSpeed = builder.projectileSpeed;
        this.cooldownTime = builder.cooldownTime;
        this.burstCount = builder.burstCount;
        this.burstDelay = builder.burstDelay;
        this.shotCount = builder.shotCount;
        this.spreadAngle = builder.spreadAngle;
        this.defaultBulletType = builder.defaultBulletType;
    }

    /**
     * Get firing pattern
     */
    public FiringPattern getFiringPattern() {
        return firingPattern;
    }

    /**
     * Get weapon damage
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Get projectile speed
     */
    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    /**
     * Get cooldown time
     */
    public int getCooldownTime() {
        return cooldownTime;
    }

    /**
     * Get burst count
     */
    public int getBurstCount() {
        return burstCount;
    }

    /**
     * Get burst delay
     */
    public int getBurstDelay() {
        return burstDelay;
    }

    /**
     * Get shot count for shotgun
     */
    public int getShotCount() {
        return shotCount;
    }

    /**
     * Get spread angle for shotgun
     */
    public float getSpreadAngle() {
        return spreadAngle;
    }

    /**
     * Get default bullet type
     */
    public String getDefaultBulletType() {
        return defaultBulletType;
    }

    /**
     * Builder for WeaponType
     */
    public static class Builder {
        private FiringPattern firingPattern = FiringPattern.AUTOMATIC;
        private float damage = 10.0f;
        private float projectileSpeed = 5.0f;
        private int cooldownTime = 20;
        private int burstCount = 3;
        private int burstDelay = 5;
        private int shotCount = 5;
        private float spreadAngle = 30.0f;
        private String defaultBulletType = "standard";

        public Builder type(FiringPattern firingPattern) {
            this.firingPattern = firingPattern;
            return this;
        }

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder projectileSpeed(float projectileSpeed) {
            this.projectileSpeed = projectileSpeed;
            return this;
        }

        public Builder cooldownTime(int cooldownTime) {
            this.cooldownTime = cooldownTime;
            return this;
        }

        public Builder burstCount(int burstCount) {
            this.burstCount = burstCount;
            return this;
        }

        public Builder burstDelay(int burstDelay) {
            this.burstDelay = burstDelay;
            return this;
        }

        public Builder shotCount(int shotCount) {
            this.shotCount = shotCount;
            return this;
        }

        public Builder spreadAngle(float spreadAngle) {
            this.spreadAngle = spreadAngle;
            return this;
        }

        public Builder defaultBulletType(String defaultBulletType) {
            this.defaultBulletType = defaultBulletType;
            return this;
        }

        public Weapon build() {
            return new Weapon(this);
        }
    }
}