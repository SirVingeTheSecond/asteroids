package dk.sdu.mmmi.cbse.commonbullet;

import javafx.scene.paint.Color;

/**
 * Configuration class for bullet properties.
 */
public class BulletType {
    private final float speed;
    private final float damage;
    private final boolean piercing;
    private final int pierceCount;
    private final boolean explosive;
    private final float explosionRadius;
    private final boolean bouncing;
    private final int bounceCount;
    private final Color color;

    /**
     * Private constructor as the purpose is to use Builder
     */
    private BulletType(Builder builder) {
        this.speed = builder.speed;
        this.damage = builder.damage;
        this.piercing = builder.piercing;
        this.pierceCount = builder.pierceCount;
        this.explosive = builder.explosive;
        this.explosionRadius = builder.explosionRadius;
        this.bouncing = builder.bouncing;
        this.bounceCount = builder.bounceCount;
        this.color = builder.color;
    }

    /**
     * Get bullet speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Get bullet damage
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Check if bullet is piercing
     */
    public boolean isPiercing() {
        return piercing;
    }

    /**
     * Get number of enemies bullet can pierce
     */
    public int getPierceCount() {
        return pierceCount;
    }

    /**
     * Check if bullet bounces
     */
    public boolean isBouncing() {
        return bouncing;
    }

    /**
     * Get number of bounces
     */
    public int getBounceCount() {
        return bounceCount;
    }

    /**
     * Get bullet color
     */
    public Color getColor() {
        return color;
    }

    /**
     * Builder for BulletType
     */
    public static class Builder {
        private float speed = 5.0f;
        private float damage = 10.0f;
        private boolean piercing = false;
        private int pierceCount = 0;
        private boolean bouncing = false;
        private int bounceCount = 0;
        private Color color = Color.YELLOW;

        public Builder speed(float speed) {
            this.speed = speed;
            return this;
        }

        public Builder damage(float damage) {
            this.damage = damage;
            return this;
        }

        public Builder piercing(boolean piercing) {
            this.piercing = piercing;
            return this;
        }

        public Builder pierceCount(int pierceCount) {
            this.pierceCount = pierceCount;
            return this;
        }

        public Builder bouncing(boolean bouncing) {
            this.bouncing = bouncing;
            return this;
        }

        public Builder bounceCount(int bounceCount) {
            this.bounceCount = bounceCount;
            return this;
        }

        public Builder color(Color color) {
            this.color = color;
            return this;
        }

        public BulletType build() {
            return new BulletType(this);
        }
    }
}