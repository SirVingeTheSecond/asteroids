package dk.sdu.mmmi.cbse.commonplayer;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for player-specific properties.
 * Contains information like lives, score, and invulnerability state.
 */
public class PlayerComponent implements IComponent {
    private int lives = 3;
    private int score = 0;
    private boolean invulnerable = false;
    private int invulnerabilityTimer = 0; // Frames of invulnerability remaining
    private static final int INVULNERABILITY_DURATION = 180; // 3 seconds at 60 FPS

    /**
     * Create a new player component with default values
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
     * Damage player (reduce lives)
     *
     * @return true if player died
     */
    public boolean damage() {
        if (invulnerable) {
            return false;
        }

        lives--;
        if (lives > 0) {
            // Make temporarily invulnerable after taking damage
            setInvulnerable(true);
            return false;
        }

        return true; // Player died
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