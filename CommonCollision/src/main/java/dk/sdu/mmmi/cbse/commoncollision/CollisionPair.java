package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;

import java.util.Objects;

/**
 * Represents a pair of colliding entities.
 * Provides equality and hashCode that treats the pair as unordered.
 */
public class CollisionPair {
    private final Entity entityA;
    private final Entity entityB;

    /**
     * Create a new collision pair
     * @param entityA First entity
     * @param entityB Second entity
     */
    public CollisionPair(Entity entityA, Entity entityB) {
        this.entityA = entityA;
        this.entityB = entityB;
    }

    /**
     * Get the first entity in the pair
     * @return First entity
     */
    public Entity getEntityA() {
        return entityA;
    }

    /**
     * Get the second entity in the pair
     * @return Second entity
     */
    public Entity getEntityB() {
        return entityB;
    }

    /**
     * Gets the "other" entity given one entity in the pair
     * @param entity One of the entities in the pair
     * @return The other entity in the pair
     * @throws IllegalArgumentException If the provided entity is not part of this pair
     */
    public Entity getOther(Entity entity) {
        if (entity.equals(entityA)) {
            return entityB;
        } else if (entity.equals(entityB)) {
            return entityA;
        } else {
            throw new IllegalArgumentException("Entity is not part of this collision pair");
        }
    }

    /**
     * Check if an entity is part of this collision pair
     * @param entity The entity to check
     * @return true if the entity is part of this pair
     */
    public boolean contains(Entity entity) {
        return entity.equals(entityA) || entity.equals(entityB);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CollisionPair that = (CollisionPair) o;

        // Order doesn't matter for equality
        return (Objects.equals(entityA.getID(), that.entityA.getID()) &&
                Objects.equals(entityB.getID(), that.entityB.getID())) ||
                (Objects.equals(entityA.getID(), that.entityB.getID()) &&
                        Objects.equals(entityB.getID(), that.entityA.getID()));
    }

    @Override
    public int hashCode() {
        // Create an order-independent hash code
        return entityA.getID().compareTo(entityB.getID()) < 0
                ? Objects.hash(entityA.getID(), entityB.getID())
                : Objects.hash(entityB.getID(), entityA.getID());
    }

    @Override
    public String toString() {
        return "CollisionPair{" + entityA.getID() + ", " + entityB.getID() + '}';
    }
}