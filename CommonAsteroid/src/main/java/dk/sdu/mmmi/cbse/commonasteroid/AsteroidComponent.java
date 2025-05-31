package dk.sdu.mmmi.cbse.commonasteroid;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for asteroid properties.
 */
public class AsteroidComponent implements IComponent {
    private int splitCount = 0;
    private int maxSplits = 2;
    private float splitSizeRatio = 0.5f;
    private int scoreValue = 100;
    private AsteroidSize size = AsteroidSize.LARGE;

    // Health
    private int maxHealth;
    private int currentHealth;

    public AsteroidComponent() {
        setHealthForSize(AsteroidSize.LARGE);
    }

    /**
     * Create asteroid components with specific size
     *
     * @param size Asteroid size
     */
    public AsteroidComponent(AsteroidSize size) {
        this.size = size;
        setHealthForSize(size);

        switch (size) {
            case LARGE:
                scoreValue = 100;
                break;
            case MEDIUM:
                scoreValue = 150;
                splitCount = 1;
                break;
            case SMALL:
                scoreValue = 200;
                splitCount = 2;
                break;
        }
    }

    /**
     * Set health based on asteroid size
     */
    private void setHealthForSize(AsteroidSize size) {
        switch (size) {
            case LARGE:
                maxHealth = 3;
                break;
            case MEDIUM:
                maxHealth = 2;
                break;
            case SMALL:
                maxHealth = 1;
                break;
            default:
                maxHealth = 1;
                break;
        }
        currentHealth = maxHealth;
    }

    /**
     * Apply damage to the asteroid
     *
     * @param damage Amount of damage to apply
     * @return true if asteroid was destroyed (health <= 0)
     */
    public boolean takeDamage(int damage) {
        currentHealth -= damage;
        return currentHealth <= 0;
    }

    /**
     * Check if asteroid is at full health
     *
     * @return true if at full health
     */
    public boolean isAtFullHealth() {
        return currentHealth >= maxHealth;
    }

    /**
     * Get health percentage (0.0 to 1.0)
     *
     * @return Health as percentage
     */
    public float getHealthPercentage() {
        if (maxHealth <= 0) {
            return 0.0f;
        }
        return (float) currentHealth / maxHealth;
    }

    /**
     * Restore full health
     */
    public void heal() {
        currentHealth = maxHealth;
    }

    public int getSplitCount() {
        return splitCount;
    }

    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    public int getMaxSplits() {
        return maxSplits;
    }

    public void setMaxSplits(int maxSplits) {
        this.maxSplits = maxSplits;
    }

    public float getSplitSizeRatio() {
        return splitSizeRatio;
    }

    public void setSplitSizeRatio(float splitSizeRatio) {
        this.splitSizeRatio = splitSizeRatio;
    }

    public int getScoreValue() {
        return scoreValue;
    }

    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    public AsteroidSize getSize() {
        return size;
    }

    public void setSize(AsteroidSize size) {
        this.size = size;
        setHealthForSize(size); // Update health when size changes
    }

    public int getMaxHealth() {
        return maxHealth;
    }

    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        // Ensure current health doesn't exceed new max
        if (currentHealth > maxHealth) {
            currentHealth = maxHealth;
        }
    }

    public int getCurrentHealth() {
        return currentHealth;
    }

    public void setCurrentHealth(int currentHealth) {
        this.currentHealth = Math.max(0, Math.min(currentHealth, maxHealth));
    }
}