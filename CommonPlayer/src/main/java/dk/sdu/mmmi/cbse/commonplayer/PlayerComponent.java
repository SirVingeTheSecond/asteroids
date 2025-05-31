package dk.sdu.mmmi.cbse.commonplayer;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for Player entity.
 */
public class PlayerComponent implements IComponent {
    private int lives = 3;

    private int maxHealth = 3;
    private int currentHealth = 3;

    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0;
    private static final int INVULNERABILITY_DURATION = 180; // 3 seconds at 60 FPS
    private static final int RESPAWN_INVULNERABILITY_DURATION = 180; // 3 seconds at 60 FPS

    private boolean needsRespawn = false;

    public PlayerComponent() {

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
                // Mark for respawn instead of immediately respawning
                needsRespawn = true;
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
     * Complete the respawn process
     */
    public void completeRespawn() {
        this.needsRespawn = false;
        this.currentHealth = maxHealth;
        this.invulnerable = true;
        this.invulnerabilityTimer = RESPAWN_INVULNERABILITY_DURATION;
    }

    /**
     * Check if player needs respawn
     *
     * @return true if respawn is needed
     */
    public boolean needsRespawn() {
        return needsRespawn;
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
}