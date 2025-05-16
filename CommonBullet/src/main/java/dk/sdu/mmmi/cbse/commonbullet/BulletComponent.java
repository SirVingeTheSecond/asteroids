package dk.sdu.mmmi.cbse.commonbullet;

import dk.sdu.mmmi.cbse.common.components.IComponent;

import java.util.UUID;

/**
 * Component for bullet properties.
 */
public class BulletComponent implements IComponent {
    private final UUID shooterID;
    private final BulletSource source;

    private float speed = 5.0f;
    private float damage = 10.0f;

    private boolean piercing = false;
    private int pierceCount = 0;
    private int currentPierceCount = 0;

    private boolean bouncing = false;
    private int bounceCount = 0;
    private int currentBounceCount = 0;

    /**
     * Source of bullets
     */
    public enum BulletSource {
        PLAYER,
        ENEMY
    }

    /**
     * Create a bullet component
     *
     * @param shooterID ID of the shooter
     * @param source Bullet source
     */
    public BulletComponent(UUID shooterID, BulletSource source) {
        this.shooterID = shooterID;
        this.source = source;
    }

    public UUID getShooterID() {
        return shooterID;
    }

    public BulletSource getSource() {
        return source;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public boolean isPiercing() {
        return piercing;
    }

    public void setPiercing(boolean piercing) {
        this.piercing = piercing;
    }

    public int getPierceCount() {
        return pierceCount;
    }

    public void setPierceCount(int pierceCount) {
        this.pierceCount = pierceCount;
    }

    public int getCurrentPierceCount() {
        return currentPierceCount;
    }

    public void incrementPierceCount() {
        currentPierceCount++;
    }

    public boolean isPierceCountExceeded() {
        return currentPierceCount >= pierceCount;
    }

    public boolean isBouncing() {
        return bouncing;
    }

    public void setBouncing(boolean bouncing) {
        this.bouncing = bouncing;
    }

    public int getBounceCount() {
        return bounceCount;
    }

    public void setBounceCount(int bounceCount) {
        this.bounceCount = bounceCount;
    }

    public int getCurrentBounceCount() {
        return currentBounceCount;
    }

    public void incrementBounceCount() {
        currentBounceCount++;
    }

    public boolean isBounceCountExceeded() {
        return currentBounceCount >= bounceCount;
    }
}