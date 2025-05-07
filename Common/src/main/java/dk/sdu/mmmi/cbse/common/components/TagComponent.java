package dk.sdu.mmmi.cbse.common.components;

import dk.sdu.mmmi.cbse.common.data.EntityType;

import java.util.EnumSet;
import java.util.Set;

/**
 * Component that stores entity types for categorization.
 * Uses enum-based approach for type safety.
 */
public class TagComponent implements IComponent {
    private final EnumSet<EntityType> types;

    /**
     * Create a TagComponent with no initial types
     */
    public TagComponent() {
        this.types = EnumSet.noneOf(EntityType.class);
    }

    /**
     * Create a TagComponent with initial entity types
     * @param initialTypes Initial entity types to add
     */
    public TagComponent(EntityType... initialTypes) {
        this.types = EnumSet.noneOf(EntityType.class);
        if (initialTypes != null) {
            for (EntityType type : initialTypes) {
                addType(type);
            }
        }
    }

    /**
     * Add an entity type to this entity
     * @param type EntityType to add
     */
    public void addType(EntityType type) {
        if (type != null) {
            types.add(type);
        }
    }

    /**
     * Remove an entity type from this entity
     * @param type EntityType to remove
     */
    public void removeType(EntityType type) {
        if (type != null) {
            types.remove(type);
        }
    }

    /**
     * Check if entity has a specific type
     * @param type EntityType to check
     * @return true if entity has the type
     */
    public boolean hasType(EntityType type) {
        return type != null && types.contains(type);
    }

    /**
     * Check if entity has all the specified types
     * @param typeList Types to check
     * @return true if entity has all types
     */
    public boolean hasAllTypes(EntityType... typeList) {
        if (typeList == null) {
            return true;
        }

        for (EntityType type : typeList) {
            if (!hasType(type)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Check if entity has any of the specified types
     * @param typeList Types to check
     * @return true if entity has at least one type
     */
    public boolean hasAnyType(EntityType... typeList) {
        if (typeList == null) {
            return false;
        }

        for (EntityType type : typeList) {
            if (hasType(type)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get all entity types for this entity
     * @return Unmodifiable set of types
     */
    public Set<EntityType> getTypes() {
        return EnumSet.copyOf(types);
    }

    /**
     * Clear all types from this entity
     */
    public void clearTypes() {
        types.clear();
    }
}