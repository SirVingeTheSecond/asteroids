package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.Entity;

/**
 * Service Provider Interface for rendering.
 */
public interface IRenderSPI {
    /**
     * Render an entity with its visual components
     *
     * @param entity Entity to render
     * @return true if the entity was rendered successfully
     */
    boolean render(Entity entity);

    /**
     * Clear the screen with the specified dimensions
     *
     * @param width Width of the screen to clear
     * @param height Height of the screen to clear
     */
    void clear(int width, int height);
}
