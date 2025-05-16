package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for weapon properties.
 * Stores the runtime state of a weapon.
 */
public class WeaponComponent implements IComponent {
    private WeaponType.FiringPattern firingPattern = WeaponType.FiringPattern.AUTOMATIC;
    private float damage = 10.0f;
    private float projectileSpeed = 8.0f;
    private int cooldownTime = 20; // Frames between shots
    private int currentCooldown = 0; // Current cooldown counter

    // Burst
    private int burstCount = 3; // Number of bullets in a burst
    private int burstDelay = 5; // Frames between burst shots
    private int currentBurstCount = 0; // Current bullets fired in burst
    private int currentBurstDelay = 0; // Current burst delay counter

    // Shotgun
    private int shotCount = 5; // Number of bullets per shot
    private float spreadAngle = 30.0f; // Angle of spread in degrees

    // Bullet type to spawn
    private String bulletType = "standard";

    // Firing state
    private boolean firing = false;

    /**
     * Create a new weapon component with default values
     */
    public WeaponComponent() {
        // Default constructor uses default values
    }

    /**
     * Create a weapon component from weapon type
     *
     * @param weaponType Weapon type
     */
    public WeaponComponent(WeaponType weaponType) {
        configureFromType(weaponType);
    }

    /**
     * Configure weapon properties based on type
     *
     * @param weaponType Weapon type
     */
    public void configureFromType(WeaponType weaponType) {
        this.firingPattern = weaponType.getFiringPattern();
        this.damage = weaponType.getDamage();
        this.projectileSpeed = weaponType.getProjectileSpeed();
        this.cooldownTime = weaponType.getCooldownTime();
        this.burstCount = weaponType.getBurstCount();
        this.burstDelay = weaponType.getBurstDelay();
        this.shotCount = weaponType.getShotCount();
        this.spreadAngle = weaponType.getSpreadAngle();
        this.bulletType = weaponType.getDefaultBulletType();
    }

    public WeaponType.FiringPattern getFiringPattern() {
        return firingPattern;
    }

    public void setFiringPattern(WeaponType.FiringPattern firingPattern) {
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

    public int getCooldownTime() {
        return cooldownTime;
    }

    public void setCooldownTime(int cooldownTime) {
        this.cooldownTime = cooldownTime;
    }

    public int getCurrentCooldown() {
        return currentCooldown;
    }

    public void setCurrentCooldown(int currentCooldown) {
        this.currentCooldown = currentCooldown;
    }

    public boolean canFire() {
        return currentCooldown <= 0;
    }

    public void resetCooldown() {
        currentCooldown = cooldownTime;
    }

    public void updateCooldown() {
        if (currentCooldown > 0) {
            currentCooldown--;
        }

        if (currentBurstDelay > 0) {
            currentBurstDelay--;
        }
    }

    public int getBurstCount() {
        return burstCount;
    }

    public void setBurstCount(int burstCount) {
        this.burstCount = burstCount;
    }

    public int getBurstDelay() {
        return burstDelay;
    }

    public void setBurstDelay(int burstDelay) {
        this.burstDelay = burstDelay;
    }

    public int getCurrentBurstCount() {
        return currentBurstCount;
    }

    public void setCurrentBurstCount(int currentBurstCount) {
        this.currentBurstCount = currentBurstCount;
    }

    public void incrementBurstCount() {
        currentBurstCount++;
    }

    public boolean isBurstComplete() {
        return currentBurstCount >= burstCount;
    }

    public void resetBurst() {
        currentBurstCount = 0;
    }

    public int getCurrentBurstDelay() {
        return currentBurstDelay;
    }

    public void setCurrentBurstDelay(int currentBurstDelay) {
        this.currentBurstDelay = currentBurstDelay;
    }

    public void startBurstDelay() {
        currentBurstDelay = burstDelay;
    }

    public boolean isBurstDelayComplete() {
        return currentBurstDelay <= 0;
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