package dk.sdu.mmmi.cbse.common.data;

/**
 * Game state data container.
 * Holds global game state information.
 */
// ToDo: This should be a singleton?
public class GameData {
    private int displayWidth = 800;
    private int displayHeight = 600;

    private float deltaTime;

    private boolean debugMode = false;

    /**
     * Get display width
     *
     * @return Width in pixels
     */
    public int getDisplayWidth() {
        return displayWidth;
    }

    /**
     * Set display width
     *
     * @param width Width in pixels
     */
    public void setDisplayWidth(int width) {
        this.displayWidth = width;
    }

    /**
     * Get display height
     *
     * @return Height in pixels
     */
    public int getDisplayHeight() {
        return displayHeight;
    }

    /**
     * Set display height
     *
     * @param height Height in pixels
     */
    public void setDisplayHeight(int height) {
        this.displayHeight = height;
    }

    /**
     * Get delta time between frames
     *
     * @return Delta time in seconds
     */
    public float getDeltaTime() {
        return deltaTime;
    }

    /**
     * Set delta time between frames
     *
     * @param deltaTime Delta time in seconds
     */
    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    /**
     * Check if debug mode is enabled
     *
     * @return true if debug mode enabled
     */
    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Set debug mode
     *
     * @param debugMode true to enable debug mode
     */
    public void setDebugMode(boolean debugMode) {
        this.debugMode = debugMode;
    }
}