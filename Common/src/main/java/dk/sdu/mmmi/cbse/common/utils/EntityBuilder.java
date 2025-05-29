package dk.sdu.mmmi.cbse.common.utils;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.IComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;

/**
 * Builder utility for standardized entity creation.
 * Ensures consistent entity construction across the application.
 */
public class EntityBuilder {
    private final Entity entity;

    /**
     * Create a new EntityBuilder with a fresh entity
     */
    public EntityBuilder() {
        this.entity = new Entity();
    }

    /**
     * Create an EntityBuilder for an existing entity
     * @param entity The existing entity to build upon
     */
    public EntityBuilder(Entity entity) {
        this.entity = entity;
    }

    /**
     * Create a new EntityBuilder
     * @return A new EntityBuilder instance
     */
    public static EntityBuilder create() {
        return new EntityBuilder();
    }

    /**
     * Add a components to the entity
     * @param component The components to add
     * @return This builder for method chaining
     */
    public EntityBuilder with(IComponent component) {
        entity.addComponent(component);
        return this;
    }

    /**
     * Add an entity type to the entity
     * @param type The entity type to add
     * @return This builder for method chaining
     */
    public EntityBuilder withType(EntityType type) {
        TagComponent tagComponent = entity.getComponent(TagComponent.class);
        if (tagComponent == null) {
            tagComponent = new TagComponent();
            entity.addComponent(tagComponent);
        }
        tagComponent.addType(type);
        return this;
    }

    /**
     * Set the position of the entity using Vector2D
     * @param position The position vector
     * @return This builder for method chaining
     */
    public EntityBuilder atPosition(Vector2D position) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            transform = new TransformComponent();
            entity.addComponent(transform);
        }
        transform.setPosition(position);
        return this;
    }

    /**
     * Set the position of the entity
     * @param x The x position
     * @param y The y position
     * @return This builder for method chaining
     */
    public EntityBuilder atPosition(float x, float y) {
        return atPosition(new Vector2D(x, y));
    }

    /**
     * Set the rotation of the entity
     * @param rotation The rotation in degrees
     * @return This builder for method chaining
     */
    public EntityBuilder withRotation(float rotation) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            transform = new TransformComponent();
            entity.addComponent(transform);
        }
        transform.setRotation(rotation);
        return this;
    }

    /**
     * Set the scale of the entity
     * @param scale The scale vector
     * @return This builder for method chaining
     */
    public EntityBuilder withScale(Vector2D scale) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            transform = new TransformComponent();
            entity.addComponent(transform);
        }
        transform.setScale(scale);
        return this;
    }

    /**
     * Set the scale of the entity uniformly
     * @param scale The uniform scale factor
     * @return This builder for method chaining
     */
    public EntityBuilder withScale(float scale) {
        return withScale(new Vector2D(scale, scale));
    }

    /**
     * Set the shape of the entity
     * @param coordinates The polygon coordinates as x1,y1,x2,y2,...
     * @return This builder for method chaining
     */
    public EntityBuilder withShape(double... coordinates) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            transform = new TransformComponent();
            entity.addComponent(transform);
        }
        transform.setPolygonCoordinates(coordinates);
        return this;
    }

    /**
     * Set the collision radius of the entity
     * @param radius The collision radius
     * @return This builder for method chaining
     */
    public EntityBuilder withRadius(float radius) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            transform = new TransformComponent();
            entity.addComponent(transform);
        }
        transform.setRadius(radius);
        return this;
    }

    /**
     * Build and return the constructed entity
     * @return The fully constructed entity
     */
    public Entity build() {
        return entity;
    }
}