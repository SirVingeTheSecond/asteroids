package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.commonui.UIComponent;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entity rendering implementation.
 * Handles the drawing of entities based on their components.
 */
public class EntityRenderer {
    private static final Logger LOGGER = Logger.getLogger(EntityRenderer.class.getName());

    /**
     * Render a single entity using its RendererComponent and TransformComponent
     *
     * @param entity Entity to render
     * @param context Graphics context to draw to
     * @param debugMode Whether to show debug visualization
     */
    public static void renderEntity(Entity entity, GraphicsContext context, boolean debugMode) {
        RendererComponent renderer = entity.getComponent(RendererComponent.class);
        TransformComponent transform = entity.getComponent(TransformComponent.class);

        if (renderer == null || transform == null || !renderer.isVisible()) {
            return;
        }

        // Save current state
        context.save();

        try {
            // Set up transform (position and rotation)
            context.translate(transform.getX(), transform.getY());
            context.rotate(transform.getRotation());

            // Set rendering properties
            context.setStroke(renderer.getStrokeColor());
            context.setFill(renderer.getFillColor());
            context.setLineWidth(renderer.getStrokeWidth());

            // Render based on shape type
            switch (renderer.getShapeType()) {
                case POLYGON:
                    renderPolygon(context, transform, renderer);
                    break;
                case CIRCLE:
                    renderCircle(context, transform, renderer);
                    break;
                case TEXT:
                    renderText(context, entity, renderer);
                    break;
                case IMAGE:
                    LOGGER.log(Level.FINE, "Image rendering not implemented yet");
                    break;
            }

            // Render debug information if enabled
            if (debugMode) {
                renderEntityDebug(context, entity, transform);
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rendering entity: " + entity.getID(), e);
        } finally {
            // Restore state
            context.restore();
        }
    }

    /**
     * Render text for UI elements
     */
    private static void renderText(GraphicsContext context, Entity entity, RendererComponent renderer) {
        UIComponent uiComponent = entity.getComponent(UIComponent.class);
        if (uiComponent == null || uiComponent.getDisplayText().isEmpty()) {
            return;
        }

        // Set font size
        context.setFont(Font.font(uiComponent.getFontSize()));
        context.setTextAlign(TextAlignment.LEFT);

        // Render text
        if (renderer.isFilled()) {
            context.fillText(uiComponent.getDisplayText(), 0, 0);
        }
        context.strokeText(uiComponent.getDisplayText(), 0, 0);
    }

    /**
     * Render a polygon shape
     */
    private static void renderPolygon(GraphicsContext context, TransformComponent transform, RendererComponent renderer) {
        double[] coordinates = transform.getPolygonCoordinates();
        if (coordinates == null || coordinates.length < 6) {
            renderCircle(context, transform, renderer);
            return;
        }

        context.beginPath();
        context.moveTo(coordinates[0], coordinates[1]);

        for (int i = 2; i < coordinates.length; i += 2) {
            context.lineTo(coordinates[i], coordinates[i + 1]);
        }

        context.closePath();

        if (renderer.isFilled()) {
            context.fill();
        }
        context.stroke();
    }

    /**
     * Render a circle shape
     */
    private static void renderCircle(GraphicsContext context, TransformComponent transform, RendererComponent renderer) {
        float radius = transform.getRadius();

        if (renderer.isFilled()) {
            context.fillOval(-radius, -radius, radius * 2, radius * 2);
        }
        context.strokeOval(-radius, -radius, radius * 2, radius * 2);
    }

    /**
     * Render debug visualization for entity
     */
    private static void renderEntityDebug(GraphicsContext context, Entity entity, TransformComponent transform) {
        context.setStroke(javafx.scene.paint.Color.RED);
        context.setLineWidth(1.0);
        context.strokeOval(-transform.getRadius(), -transform.getRadius(),
                transform.getRadius() * 2, transform.getRadius() * 2);

        context.setStroke(javafx.scene.paint.Color.GREEN);
        context.strokeLine(0, 0, transform.getRadius(), 0);

        context.setStroke(javafx.scene.paint.Color.WHITE);
        context.strokeText(entity.getID().substring(0, 8), 0, 0);
    }
}