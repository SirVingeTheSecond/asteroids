package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for weapon properties.
 * Stores the runtime state of a weapon with time-based cooldowns.
 */
public class WeaponComponent implements IComponent {
    private Weapon.FiringPattern firingPattern = Weapon.FiringPattern.AUTOMATIC;
    private float damage = 10.0f;
    private float projectileSpeed = 8.0f;
    private float cooldownTime = 0.33f; // Seconds between shots
    private float currentCooldown = 0.0f; // Current cooldown timer in seconds

    // Burst configuration
    private int burstCount = 3; // Number of bullets in a burst
    private float burstDelay = 0.083f; // Seconds between burst shots
    private int currentBurstCount = 0; // Current bullets fired in burst
    private float currentBurstDelay = 0.0f; // Current burst delay timer in seconds

    // Shotgun configuration
    private int shotCount = 5; // Number of bullets per shot
    private float spreadAngle = 30.0f; // Angle of spread in degrees

    // Bullet type to spawn
    private String bulletType = "standard";

    // Firing state
    private boolean firing = false;

    /**
     * Create a weapon component with default values
     */
    public WeaponComponent() {
        // Using default values
    }

    /**
     * Create a weapon component from weapon type
     *
     * @param weapon Weapon type configuration
     */
    public WeaponComponent(Weapon weapon) {
        configureFromType(weapon);
    }

    /**
     * Configure weapon properties based on weapon type
     *
     * @param weapon Weapon type configuration
     */
    public void configureFromType(Weapon weapon) {
        this.firingPattern = weapon.getFiringPattern();
        this.damage = weapon.getDamage();
        this.projectileSpeed = weapon.getProjectileSpeed();
        this.cooldownTime = weapon.getCooldownTime();
        this.burstCount = weapon.getBurstCount();
        this.burstDelay = weapon.getBurstDelay();
        this.shotCount = weapon.getShotCount();
        this.spreadAngle = weapon.getSpreadAngle();
        this.bulletType = weapon.getDefaultBulletType();
    }

    /**
     * Update weapon cooldown timers
     *
     * @param deltaTime Time elapsed since last update in seconds
     */
    public void updateCooldown(float deltaTime) {
        if (currentCooldown > 0.0f) {
            currentCooldown = Math.max(0.0f, currentCooldown - deltaTime);
        }

        if (currentBurstDelay > 0.0f) {
            currentBurstDelay = Math.max(0.0f, currentBurstDelay - deltaTime);
        }
    }

    /**
     * Check if weapon can fire
     *
     * @return true if weapon can fire
     */
    public boolean canFire() {
        return currentCooldown <= 0.0f;
    }

    /**
     * Reset cooldown after firing
     */
    public void resetCooldown() {
        currentCooldown = cooldownTime;
    }

    /**
     * Start burst delay timer
     */
    public void startBurstDelay() {
        currentBurstDelay = burstDelay;
    }

    /**
     * Check if burst delay has completed
     *
     * @return true if burst delay is complete
     */
    public boolean isBurstDelayComplete() {
        return currentBurstDelay <= 0.0f;
    }

    /**
     * Increment burst count and check if burst is complete
     */
    public void incrementBurstCount() {
        currentBurstCount++;
    }

    /**
     * Check if current burst is complete
     *
     * @return true if burst is complete
     */
    public boolean isBurstComplete() {
        return currentBurstCount >= burstCount;
    }

    /**
     * Reset burst counter
     */
    public void resetBurst() {
        currentBurstCount = 0;
    }

    // Getters and setters

    public Weapon.FiringPattern getFiringPattern() {
        return firingPattern;
    }

    public void setFiringPattern(Weapon.FiringPattern firingPattern) {
        this.firingPattern = firingPattern;
    }

    public float getDamage() {
        return damage;
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getProjectileSpeed() {
        return projectileSpeed;
    }

    public void setProjectileSpeed(float projectileSpeed) {
        this.projectileSpeed = projectileSpeed;
    }

    public float getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(float cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public float getCurrentCooldown() {
        return currentCooldown;
    }

    public void setCurrentCooldown(float currentCooldown) {
        this.currentCooldown = currentCooldown;
    }

    public int getBurstCount() {
        return burstCount;
    }

    public void setBurstCount(int burstCount) {
        this.burstCount = burstCount;
    }

    public float getBurstDelay() {
        return burstDelay;
    }

    public void setBurstDelay(float burstDelay) {
        this.burstDelay = burstDelay;
    }

    public int getCurrentBurstCount() {
        return currentBurstCount;
    }

    public void setCurrentBurstCount(int currentBurstCount) {
        this.currentBurstCount = currentBurstCount;
    }

    public float getCurrentBurstDelay() {
        return currentBurstDelay;
    }

    public void setCurrentBurstDelay(float currentBurstDelay) {
        this.currentBurstDelay = currentBurstDelay;
    }

    public int getShotCount() {
        return shotCount;
    }

    public void setShotCount(int shotCount) {
        this.shotCount = shotCount;
    }

    public float getSpreadAngle() {
        return spreadAngle;
    }

    public void setSpreadAngle(float spreadAngle) {
        this.spreadAngle = spreadAngle;
    }

    public String getBulletType() {
        return bulletType;
    }

    public void setBulletType(String bulletType) {
        this.bulletType = bulletType;
    }

    public boolean isFiring() {
        return firing;
    }

    public void setFiring(boolean firing) {
        this.firing = firing;

        if (!firing) {
            resetBurst();
        }
    }
}