package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.components.IComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;

import java.util.EnumMap;
import java.util.Map;

/**
 * Component that defines how an entity responds to collisions with different entity types.
 */
public class CollisionResponseComponent implements IComponent {
    private final Map<EntityType, ICollisionHandler> handlers = new EnumMap<>(EntityType.class);
    private boolean removeOnAnyCollision = false;
    private boolean stopOtherCollisions = false; // If true, prevents further collision processing

    /**
     * Register a collision handler for a specific entity type
     *
     * @param entityType The type of entity this handler responds to
     * @param handler The collision handler
     */
    public void addHandler(EntityType entityType, ICollisionHandler handler) {
        handlers.put(entityType, handler);
    }

    /**
     * Remove a collision handler for a specific entity type
     *
     * @param entityType The entity type to remove handler for
     */
    public void removeHandler(EntityType entityType) {
        handlers.remove(entityType);
    }

    /**
     * Get the collision handler for a specific entity type
     *
     * @param entityType The entity type
     * @return The collision handler, or null if none registered
     */
    public ICollisionHandler getHandler(EntityType entityType) {
        return handlers.get(entityType);
    }

    /**
     * Check if this entity has a handler for the given entity type
     *
     * @param entityType The entity type to check
     * @return true if handler exists
     */
    public boolean hasHandler(EntityType entityType) {
        return handlers.containsKey(entityType);
    }

    /**
     * Get all registered entity types
     *
     * @return Set of entity types with handlers
     */
    public Iterable<EntityType> getHandledTypes() {
        return handlers.keySet();
    }

    /**
     * Check if entity should be removed on any collision
     *
     * @return true if should be removed
     */
    public boolean shouldRemoveOnAnyCollision() {
        return removeOnAnyCollision;
    }

    /**
     * Set whether entity should be removed on any collision
     *
     * @param removeOnAnyCollision true to remove on any collision
     */
    public void setRemoveOnAnyCollision(boolean removeOnAnyCollision) {
        this.removeOnAnyCollision = removeOnAnyCollision;
    }

    /**
     * Check if this collision should stop further collision processing
     *
     * @return true if should stop other collisions
     */
    public boolean shouldStopOtherCollisions() {
        return stopOtherCollisions;
    }

    /**
     * Set whether this collision should stop further collision processing
     *
     * @param stopOtherCollisions true to stop other collisions
     */
    public void setStopOtherCollisions(boolean stopOtherCollisions) {
        this.stopOtherCollisions = stopOtherCollisions;
    }

    /**
     * Handle collision with another entity
     *
     * @param self This entity
     * @param other The other entity in the collision
     * @param context Collision context with additional data
     * @return Collision result indicating what actions to take
     */
    public CollisionResult handleCollision(Entity self, Entity other, CollisionContext context) {
        EntityType otherType = context.getEntityType(other);
        if (otherType == null) {
            return CollisionResult.none();
        }

        ICollisionHandler handler = getHandler(otherType);
        if (handler == null) {
            return CollisionResult.none();
        }

        return handler.handle(self, other, context);
    }
}