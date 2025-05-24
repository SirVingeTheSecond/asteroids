package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Result of a collision handling operation.
 */
public class CollisionResult {
    private final List<Entity> entitiesToRemove = new ArrayList<>();
    private final List<Runnable> actions = new ArrayList<>();
    private boolean stopProcessing = false;

    /**
     * Create an empty collision result (no action taken)
     */
    public static CollisionResult none() {
        return new CollisionResult();
    }

    /**
     * Create a collision result that removes an entity
     */
    public static CollisionResult remove(Entity entity) {
        CollisionResult result = new CollisionResult();
        result.entitiesToRemove.add(entity);
        return result;
    }

    /**
     * Create a collision result that removes multiple entities
     */
    public static CollisionResult remove(Entity... entities) {
        CollisionResult result = new CollisionResult();
        result.entitiesToRemove.addAll(Arrays.asList(entities));
        return result;
    }

    /**
     * Create a collision result with a custom action
     */
    public static CollisionResult action(Runnable action) {
        CollisionResult result = new CollisionResult();
        result.actions.add(action);
        return result;
    }

    /**
     * Add an entity to be removed
     */
    public CollisionResult addRemoval(Entity entity) {
        entitiesToRemove.add(entity);
        return this;
    }

    /**
     * Add a custom action to be executed
     */
    public CollisionResult addAction(Runnable action) {
        actions.add(action);
        return this;
    }

    /**
     * Mark that collision processing should stop after this result
     */
    public CollisionResult stopProcessing() {
        this.stopProcessing = true;
        return this;
    }

    /**
     * Get entities to be removed
     */
    public List<Entity> getEntitiesToRemove() {
        return new ArrayList<>(entitiesToRemove);
    }

    /**
     * Get actions to be executed
     */
    public List<Runnable> getActions() {
        return new ArrayList<>(actions);
    }

    /**
     * Check if processing should stop
     */
    public boolean shouldStopProcessing() {
        return stopProcessing;
    }

    /**
     * Check if this result has any effects
     */
    public boolean isEmpty() {
        return entitiesToRemove.isEmpty() && actions.isEmpty();
    }

    /**
     * Combine this result with another result
     */
    public CollisionResult combine(CollisionResult other) {
        this.entitiesToRemove.addAll(other.entitiesToRemove);
        this.actions.addAll(other.actions);
        this.stopProcessing = this.stopProcessing || other.stopProcessing;
        return this;
    }
}