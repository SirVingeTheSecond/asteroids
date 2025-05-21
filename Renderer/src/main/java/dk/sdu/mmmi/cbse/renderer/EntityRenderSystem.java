package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.transform.Affine;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entity rendering implementation.
 */
public class EntityRenderSystem implements ILateUpdate {
    private static final Logger LOGGER = Logger.getLogger(EntityRenderer.class.getName());

    /**
     * Render all entities in sorted order by render layer and Y position
     */
    private void renderEntities(World world) {
        // Get all entities with renderer components
        List<Entity> renderableEntities = getRenderableEntities(world);

        // Sort by render layer (low to high) and then by Y position
        renderableEntities.sort(Comparator.comparingInt(e ->
                        e.getComponent(RendererComponent.class).getRenderLayer())
                .thenComparingDouble(e ->
                        e.getComponent(dk.sdu.mmmi.cbse.common.components.TransformComponent.class).getY()));

        // Render all entities
        int renderedCount = 0;
        for (Entity entity : renderableEntities) {
            if (entityRenderer.render(entity)) {
                renderedCount++;
            }
        }

        LOGGER.log(Level.FINEST, "Rendered {0} entities", renderedCount);
    }

    /**
     * Get all entities with renderer components
     */
    private List<Entity> getRenderableEntities(World world) {
        List<Entity> renderableEntities = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            if (entity.hasComponent(RendererComponent.class)) {
                renderableEntities.add(entity);
            }
        }

        return renderableEntities;
    }

    @Override
    public boolean render(Entity entity) {
        GraphicsContext context = RenderingContext.getInstance().getGraphicsContext();
        if (context == null) {
            LOGGER.log(Level.WARNING, "Cannot render entity: graphics context not set");
            return false;
        }

        // Get required components
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        RendererComponent renderer = entity.getComponent(RendererComponent.class);

        // Skip if missing components or not visible
        if (transform == null || renderer == null || !renderer.isVisible()) {
            return false;
        }

        // Save current context state
        context.save();

        try {
            // Setup transform
            setupTransform(context, transform);

            // Setup style
            setupStyle(context, renderer);

            // Draw shape based on type
            switch (renderer.getShapeType()) {
                case POLYGON:
                    drawPolygon(context, transform, renderer);
                    break;
                case CIRCLE:
                    drawCircle(context, transform, renderer);
                    break;
                default:
                    LOGGER.log(Level.WARNING, "Unsupported shape type: {0}", renderer.getShapeType());
                    break;
            }

            return true;
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error rendering entity: " + e.getMessage(), e);
            return false;
        } finally {
            // Restore context state
            context.restore();
        }
    }

    @Override
    public void clear(int width, int height) {
        GraphicsContext context = RenderingContext.getInstance().getGraphicsContext();
        if (context != null) {
            context.clearRect(0, 0, width, height);
        }
    }

    /**
     * Setup transformation matrix for entity positioning
     */
    private void setupTransform(GraphicsContext gc, TransformComponent transform) {
        Vector2D position = transform.getPosition();
        float rotation = transform.getRotation();

        // Create affine transform
        Affine affine = new Affine();

        // Apply transformations in correct order: translate -> rotate -> scale
        affine.appendTranslation(position.x(), position.y());
        affine.appendRotation(rotation);

        // Apply to graphics context
        gc.setTransform(affine);
    }

    /**
     * Setup render style based on renderer component
     */
    private void setupStyle(GraphicsContext gc, RendererComponent renderer) {
        gc.setStroke(renderer.getStrokeColor());
        gc.setFill(renderer.getFillColor());
        gc.setLineWidth(renderer.getStrokeWidth());
    }

    /**
     * Draw polygon shape based on transform coordinates
     */
    private void drawPolygon(GraphicsContext gc, TransformComponent transform, RendererComponent renderer) {
        double[] coordinates = transform.getPolygonCoordinates();

        if (coordinates == null || coordinates.length < 6) { // Need at least 3 points (6 values)
            // Fallback to circle if no valid polygon
            drawCircle(gc, transform, renderer);
            return;
        }

        // Separate X and Y coordinates
        int count = coordinates.length / 2;
        double[] xPoints = new double[count];
        double[] yPoints = new double[count];

        for (int i = 0; i < count; i++) {
            xPoints[i] = coordinates[i * 2];
            yPoints[i] = coordinates[i * 2 + 1];
        }

        // Fill shape if needed
        if (renderer.isFilled()) {
            gc.fillPolygon(xPoints, yPoints, count);
        }

        // Draw outline
        gc.strokePolygon(xPoints, yPoints, count);
    }

    /**
     * Draw circle shape based on radius
     */
    private void drawCircle(GraphicsContext gc, TransformComponent transform, RendererComponent renderer) {
        float radius = transform.getRadius();

        // Circle is drawn from top-left corner, offset by radius
        double diameter = radius * 2;
        double x = -radius;
        double y = -radius;

        // Fill circle if needed
        if (renderer.isFilled()) {
            gc.fillOval(x, y, diameter, diameter);
        }

        // Draw outline
        gc.strokeOval(x, y, diameter, diameter);
    }
}