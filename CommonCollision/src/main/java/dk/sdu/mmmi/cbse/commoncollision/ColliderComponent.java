package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for entity collision properties.
 */
public class ColliderComponent implements IComponent {
    private CollisionLayer layer;

    /**
     * Get the collision layer
     * @return The collision layer
     */
    public CollisionLayer getLayer() {
        return layer;
    }

    /**
     * Set the collision layer
     * @param layer The collision layer
     */
    public void setLayer(CollisionLayer layer) {
        this.layer = layer;
    }
}