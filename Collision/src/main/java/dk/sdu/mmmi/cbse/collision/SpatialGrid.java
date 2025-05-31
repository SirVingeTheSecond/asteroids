package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Spatial partitioning grid for optimized collision detection.
 * Reduces collision checks from O(n^2) to O(n) by only checking entities in nearby grid cells.
 */
public class SpatialGrid {
    private static final Logger LOGGER = Logger.getLogger(SpatialGrid.class.getName());

    private final int cellSize;
    private final Map<Integer, List<Entity>> grid;
    private int gridWidth;
    private int gridHeight;

    public SpatialGrid(int cellSize) {
        this.cellSize = cellSize;
        this.grid = new HashMap<>();
        LOGGER.log(Level.INFO, "SpatialGrid created with cell size: {0}", cellSize);
    }

    /**
     * Clear the grid and prepare for new frame
     */
    public void clear() {
        grid.clear();
    }

    /**
     * Update the grid with current entity positions
     *
     * @param entities List of entities to place in grid
     * @param gameData Game data containing screen dimensions
     */
    public void updateGrid(List<Entity> entities, GameData gameData) {
        gridWidth = (gameData.getDisplayWidth() / cellSize) + 1;
        gridHeight = (gameData.getDisplayHeight() / cellSize) + 1;

        for (Entity entity : entities) {
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) continue;

            // Calculate grid coordinates for this entity
            Set<Integer> cellKeys = getCellKeysForEntity(transform);

            // Add entity to all cells it occupies
            for (Integer key : cellKeys) {
                grid.computeIfAbsent(key, k -> new ArrayList<>()).add(entity);
            }
        }

        LOGGER.log(Level.FINE, "Updated spatial grid with {0} entities across {1} cells",
                new Object[]{entities.size(), grid.size()});
    }

    /**
     * Get all entities that could potentially collide with the given entity
     *
     * @param entity Entity to find neighbors for
     * @return List of nearby entities (including the entity itself)
     */
    public List<Entity> getNearbyEntities(Entity entity) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        if (transform == null) {
            return Collections.emptyList();
        }

        Set<Entity> nearbyEntities = new HashSet<>();
        Set<Integer> cellKeys = getCellKeysForEntity(transform);

        // Collect all entities from relevant cells
        for (Integer key : cellKeys) {
            List<Entity> cellEntities = grid.get(key);
            if (cellEntities != null) {
                nearbyEntities.addAll(cellEntities);
            }
        }

        return new ArrayList<>(nearbyEntities);
    }

    /**
     * Calculate which grid cells an entity occupies based on its position and radius
     *
     * @param transform Entity's transform components
     * @return Set of cell keys that the entity occupies
     */
    private Set<Integer> getCellKeysForEntity(TransformComponent transform) {
        Set<Integer> cellKeys = new HashSet<>();

        float x = transform.getX();
        float y = transform.getY();
        float radius = transform.getRadius();

        // Calculate bounding box of entity including radius
        int minX = (int) Math.floor((x - radius) / cellSize);
        int maxX = (int) Math.floor((x + radius) / cellSize);
        int minY = (int) Math.floor((y - radius) / cellSize);
        int maxY = (int) Math.floor((y + radius) / cellSize);

        // Clamp to grid boundaries
        minX = Math.max(0, minX);
        maxX = Math.min(gridWidth - 1, maxX);
        minY = Math.max(0, minY);
        maxY = Math.min(gridHeight - 1, maxY);

        // Add all cells in the bounding box
        for (int gx = minX; gx <= maxX; gx++) {
            for (int gy = minY; gy <= maxY; gy++) {
                cellKeys.add(getCellKey(gx, gy));
            }
        }

        return cellKeys;
    }

    /**
     * Convert grid coordinates to a single integer key
     *
     * @param gridX Grid X coordinate
     * @param gridY Grid Y coordinate
     * @return Integer key for the cell
     */
    private int getCellKey(int gridX, int gridY) {
        return gridY * gridWidth + gridX;
    }
}