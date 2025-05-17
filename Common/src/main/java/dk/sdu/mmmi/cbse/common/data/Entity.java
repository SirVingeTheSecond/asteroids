package dk.sdu.mmmi.cbse.common.data;

import dk.sdu.mmmi.cbse.common.components.IComponent;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base entity class for the component-based design.
 */
public class Entity {
    private final UUID id = UUID.randomUUID();
    private final Map<Class<?>, IComponent> components = new ConcurrentHashMap<>();

    /**
     * Creates a new entity with no components.
     * Components should be added explicitly by systems.
     */
    public Entity() {

    }

    /**
     * Get the unique identifier for this entity
     * @return Unique ID string
     */
    public String getID() {
        return id.toString();
    }

    /**
     * Add a component to this entity.
     * @param component The component to add
     * @param <T> Type of component extending Component interface
     * @throws NullPointerException if component is null
     */
    public <T extends IComponent> void addComponent(T component) {
        Objects.requireNonNull(component, "Component cannot be null");
        Class<?> componentClass = component.getClass();
        components.put(componentClass, component);
    }

    /**
     * Get a component by type.
     * @param componentType The class of the component to get
     * @param <T> Component type
     * @return The component if present, null otherwise
     * @throws NullPointerException if componentType is null
     */
    @SuppressWarnings("unchecked")
    public <T extends IComponent> T getComponent(Class<T> componentType) {
        Objects.requireNonNull(componentType, "Component type cannot be null");
        return (T) components.get(componentType);
    }

    /**
     * Remove a component by type.
     * @param componentType The class of the component to remove
     * @param <T> Component type
     * @return True if removed, false if not present
     * @throws NullPointerException if componentType is null
     */
    public <T extends IComponent> boolean removeComponent(Class<T> componentType) {
        Objects.requireNonNull(componentType, "Component type cannot be null");
        return components.remove(componentType) != null;
    }

    /**
     * Check if this entity has a component of the specified type.
     * @param componentType The class of the component to check
     * @param <T> Component type
     * @return true if the entity has the component, false otherwise
     * @throws NullPointerException if componentType is null
     */
    public <T extends IComponent> boolean hasComponent(Class<T> componentType) {
        Objects.requireNonNull(componentType, "Component type cannot be null");
        return components.containsKey(componentType);
    }

    /**
     * Get the number of components this entity has
     * @return The component count
     */
    public int getComponentCount() {
        return components.size();
    }

    // Not needed?
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity entity = (Entity) o;
        return id.equals(entity.id);
    }

    // Not needed?
    @Override
    public String toString() {
        return "Entity[" + id + "]";
    }
}