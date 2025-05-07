package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for bullet-specific properties.
 * Contains information about shooter, damage, speed, etc.
 */
public class WeaponComponent implements IComponent {
    private String shooterID; // ID of the entity that fired this bullet
    private BulletType type;
    private float speed;
    private int remainingLifetime; // Current lifetime countdown
    private int lifetime;
    private float damage;

    /**
     * Types of bullets for different sources
     */
    public enum BulletType {
        PLAYER,  // Fired by player
        ENEMY    // Fired by enemies
    }

    /**
     * Create a new bullet component with default values
     */
    public WeaponComponent() {
        this.speed = 2.0f;
        this.lifetime = 600; // 10 seconds at 60 FPS
        this.remainingLifetime = lifetime;
        this.damage = 10.0f;
        this.type = BulletType.PLAYER; // Default
    }

    // Getters and setters
    public void setType(BulletType type) {
        this.type = type;
    }

    public BulletType getType() {
        return type;
    }

    public void setShooterID(String id) {
        this.shooterID = id;
    }

    public String getShooterID() {
        return shooterID;
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

    public int getLifetime() {
        return lifetime;
    }

    public void setLifetime(int lifetime) {
        this.lifetime = lifetime;
        this.remainingLifetime = lifetime;
    }

    public int getRemainingLifetime() {
        return remainingLifetime;
    }

    public void setRemainingLifetime(int lifetime) {
        this.remainingLifetime = lifetime;
    }

    /**
     * Reduce remaining lifetime by one frame
     */
    public void reduceLifetime() {
        remainingLifetime--;
    }
}