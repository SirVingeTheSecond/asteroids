package dk.sdu.mmmi.cbse.common.utils;

import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Utility class for handling entity flickering effects.
 */
public class FlickerUtility {
    private static final Logger LOGGER = Logger.getLogger(FlickerUtility.class.getName());

    private FlickerUtility() {

    }

    /**
     * Start flickering for an entity
     *
     * @param entity Entity to start flickering
     * @param duration Duration in seconds
     * @param flickerStroke Stroke color during flicker
     * @param flickerFill Fill color during flicker
     */
    public static void startFlicker(Entity entity, float duration, Color flickerStroke, Color flickerFill) {
        FlickerComponent flicker = entity.getComponent(FlickerComponent.class);
        RendererComponent renderer = entity.getComponent(RendererComponent.class);

        if (flicker == null || renderer == null) {
            LOGGER.log(Level.WARNING, "ERROR: Entity {0} missing components - FlickerComponent: {1}, RendererComponent: {2}",
                    new Object[]{entity.getID(), (flicker != null), (renderer != null)});
            return;
        }

        // Store original colors if not already stored
        if (flicker.getOriginalStrokeColor() == null) {
            flicker.setOriginalStrokeColor(renderer.getStrokeColor());
            flicker.setOriginalFillColor(renderer.getFillColor());
            LOGGER.log(Level.FINE, "Stored original colors for entity: {0} - Stroke: {1}, Fill: {2}",
                    new Object[]{entity.getID(), renderer.getStrokeColor(), renderer.getFillColor()});
        }

        flicker.startFlicker(duration, flickerStroke, flickerFill);
        LOGGER.log(Level.INFO, "Started flickering for entity: {0} - Duration: {1}s, Rate: {2}Hz",
                new Object[]{entity.getID(), duration, flicker.getFlickerRate()});
    }

    /**
     * Update flickering for an entity
     *
     * @param entity Entity to update
     * @param deltaTime Time elapsed since last update
     */
    public static void updateFlicker(Entity entity, float deltaTime) {
        FlickerComponent flicker = entity.getComponent(FlickerComponent.class);
        RendererComponent renderer = entity.getComponent(RendererComponent.class);

        if (flicker == null || renderer == null) {
            return;
        }

        boolean wasFlickering = flicker.isFlickering();
        boolean wasFlickerOn = flicker.isFlickerOn();

        flicker.updateFlicker(deltaTime);

        // Apply flicker colors based on current flicker state
        if (flicker.isFlickering()) {
            if (flicker.isFlickerOn()) {
                // Show flicker colors
                renderer.setStrokeColor(flicker.getFlickerStrokeColor());
                renderer.setFillColor(flicker.getFlickerFillColor());

                // Debug log state changes
                if (!wasFlickerOn) {
                    LOGGER.log(Level.FINE, "Entity {0} flickering ON - showing flicker colors (Red/DarkRed)",
                            entity.getID());
                }
            } else {
                // Show original colors
                renderer.setStrokeColor(flicker.getOriginalStrokeColor());
                renderer.setFillColor(flicker.getOriginalFillColor());

                // Debug log state changes
                if (wasFlickerOn) {
                    LOGGER.log(Level.FINE, "Entity {0} flickering OFF - showing original colors",
                            entity.getID());
                }
            }
        } else if (wasFlickering) {
            // Flickering just stopped, ensure we're showing original colors
            if (flicker.getOriginalStrokeColor() != null) {
                renderer.setStrokeColor(flicker.getOriginalStrokeColor());
                renderer.setFillColor(flicker.getOriginalFillColor());
                LOGGER.log(Level.INFO, "Entity {0} stopped flickering - restored original colors",
                        entity.getID());
            }
        }
    }

    /**
     * Stop flickering for an entity
     *
     * @param entity Entity to stop flickering
     */
    public static void stopFlicker(Entity entity) {
        FlickerComponent flicker = entity.getComponent(FlickerComponent.class);
        RendererComponent renderer = entity.getComponent(RendererComponent.class);

        if (flicker == null || renderer == null) {
            return;
        }

        flicker.stopFlicker();

        // Restore original colors
        if (flicker.getOriginalStrokeColor() != null) {
            renderer.setStrokeColor(flicker.getOriginalStrokeColor());
            renderer.setFillColor(flicker.getOriginalFillColor());
            LOGGER.log(Level.INFO, "Manually stopped flickering for entity: {0}", entity.getID());
        }
    }

    /**
     * Check if an entity is currently flickering
     *
     * @param entity Entity to check
     * @return true if flickering
     */
    public static boolean isFlickering(Entity entity) {
        FlickerComponent flicker = entity.getComponent(FlickerComponent.class);
        return flicker != null && flicker.isFlickering();
    }

    /**
     * Start damage flicker with predefined colors
     *
     * @param entity Entity to flicker
     * @param duration Duration in seconds
     */
    public static void startDamageFlicker(Entity entity, float duration) {
        FlickerComponent flicker = entity.getComponent(FlickerComponent.class);
        if (flicker == null) {
            LOGGER.log(Level.WARNING, "ERROR: Entity {0} missing FlickerComponent for damage flicker!", entity.getID());
            return;
        }

        LOGGER.log(Level.INFO, "Starting damage flicker for entity: {0} duration: {1}s",
                new Object[]{entity.getID(), duration});
        startFlicker(entity, duration, Color.RED, Color.DARKRED);
    }

    /**
     * Start invulnerability flicker with predefined colors
     *
     * @param entity Entity to flicker
     * @param duration Duration in seconds
     */
    public static void startInvulnerabilityFlicker(Entity entity, float duration) {
        LOGGER.log(Level.INFO, "Starting invulnerability flicker for entity: {0} duration: {1}s",
                new Object[]{entity.getID(), duration});
        startFlicker(entity, duration, Color.LIGHTCYAN, Color.CYAN);
    }
}