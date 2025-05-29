package dk.sdu.mmmi.cbse.commonweapon;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for weapon properties with proper burst state management.
 */
public class WeaponComponent implements IComponent {
    private Weapon.FiringPattern firingPattern = Weapon.FiringPattern.AUTOMATIC;
    private float damage = 10.0f;
    private float projectileSpeed = 8.0f;
    private float cooldownTime = 0.33f;
    private float currentCooldown = 0.0f;

    // Burst
    private int burstCount = 3;
    private float burstDelay = 0.083f;
    private int currentBurstCount = 0;
    private float currentBurstDelay = 0.0f;
    private boolean burstInProgress = false;
    private boolean burstTriggered = false;

    // Shotgun
    private int shotCount = 5;
    private float spreadAngle = 30.0f;

    private String bulletType = "standard";
    private boolean firing = false;

    public WeaponComponent() {

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
     * Update weapon cooldown timers and burst state
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
     */
    public boolean canFire() {
        switch (firingPattern) {
            case BURST:
                return canFireBurst();
            default:
                return currentCooldown <= 0.0f;
        }
    }

    /**
     * Check if burst weapon can fire
     */
    private boolean canFireBurst() {
        if (!burstInProgress) {
            // Can start new burst if main cooldown is ready
            return currentCooldown <= 0.0f;
        } else {
            // Can continue burst if burst delay is complete and burst not finished
            return currentBurstDelay <= 0.0f && !isBurstComplete();
        }
    }

    /**
     * Trigger weapon firing (handles burst initiation)
     */
    public void triggerFire() {
        if (firingPattern == Weapon.FiringPattern.BURST && !burstInProgress && canFire()) {
            startBurst();
        }
        burstTriggered = true;
    }

    /**
     * Start a new burst sequence
     */
    public void startBurst() {
        burstInProgress = true;
        currentBurstCount = 0;
        currentBurstDelay = 0.0f;
    }

    /**
     * Fire one shot in burst and update burst state
     */
    public void fireBurstShot() {
        currentBurstCount++;

        if (isBurstComplete()) {
            completeBurst();
        } else {
            currentBurstDelay = burstDelay;
        }
    }

    /**
     * Complete the current burst
     */
    public void completeBurst() {
        burstInProgress = false;
        currentBurstCount = 0;
        currentCooldown = cooldownTime; // Set main cooldown before next burst
    }

    /**
     * Reset cooldown after firing
     */
    public void resetCooldown() {
        currentCooldown = cooldownTime;
    }

    /**
     * Check if current burst is complete
     */
    public boolean isBurstComplete() {
        return currentBurstCount >= burstCount;
    }

    /**
     * Check if burst delay has completed
     */
    public boolean isBurstDelayComplete() {
        return currentBurstDelay <= 0.0f;
    }

    public Weapon.FiringPattern getFiringPattern() { return firingPattern; }
    public void setFiringPattern(Weapon.FiringPattern firingPattern) { this.firingPattern = firingPattern; }

    public float getDamage() { return damage; }
    public void setDamage(float damage) { this.damage = damage; }

    public float getProjectileSpeed() { return projectileSpeed; }
    public void setProjectileSpeed(float projectileSpeed) { this.projectileSpeed = projectileSpeed; }

    public float getCooldownTime() { return cooldownTime; }
    public void setCooldownTime(float cooldownTime) { this.cooldownTime = cooldownTime; }

    public int getBurstCount() { return burstCount; }
    public void setBurstCount(int burstCount) { this.burstCount = burstCount; }

    public float getBurstDelay() { return burstDelay; }
    public void setBurstDelay(float burstDelay) { this.burstDelay = burstDelay; }

    public int getCurrentBurstCount() { return currentBurstCount; }
    public boolean isBurstInProgress() { return burstInProgress; }

    public int getShotCount() { return shotCount; }
    public void setShotCount(int shotCount) { this.shotCount = shotCount; }

    public float getSpreadAngle() { return spreadAngle; }
    public void setSpreadAngle(float spreadAngle) { this.spreadAngle = spreadAngle; }

    public String getBulletType() { return bulletType; }
    public void setBulletType(String bulletType) { this.bulletType = bulletType; }

    public boolean isFiring() { return firing; }
    public void setFiring(boolean firing) {
        this.firing = firing;
        if (!firing && burstInProgress) {
            // Player released fire button during burst - complete burst
            completeBurst();
        }
    }
}