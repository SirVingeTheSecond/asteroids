package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.components.IComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;

import java.util.HashMap;
import java.util.Map;

/**
 * Component that handles collision responses.
 * Allows defining different responses for collision layers and groups.
 */
public class CollisionResponseComponent implements IComponent {
    private final Map<CollisionLayer, CollisionResponseHandler> layerResponses = new HashMap<>();
    private final Map<CollisionGroup, CollisionResponseHandler> groupResponses = new HashMap<>();

    /**
     * Add a response handler for a specific layer
     * @param layer The collision layer to handle
     * @param handler The response handler
     */
    public void addLayerResponse(CollisionLayer layer, CollisionResponseHandler handler) {
        layerResponses.put(layer, handler);
    }

    /**
     * Add a response handler for a specific group
     * @param group The collision group to handle
     * @param handler The response handler
     */
    public void addGroupResponse(CollisionGroup group, CollisionResponseHandler handler) {
        groupResponses.put(group, handler);
    }

    /**
     * Handle a collision with another entity
     * @param self This entity
     * @param other The other entity
     * @param world The game world
     * @return true if collision was handled
     */
    public boolean handleCollision(Entity self, Entity other, World world) {
        ColliderComponent otherCC = other.getComponent(ColliderComponent.class);
        if (otherCC == null || !otherCC.isActive()) {
            return false;
        }

        // Check layer-specific response
        CollisionResponseHandler layerHandler = layerResponses.get(otherCC.getLayer());
        if (layerHandler != null && layerHandler.onCollision(self, other, world)) {
            return true;
        }

        // Check group-specific responses
        for (CollisionGroup group : CollisionGroup.values()) {
            if (otherCC.isInGroup(group)) {
                CollisionResponseHandler groupHandler = groupResponses.get(group);
                if (groupHandler != null && groupHandler.onCollision(self, other, world)) {
                    return true;
                }
            }
        }

        return false;
    }
}