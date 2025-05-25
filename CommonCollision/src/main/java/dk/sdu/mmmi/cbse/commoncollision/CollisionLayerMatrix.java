package dk.sdu.mmmi.cbse.commoncollision;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collision layer matrix that determines which entities can collide.
 */
public class CollisionLayerMatrix {
    private static final Logger LOGGER = Logger.getLogger(CollisionLayerMatrix.class.getName());
    private static final CollisionLayerMatrix INSTANCE = new CollisionLayerMatrix();

    private final Map<CollisionLayer, EnumSet<CollisionLayer>> collisionMatrix = new EnumMap<>(CollisionLayer.class);

    private CollisionLayerMatrix() {
        initializeDefaultMatrix();
        LOGGER.log(Level.INFO, "CollisionLayerMatrix initialized with boundary separation");
    }

    public static CollisionLayerMatrix getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the collision matrix with game-specific rules
     */
    private void initializeDefaultMatrix() {
        for (CollisionLayer layer : CollisionLayer.values()) {
            collisionMatrix.put(layer, EnumSet.noneOf(CollisionLayer.class));
        }

        defineCollisionRules();
    }

    /**
     * Define collision rules with proper boundary separation
     */
    private void defineCollisionRules() {
        // Player collisions
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY_PROJECTILE, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.OBSTACLE, true);     // Asteroids
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.BOUNDARY, true);    // Screen edges
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.TRIGGER, true);

        // Enemy collisions
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.PLAYER_PROJECTILE, true);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.OBSTACLE, true);     // Asteroids
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.BOUNDARY, true);     // Screen edges

        // Bullets collisions
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.OBSTACLE, true);  // Bullets hit asteroids
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.OBSTACLE, true);   // Enemy bullets hit asteroids

        // Bullets do NOT collide with boundaries
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.BOUNDARY, false);
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.BOUNDARY, false);

        // Prevent friendly fire
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.PLAYER_PROJECTILE, false);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.ENEMY_PROJECTILE, false);

        // DEFAULT layer interactions
        for (CollisionLayer layer : CollisionLayer.values()) {
            if (layer != CollisionLayer.INVINCIBLE && layer != CollisionLayer.DEFAULT) {
                setLayersCollide(CollisionLayer.DEFAULT, layer, true);
            }
        }

        // INVINCIBLE layer never collides
        for (CollisionLayer layer : CollisionLayer.values()) {
            setLayersCollide(CollisionLayer.INVINCIBLE, layer, false);
        }

        LOGGER.log(Level.INFO, "Collision rules defined - Player-Boundary: {0}, Projectile-Boundary: {1}",
                new Object[]{canLayersCollide(CollisionLayer.PLAYER, CollisionLayer.BOUNDARY),
                        canLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.BOUNDARY)});
    }

    /**
     * Set collision relationship between two layers
     */
    public void setLayersCollide(CollisionLayer layer1, CollisionLayer layer2, boolean canCollide) {
        EnumSet<CollisionLayer> collisionsLayer1 = collisionMatrix.get(layer1);
        EnumSet<CollisionLayer> collisionsLayer2 = collisionMatrix.get(layer2);

        if (canCollide) {
            collisionsLayer1.add(layer2);
            if (layer1 != layer2) { // Avoid duplicate self-reference
                collisionsLayer2.add(layer1);
            }
        } else {
            collisionsLayer1.remove(layer2);
            collisionsLayer2.remove(layer1);
        }

        LOGGER.log(Level.FINE, "Set collision between {0} and {1}: {2}",
                new Object[]{layer1, layer2, canCollide});
    }

    /**
     * Check if two layers can collide
     */
    public boolean canLayersCollide(CollisionLayer layer1, CollisionLayer layer2) {
        // Fast path for INVINCIBLE layer
        if (layer1 == CollisionLayer.INVINCIBLE || layer2 == CollisionLayer.INVINCIBLE) {
            return false;
        }

        // Use EnumSet for O(1) lookup
        EnumSet<CollisionLayer> collisionsLayer1 = collisionMatrix.get(layer1);
        return collisionsLayer1 != null && collisionsLayer1.contains(layer2);
    }

    /**
     * Get all layers that can collide with the specified layer
     */
    public Set<CollisionLayer> getCollidingLayers(CollisionLayer layer) {
        return EnumSet.copyOf(collisionMatrix.getOrDefault(layer, EnumSet.noneOf(CollisionLayer.class)));
    }

    /**
     * Reset the collision matrix to default state
     */
    public void resetToDefaults() {
        collisionMatrix.clear();
        initializeDefaultMatrix();
        LOGGER.log(Level.INFO, "Collision matrix reset to defaults");
    }

    /**
     * Get statistics about the collision matrix
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();

        int totalConnections = 0;
        for (Map.Entry<CollisionLayer, EnumSet<CollisionLayer>> entry : collisionMatrix.entrySet()) {
            int connections = entry.getValue().size();
            stats.put(entry.getKey().name() + "_connections", connections);
            totalConnections += connections;
        }

        stats.put("total_connections", totalConnections);
        stats.put("total_layers", CollisionLayer.values().length);

        return stats;
    }
}