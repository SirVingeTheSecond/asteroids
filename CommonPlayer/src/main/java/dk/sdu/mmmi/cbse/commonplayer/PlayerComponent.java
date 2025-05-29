package dk.sdu.mmmi.cbse.commonplayer;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for player-specific properties.
 */
public class PlayerComponent implements IComponent {
    private int lives = 3;
    private int score = 0;

    // Health system
    private int maxHealth = 3;
    private int currentHealth = 3;

    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0; // Frames of invulnerability remaining
    private static final int INVULNERABILITY_DURATION = 180; // 3 seconds at 60 FPS

    /**
     * Create a new player components with default values
     */
    public PlayerComponent() {
        // Using default values
    }

    /**
     * Get player lives
     *
     * @return Lives remaining
     */
    public int getLives() {
        return lives;
    }

    /**
     * Set player lives
     *
     * @param lives Lives to set
     */
    public void setLives(int lives) {
        this.lives = lives;
    }

    /**
     * Get current health
     *
     * @return Current health points
     */
    public int getCurrentHealth() {
        return currentHealth;
    }

    /**
     * Get maximum health
     *
     * @return Maximum health points
     */
    public int getMaxHealth() {
        return maxHealth;
    }

    /**
     * Set maximum health and reset current health
     *
     * @param maxHealth Maximum health to set
     */
    public void setMaxHealth(int maxHealth) {
        this.maxHealth = maxHealth;
        this.currentHealth = maxHealth;
    }

    /**
     * Restore full health
     */
    public void heal() {
        currentHealth = maxHealth;
    }

    /**
     * Apply damage to player
     *
     * @param damage Amount of damage to apply
     * @return true if player died (health reached 0)
     */
    public boolean takeDamage(int damage) {
        if (invulnerable) {
            return false;
        }

        currentHealth -= damage;

        if (currentHealth <= 0) {
            currentHealth = 0;
            lives--;

            if (lives > 0) {
                // Respawn with full health and temporary invulnerability
                currentHealth = maxHealth;
                setInvulnerable(true);
                return false; // Still alive
            } else {
                return true; // Player died (no lives left)
            }
        } else {
            // Damaged but not killed, make temporarily invulnerable
            setInvulnerable(true);
            return false;
        }
    }

    /**
     * Damage player (backward compatibility - defaults to 1 damage)
     *
     * @return true if player died
     */
    public boolean damage() {
        return takeDamage(1);
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
     * Check if player is at full health
     *
     * @return true if at full health
     */
    public boolean isAtFullHealth() {
        return currentHealth >= maxHealth;
    }

    /**
     * Get player score
     *
     * @return Current score
     */
    public int getScore() {
        return score;
    }

    /**
     * Set player score
     *
     * @param score Score to set
     */
    public void setScore(int score) {
        this.score = score;
    }

    /**
     * Add points to player score
     *
     * @param points Points to add
     */
    public void addScore(int points) {
        this.score += points;
    }

    /**
     * Check if player is invulnerable
     *
     * @return true if invulnerable
     */
    public boolean isInvulnerable() {
        return invulnerable;
    }

    /**
     * Set player invulnerability
     *
     * @param invulnerable true for invulnerable
     */
    public void setInvulnerable(boolean invulnerable) {
        this.invulnerable = invulnerable;
        if (invulnerable) {
            this.invulnerabilityTimer = INVULNERABILITY_DURATION;
        }
    }

    /**
     * Update invulnerability state
     * Decrements timer and disables invulnerability when expired
     */
    public void updateInvulnerability() {
        if (invulnerable) {
            invulnerabilityTimer--;
            if (invulnerabilityTimer <= 0) {
                invulnerable = false;
            }
        }
    }

    /**
     * Get invulnerability timer
     *
     * @return Frames of invulnerability remaining
     */
    public int getInvulnerabilityTimer() {
        return invulnerabilityTimer;
    }

    /**
     * Set invulnerability timer
     *
     * @param invulnerabilityTimer Frames of invulnerability
     */
    public void setInvulnerabilityTimer(int invulnerabilityTimer) {
        this.invulnerabilityTimer = invulnerabilityTimer;
    }
}