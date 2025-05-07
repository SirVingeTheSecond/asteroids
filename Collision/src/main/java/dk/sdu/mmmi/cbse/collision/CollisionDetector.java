package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.collision.CollisionPair;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;

import java.util.*;

/**
 * Collision detection system with spatial partitioning.
 * Provides efficient collision detection for large numbers of entities.
 */
public class CollisionDetector {
    private static final int CELL_SIZE = 64; // Size of each grid cell
    private final Map<Integer, Set<Entity>> grid = new HashMap<>(); // Spatial grid
    private final Map<EntityType, List<Entity>> entityTypeMap = new EnumMap<>(EntityType.class);
    private boolean useTypeOptimization = true; // Can be toggled for debugging

    /**
     * Clear all tracked entities from the detector
     */
    public void clear() {
        grid.clear();
        entityTypeMap.clear();
    }

    /**
     * Add an entity to the collision detection system
     *
     * @param entity The entity to add
     */
    public void addEntity(Entity entity) {
        // Skip entities without required components
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        CollisionComponent collision = entity.getComponent(CollisionComponent.class);

        if (transform == null || collision == null || !collision.isActive()) {
            return;
        }

        // Add to spatial grid
        List<Integer> cells = getCellsForEntity(entity);
        for (Integer cell : cells) {
            grid.computeIfAbsent(cell, k -> new HashSet<>()).add(entity);
        }

        // Add to type map for additional optimization
        TagComponent tagComponent = entity.getComponent(TagComponent.class);
        if (tagComponent != null) {
            for (EntityType type : tagComponent.getTypes()) {
                entityTypeMap.computeIfAbsent(type, k -> new ArrayList<>()).add(entity);
            }
        }
    }

    /**
     * Detect all entity collisions in the world
     *
     * @return Set of collision pairs
     */
    public Set<CollisionPair> detectCollisions() {
        Set<CollisionPair> collisions = new HashSet<>();
        Set<CollisionPair> processedPairs = new HashSet<>();

        // Process entities by type for additional optimization
        if (useTypeOptimization) {
            detectCollisionsByType(collisions, processedPairs);
        } else {
            // Fallback to checking all entities in the grid
            for (Set<Entity> cellEntities : grid.values()) {
                for (Entity entity : cellEntities) {
                    checkEntityCollisions(entity, collisions, processedPairs);
                }
            }
        }

        return collisions;
    }

    /**
     * Get all potential collisions for an entity
     *
     * @param entity The entity to check
     * @return Set of entities that could potentially collide
     */
    public Set<Entity> getPotentialCollisions(Entity entity) {
        Set<Entity> result = new HashSet<>();

        // Get all cells this entity occupies
        List<Integer> cells = getCellsForEntity(entity);

        // Collect all entities from those cells
        for (Integer cell : cells) {
            Set<Entity> entitiesInCell = grid.get(cell);
            if (entitiesInCell != null) {
                result.addAll(entitiesInCell);
            }
        }

        // Remove self from potential collisions
        result.remove(entity);

        // Apply type filtering if enabled
        if (useTypeOptimization) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && !tagComponent.getTypes().isEmpty()) {
                return filterByRelevantTypes(result, tagComponent.getTypes());
            }
        }

        return result;
    }

    /**
     * Check a specific entity against all potential collisions
     *
     * @param entity The entity to check
     * @param collisions Set to add collisions to
     * @param processedPairs Set of already processed pairs
     */
    private void checkEntityCollisions(Entity entity, Set<CollisionPair> collisions, Set<CollisionPair> processedPairs) {
        Set<Entity> potentialCollisions = getPotentialCollisions(entity);

        for (Entity other : potentialCollisions) {
            // Skip if this pair has already been processed
            CollisionPair pair = new CollisionPair(entity, other);
            if (processedPairs.contains(pair)) {
                continue;
            }

            // Check for actual collision
            if (checkCollision(entity, other)) {
                collisions.add(pair);
            }

            processedPairs.add(pair);
        }
    }

    /**
     * Detect collisions by processing entity types in a specific order
     *
     * @param collisions Set to add collisions to
     * @param processedPairs Set of already processed pairs
     */
    private void detectCollisionsByType(Set<CollisionPair> collisions, Set<CollisionPair> processedPairs) {
        // Process entity types in priority order

        // 1. Player collisions
        processTypeCollisions(EntityType.PLAYER, collisions, processedPairs);

        // 2. Bullet collisions
        processTypeCollisions(EntityType.BULLET, collisions, processedPairs);

        // 3. Enemy collisions
        processTypeCollisions(EntityType.ENEMY, collisions, processedPairs);

        // 4. Asteroid collisions
        processTypeCollisions(EntityType.ASTEROID, collisions, processedPairs);

        // 5. Powerup collisions
        processTypeCollisions(EntityType.POWERUP, collisions, processedPairs);
    }

    /**
     * Process collisions for a specific entity type
     *
     * @param type The entity type to process
     * @param collisions Set to add collisions to
     * @param processedPairs Set of already processed pairs
     */
    private void processTypeCollisions(EntityType type, Set<CollisionPair> collisions, Set<CollisionPair> processedPairs) {
        List<Entity> entitiesOfType = entityTypeMap.get(type);
        if (entitiesOfType == null) {
            return;
        }

        for (Entity entity : entitiesOfType) {
            checkEntityCollisions(entity, collisions, processedPairs);
        }
    }

    /**
     * Filter a set of entities by relevant collision types
     *
     * @param entities The entities to filter
     * @param sourceTypes The types of the source entity
     * @return Filtered set of entities
     */
    private Set<Entity> filterByRelevantTypes(Set<Entity> entities, Set<EntityType> sourceTypes) {
        if (sourceTypes.isEmpty()) {
            return entities;
        }

        Set<EntityType> relevantTypes = getRelevantCollisionTypes(sourceTypes);
        Set<Entity> filtered = new HashSet<>();

        for (Entity entity : entities) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null) {
                // Keep entities that have at least one relevant type
                for (EntityType type : tagComponent.getTypes()) {
                    if (relevantTypes.contains(type)) {
                        filtered.add(entity);
                        break;
                    }
                }
            }
        }

        return filtered;
    }

    /**
     * Determine which entity types need to be checked for collisions based on source types
     *
     * @param sourceTypes The entity's types
     * @return Set of entity types to check against
     */
    private Set<EntityType> getRelevantCollisionTypes(Set<EntityType> sourceTypes) {
        Set<EntityType> typesToCheck = EnumSet.noneOf(EntityType.class);

        for (EntityType type : sourceTypes) {
            switch (type) {
                case PLAYER:
                    // Players collide with enemies, asteroids, and enemy bullets
                    typesToCheck.add(EntityType.ENEMY);
                    typesToCheck.add(EntityType.ASTEROID);
                    typesToCheck.add(EntityType.BULLET);
                    typesToCheck.add(EntityType.POWERUP);
                    break;
                case ENEMY:
                    // Enemies collide with players, asteroids, and player bullets
                    typesToCheck.add(EntityType.PLAYER);
                    typesToCheck.add(EntityType.ASTEROID);
                    typesToCheck.add(EntityType.BULLET);
                    break;
                case ASTEROID:
                    // Asteroids collide with everything
                    typesToCheck.add(EntityType.PLAYER);
                    typesToCheck.add(EntityType.ENEMY);
                    typesToCheck.add(EntityType.BULLET);
                    typesToCheck.add(EntityType.ASTEROID);
                    break;
                case BULLET:
                    // Bullets collide with asteroids, and either players or enemies (but not their owners)
                    typesToCheck.add(EntityType.ASTEROID);
                    typesToCheck.add(EntityType.PLAYER);
                    typesToCheck.add(EntityType.ENEMY);
                    break;
                case POWERUP:
                    // Powerups only collide with players
                    typesToCheck.add(EntityType.PLAYER);
                    break;
            }
        }

        return typesToCheck;
    }

    /**
     * Check if two entities are colliding
     *
     * @param entity1 First entity
     * @param entity2 Second entity
     * @return true if the entities are colliding
     */
    public boolean checkCollision(Entity entity1, Entity entity2) {
        TransformComponent transform1 = entity1.getComponent(TransformComponent.class);
        TransformComponent transform2 = entity2.getComponent(TransformComponent.class);

        if (transform1 == null || transform2 == null) {
            return false;
        }

        // Check if either entity has inactive collision
        CollisionComponent cc1 = entity1.getComponent(CollisionComponent.class);
        CollisionComponent cc2 = entity2.getComponent(CollisionComponent.class);

        if (cc1 == null || cc2 == null || !cc1.isActive() || !cc2.isActive()) {
            return false;
        }

        // Simple circle collision detection
        float dx = transform1.getX() - transform2.getX();
        float dy = transform1.getY() - transform2.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        return distance < (transform1.getRadius() + transform2.getRadius());
    }

    /**
     * Get all grid cells that an entity overlaps
     *
     * @param entity The entity to check
     * @return List of cell indices
     */
    private List<Integer> getCellsForEntity(Entity entity) {
        List<Integer> cells = new ArrayList<>();

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            return cells;
        }

        // Calculate the grid cells this entity overlaps
        int minX = (int)((transform.getX() - transform.getRadius()) / CELL_SIZE);
        int maxX = (int)((transform.getX() + transform.getRadius()) / CELL_SIZE);
        int minY = (int)((transform.getY() - transform.getRadius()) / CELL_SIZE);
        int maxY = (int)((transform.getY() + transform.getRadius()) / CELL_SIZE);

        // Add all overlapping cells
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                cells.add(getGridKey(x, y));
            }
        }

        return cells;
    }

    /**
     * Convert grid coordinates to a single cell identifier
     *
     * @param x X grid position
     * @param y Y grid position
     * @return Grid cell key
     */
    private int getGridKey(int x, int y) {
        // Using Szudzik's function (a space-filling curve) for better distribution
        return x >= 0 ? (x * x + x + y) : (y * y + x);
    }

    /**
     * Enable or disable type-based optimization
     *
     * @param enabled Whether to enable type optimization
     */
    public void setTypeOptimizationEnabled(boolean enabled) {
        this.useTypeOptimization = enabled;
    }
}