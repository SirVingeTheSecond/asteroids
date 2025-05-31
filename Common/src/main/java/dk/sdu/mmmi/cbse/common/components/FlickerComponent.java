package dk.sdu.mmmi.cbse.common.components;

import javafx.scene.paint.Color;

/**
 * Component for entities that can flicker when damaged or invulnerable.
 */
public class FlickerComponent implements IComponent {
    private boolean flickering = false;
    private float flickerTimer = 0.0f;
    private float flickerDuration = 0.0f;
    private float flickerRate = 10.0f; // Hz

    // Original colors to restore after flickering
    private Color originalStrokeColor;
    private Color originalFillColor;

    // Flicker colors
    private Color flickerStrokeColor;
    private Color flickerFillColor;

    /**
     * Create a flicker components with default settings
     */
    public FlickerComponent() {

    }

    /**
     * Start flickering with specified duration and colors
     *
     * @param duration How long to flicker in seconds
     * @param flickerStroke Color to flicker to for stroke
     * @param flickerFill Color to flicker to for fill
     */
    public void startFlicker(float duration, Color flickerStroke, Color flickerFill) {
        this.flickering = true;
        this.flickerTimer = 0.0f;
        this.flickerDuration = duration;
        this.flickerStrokeColor = flickerStroke;
        this.flickerFillColor = flickerFill;
    }

    /**
     * Update the flicker timer
     *
     * @param deltaTime Time elapsed since last update
     */
    public void updateFlicker(float deltaTime) {
        if (!flickering) {
            return;
        }

        flickerTimer += deltaTime;

        if (flickerTimer >= flickerDuration) {
            stopFlicker();
        }
    }

    /**
     * Stop flickering and reset to original colors
     */
    public void stopFlicker() {
        this.flickering = false;
        this.flickerTimer = 0.0f;
    }

    /**
     * Check if currently in the "flicker on" phase
     *
     * @return true if should show flicker colors
     */
    public boolean isFlickerOn() {
        if (!flickering) {
            return false;
        }

        // Calculate flicker phase based on time and rate
        int flickerFrame = (int)(flickerTimer * flickerRate) % 2;
        return flickerFrame == 1;
    }

    // Getters and setters
    public boolean isFlickering() {
        return flickering;
    }

    public void setFlickering(boolean flickering) {
        this.flickering = flickering;
    }

    public float getFlickerTimer() {
        return flickerTimer;
    }

    public void setFlickerTimer(float flickerTimer) {
        this.flickerTimer = flickerTimer;
    }

    public float getFlickerDuration() {
        return flickerDuration;
    }

    public void setFlickerDuration(float flickerDuration) {
        this.flickerDuration = flickerDuration;
    }

    public float getFlickerRate() {
        return flickerRate;
    }

    public void setFlickerRate(float flickerRate) {
        this.flickerRate = flickerRate;
    }

    public Color getOriginalStrokeColor() {
        return originalStrokeColor;
    }

    public void setOriginalStrokeColor(Color originalStrokeColor) {
        this.originalStrokeColor = originalStrokeColor;
    }

    public Color getOriginalFillColor() {
        return originalFillColor;
    }

    public void setOriginalFillColor(Color originalFillColor) {
        this.originalFillColor = originalFillColor;
    }

    public Color getFlickerStrokeColor() {
        return flickerStrokeColor;
    }

    public void setFlickerStrokeColor(Color flickerStrokeColor) {
        this.flickerStrokeColor = flickerStrokeColor;
    }

    public Color getFlickerFillColor() {
        return flickerFillColor;
    }

    public void setFlickerFillColor(Color flickerFillColor) {
        this.flickerFillColor = flickerFillColor;
    }
}