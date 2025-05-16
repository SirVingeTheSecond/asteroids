package dk.sdu.mmmi.cbse.commonbullet;

import dk.sdu.mmmi.cbse.common.components.IComponent;

import java.util.UUID;

/**
 * Component for bullet properties.
 */
public class BulletComponent implements IComponent {
    private final UUID shooterID; // ID of the entity that fired this bullet
    private final BulletType type;
    private float speed = 5.0f;
    private float damage = 10.0f;

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
    public BulletComponent(UUID shooterId, BulletType type) {
        this.shooterID = shooterId;
        this.type = type;
    }

    /**
     * Get the bullet type
     * @return Bullet type
     */
    public BulletType getType() {
        return type;
    }

    /**
     * Get the shooter's ID
     * @return Shooter entity ID
     */
    public UUID getShooterID() {
        return shooterID;
    }

    /**
     * Get the bullet speed
     * @return Bullet speed
     */
    public float getSpeed() {
        return speed;
    }

    /**
     * Set the bullet speed
     * @param speed Bullet speed
     */
    public void setSpeed(float speed) {
        this.speed = speed;
    }

    /**
     * Get the bullet damage
     * @return Bullet damage
     */
    public float getDamage() {
        return damage;
    }

    /**
     * Set the bullet damage
     * @param damage Bullet damage
     */
    public void setDamage(float damage) {
        this.damage = damage;
    }
}