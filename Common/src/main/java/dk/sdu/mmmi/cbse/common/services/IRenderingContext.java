package dk.sdu.mmmi.cbse.common.services;

import javafx.scene.canvas.GraphicsContext;

/**
 * Central service for maintaining graphics context.
 */
public interface IRenderingContext {
    /**
     * Set the graphics context for rendering
     *
     * @param context JavaFX graphics context
     */
    void setGraphicsContext(GraphicsContext context);

    /**
     * Get the current graphics context
     *
     * @return Current graphics context
     */
    GraphicsContext getGraphicsContext();
}