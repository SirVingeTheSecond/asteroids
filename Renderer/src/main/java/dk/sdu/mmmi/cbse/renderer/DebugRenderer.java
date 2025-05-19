package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Debug visualization service for rendering debug information.
 */
public class DebugRenderer implements IDebugRendererSPI {
    private static final Logger LOGGER = Logger.getLogger(DebugRenderer.class.getName());
    private boolean enabled = false;

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        LOGGER.log(Level.INFO, "Debug renderer {0}", enabled ? "enabled" : "disabled");
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void render(GameData gameData, World world) {
        if (!enabled) {
            return;
        }

        GraphicsContext gc = RenderingContext.getInstance().getGraphicsContext();
        if (gc == null) {
            LOGGER.log(Level.WARNING, "Cannot render debug: graphics context not set");
            return;
        }

        // Save context
        gc.save();

        // Reset transform
        gc.setTransform(1, 0, 0, 1, 0, 0);

        // Render entity bounding circles
        renderBoundingCircles(gc, world);

        // Render grid
        renderGrid(gc, gameData);

        // Render world bounds
        renderWorldBounds(gc, gameData);

        // Restore context
        gc.restore();
    }

    /**
     * Render bounding circles for all entities
     */
    private void renderBoundingCircles(GraphicsContext gc, World world) {
        gc.setStroke(Color.RED);
        gc.setLineWidth(1.0);

        for (Entity entity : world.getEntities()) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform != null) {
                float x = transform.getX();
                float y = transform.getY();
                float radius = transform.getRadius();

                gc.strokeOval(x - radius, y - radius, radius * 2, radius * 2);
            }
        }
    }

    /**
     * Render grid for spatial reference
     */
    private void renderGrid(GraphicsContext gc, GameData gameData) {
        int width = gameData.getDisplayWidth();
        int height = gameData.getDisplayHeight();
        int gridSize = 64;

        gc.setStroke(Color.rgb(50, 50, 50, 0.3));
        gc.setLineWidth(0.5);

        // Vertical lines
        for (int x = 0; x <= width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Horizontal lines
        for (int y = 0; y <= height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }
    }

    /**
     * Render world boundaries
     */
    private void renderWorldBounds(GraphicsContext gc, GameData gameData) {
        int width = gameData.getDisplayWidth();
        int height = gameData.getDisplayHeight();

        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(2.0);
        gc.strokeRect(0, 0, width, height);
    }
}