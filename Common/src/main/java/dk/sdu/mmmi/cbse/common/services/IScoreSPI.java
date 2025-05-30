package dk.sdu.mmmi.cbse.common.services;

/**
 * Service Provider Interface for score functionality.
 * Provides operations for score management and retrieval following CBSE principles.
 */
public interface IScoreSPI {
    /**
     * Get the current score from the authoritative source
     *
     * @return Current score value
     */
    int getCurrentScore();

    /**
     * Add points to the current score
     *
     * @param points Points to add (can be negative for penalties)
     */
    void addScore(int points);

    /**
     * Reset the score to zero
     */
    void resetScore();

    /**
     * Check if the scoring service is available and functional
     *
     * @return true if scoring operations will work
     */
    boolean isServiceAvailable();

    /**
     * Get the scoring service URL for debugging/configuration
     *
     * @return Service URL or fallback indicator
     */
    String getServiceInfo();
}