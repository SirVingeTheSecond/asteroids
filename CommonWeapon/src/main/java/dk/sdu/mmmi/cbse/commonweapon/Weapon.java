package dk.sdu.mmmi.cbse.commonweapon;

/**
 * Configuration class for weapon types.
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
    private final float cooldownTime; // Seconds between shots

    // Burst
    private final int burstCount;
    private final float burstDelay; // Seconds between burst shots

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
     *
     * @return Firing pattern
     */
    public FiringPattern getFiringPattern() {
        return firingPattern;
    }

    /**
     * Get weapon damage
     *
     * @return Damage per shot
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Get projectile speed
     *
     * @return Speed in units per second
     */
    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    /**
     * Get cooldown time between shots
     *
     * @return Cooldown time in seconds
     */
    public float getCooldownTime() {
        return cooldownTime;
    }

    /**
     * Get burst count for burst weapons
     *
     * @return Number of shots in a burst
     */
    public int getBurstCount() {
        return burstCount;
    }

    /**
     * Get burst delay between shots in a burst
     *
     * @return Delay in seconds between burst shots
     */
    public float getBurstDelay() {
        return burstDelay;
    }

    /**
     * Get shot count for shotgun weapons
     *
     * @return Number of projectiles per shot
     */
    public int getShotCount() {
        return shotCount;
    }

    /**
     * Get spread angle for shotgun weapons
     *
     * @return Spread angle in degrees
     */
    public float getSpreadAngle() {
        return spreadAngle;
    }

    /**
     * Get default bullet type for this weapon
     *
     * @return Default bullet type identifier
     */
    public String getDefaultBulletType() {
        return defaultBulletType;
    }

    /**
     * Builder for Weapon configuration
     */
    public static class Builder {
        private FiringPattern firingPattern = FiringPattern.AUTOMATIC;
        private float damage = 10.0f;
        private float projectileSpeed = 5.0f;
        private float cooldownTime = 0.33f; // ~3 shots per second
        private int burstCount = 3;
        private float burstDelay = 0.083f; // ~12 shots per second burst rate
        private int shotCount = 5;
        private float spreadAngle = 30.0f;
        private String defaultBulletType = "standard";

        /**
         * Set firing pattern
         *
         * @param firingPattern Weapon firing pattern
         * @return Builder for method chaining
         */
        public Builder type(FiringPattern firingPattern) {
            this.firingPattern = firingPattern;
            return this;
        }

        /**
         * Set weapon damage
         *
         * @param damage Damage per shot
         * @return Builder for method chaining
         */
        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        /**
         * Set projectile speed
         *
         * @param projectileSpeed Speed in units per second
         * @return Builder for method chaining
         */
        public Builder projectileSpeed(float projectileSpeed) {
            this.projectileSpeed = projectileSpeed;
            return this;
        }

        /**
         * Set cooldown time between shots
         *
         * @param cooldownTime Time in seconds between shots
         * @return Builder for method chaining
         */
        public Builder cooldownTime(float cooldownTime) {
            this.cooldownTime = cooldownTime;
            return this;
        }

        /**
         * Set burst count for burst weapons
         *
         * @param burstCount Number of shots in a burst
         * @return Builder for method chaining
         */
        public Builder burstCount(int burstCount) {
            this.burstCount = burstCount;
            return this;
        }

        /**
         * Set burst delay between shots in a burst
         *
         * @param burstDelay Time in seconds between burst shots
         * @return Builder for method chaining
         */
        public Builder burstDelay(float burstDelay) {
            this.burstDelay = burstDelay;
            return this;
        }

        /**
         * Set shot count for shotgun weapons
         *
         * @param shotCount Number of projectiles per shot
         * @return Builder for method chaining
         */
        public Builder shotCount(int shotCount) {
            this.shotCount = shotCount;
            return this;
        }

        /**
         * Set spread angle for shotgun weapons
         *
         * @param spreadAngle Spread angle in degrees
         * @return Builder for method chaining
         */
        public Builder spreadAngle(float spreadAngle) {
            this.spreadAngle = spreadAngle;
            return this;
        }

        /**
         * Set default bullet type
         *
         * @param defaultBulletType Default bullet type identifier
         * @return Builder for method chaining
         */
        public Builder defaultBulletType(String defaultBulletType) {
            this.defaultBulletType = defaultBulletType;
            return this;
        }

        /**
         * Build the weapon configuration
         *
         * @return Immutable weapon configuration
         */
        public Weapon build() {
            return new Weapon(this);
        }
    }
}