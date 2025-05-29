package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;

import java.util.EnumSet;
import java.util.Set;

/**
 * Context information passed to collision handlers.
 */
public class CollisionContext {
    private final GameData gameData;
    private final World world;
    private final IEventService eventService;

    public CollisionContext(GameData gameData, World world, IEventService eventService) {
        this.gameData = gameData;
        this.world = world;
        this.eventService = eventService;
    }

    public GameData getGameData() {
        return gameData;
    }

    public World getWorld() {
        return world;
    }

    public IEventService getEventService() {
        return eventService;
    }

    /**
     * Get the entity type of an entity
     *
     * @param entity Entity to check
     * @return First entity type found, or null if no tag components
     */
    public EntityType getEntityType(Entity entity) {
        TagComponent tag = entity.getComponent(TagComponent.class);
        if (tag == null) {
            return null;
        }

        for (EntityType type : EntityType.values()) {
            if (tag.hasType(type)) {
                return type;
            }
        }
        return null;
    }

    /**
     * Get all entity types of an entity
     *
     * @param entity Entity to check
     * @return Set of entity types
     */
    public Set<EntityType> getEntityTypes(Entity entity) {
        TagComponent tag = entity.getComponent(TagComponent.class);
        if (tag == null) {
            return EnumSet.noneOf(EntityType.class);
        }
        return tag.getTypes();
    }

    /**
     * Check if entity has a specific type
     *
     * @param entity Entity to check
     * @param type Type to check for
     * @return true if entity has the type
     */
    public boolean hasType(Entity entity, EntityType type) {
        TagComponent tag = entity.getComponent(TagComponent.class);
        return tag != null && tag.hasType(type);
    }
}