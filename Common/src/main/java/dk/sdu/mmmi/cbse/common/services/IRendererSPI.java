package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Service interface for entity rendering.
 * Provides functionality for drawing entities on the canvas.
 */
public interface IRendererSPI {
    /**
     * Render an entity on the canvas
     *
     * @param entity Entity to render
     * @return true if entity was rendered
     */
    boolean render(Entity entity);

    /**
     * Clear the canvas for a new frame
     *
     * @param width Canvas width
     * @param height Canvas height
     */
    void clear(int width, int height);
}