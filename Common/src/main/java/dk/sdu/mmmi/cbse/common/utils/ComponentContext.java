package dk.sdu.mmmi.cbse.common.utils;

import dk.sdu.mmmi.cbse.common.components.IComponent;
import dk.sdu.mmmi.cbse.common.components.ShootingComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Utility class for clean communication between components.
 * Provides a context object for sharing data between components and systems
 * without creating direct dependencies.
 */
public class ComponentContext {
    private final Map<String, Object> values = new HashMap<>();

    /**
     * Set a value in the context
     *
     * @param key The key for the value
     * @param value The value to store
     * @return This context for method chaining
     */
    public ComponentContext set(String key, Object value) {
        values.put(key, value);
        return this;
    }

    /**
     * Get a value from the context
     *
     * @param <T> The expected type of the value
     * @param key The key for the value
     * @param defaultValue Default value if key not found
     * @return The value or defaultValue if not found
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, T defaultValue) {
        return (T) values.getOrDefault(key, defaultValue);
    }

    /**
     * Apply a function to a component if present on the entity
     *
     * @param <T> The component type
     * @param <R> The return type
     * @param entity The entity to check
     * @param componentClass The component class
     * @param function The function to apply
     * @param defaultValue Default value if component not found
     * @return The function result or defaultValue
     */
    public static <T extends IComponent, R> R with(
            Entity entity,
            Class<T> componentClass,
            Function<T, R> function,
            R defaultValue) {

        T component = entity.getComponent(componentClass);
        if (component != null) {
            return function.apply(component);
        } else {
            return defaultValue;
        }
    }

    /**
     * Apply an action to a component if present on the entity
     *
     * @param <T> The component type
     * @param entity The entity to check
     * @param componentClass The component class
     * @param action The action to apply
     * @return true if component was found and action applied
     */
    public static <T extends IComponent> boolean ifPresent(
            Entity entity,
            Class<T> componentClass,
            Consumer<T> action) {

        T component = entity.getComponent(componentClass);
        if (component != null) {
            action.accept(component);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Create a context from an entity's components
     *
     * @param entity The entity to extract data from
     * @return A new context with component data
     */
    public static ComponentContext fromEntity(Entity entity) {
        ComponentContext context = new ComponentContext();

        // Extract common component data into the context
        ifPresent(entity, TransformComponent.class, transform -> {
            context.set("positionX", transform.getX());
            context.set("positionY", transform.getY());
            context.set("rotation", transform.getRotation());
            context.set("radius", transform.getRadius());
        });

        ifPresent(entity, ShootingComponent.class, shooting -> {
            context.set("projectileSpeed", shooting.getProjectileSpeed());
            context.set("projectileLifetime", shooting.getProjectileLifetime());
            context.set("projectileDamage", shooting.getDamage());
        });

        return context;
    }
}