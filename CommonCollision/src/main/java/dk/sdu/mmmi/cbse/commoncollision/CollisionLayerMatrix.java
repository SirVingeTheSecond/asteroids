package dk.sdu.mmmi.cbse.commoncollision;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton class that defines which collision layers can interact with each other.
 */
public class CollisionLayerMatrix {
    private static final Logger LOGGER = Logger.getLogger(CollisionLayerMatrix.class.getName());
    private static final CollisionLayerMatrix INSTANCE = new CollisionLayerMatrix();

    private final Map<CollisionLayer, Map<CollisionLayer, Boolean>> matrix;

    private CollisionLayerMatrix() {
        this.matrix = new EnumMap<>(CollisionLayer.class);
        initializeMatrix();
        defineCollisionRules();
        LOGGER.log(Level.INFO, "CollisionLayerMatrix initialized with collision rules");
    }

    public static CollisionLayerMatrix getInstance() {
        return INSTANCE;
    }

    /**
     * Initialize the matrix with default false values
     */
    private void initializeMatrix() {
        for (CollisionLayer layer1 : CollisionLayer.values()) {
            Map<CollisionLayer, Boolean> row = new EnumMap<>(CollisionLayer.class);
            for (CollisionLayer layer2 : CollisionLayer.values()) {
                row.put(layer2, false);
            }
            matrix.put(layer1, row);
        }
    }

    /**
     * Define collision rules
     */
    private void defineCollisionRules() {
        // Player collisions
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY_PROJECTILE, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.OBSTACLE, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.BOUNDARY, true);

        // Enemy collisions
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.PLAYER_PROJECTILE, true);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.OBSTACLE, false);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.BOUNDARY, true);

        // Asteroid-Asteroid collisions
        setLayersCollide(CollisionLayer.OBSTACLE, CollisionLayer.OBSTACLE, true);

        // Bullets collisions
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.OBSTACLE, true);
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.OBSTACLE, true);

        // Player bullets vs Enemy bullets should destroy each other
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.ENEMY_PROJECTILE, true);

        // Asteroids do NOT collide with boundaries (they wrap instead)
        setLayersCollide(CollisionLayer.OBSTACLE, CollisionLayer.BOUNDARY, false);

        // Bullets do NOT collide with boundaries
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.BOUNDARY, false);
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.BOUNDARY, false);

        // Prevent friendly fire
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.PLAYER_PROJECTILE, false);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.ENEMY_PROJECTILE, false);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.ENEMY, false);

        // Enemy bullets don't collide with each other
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.ENEMY_PROJECTILE, false);

        // INVINCIBLE layer (HUNTERS) - CAN be hit by player bullets but ignores boundaries
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.PLAYER_PROJECTILE, true);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.PLAYER, true);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.BOUNDARY, false);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.OBSTACLE, false);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.ENEMY_PROJECTILE, false);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.ENEMY, false);
        setLayersCollide(CollisionLayer.INVINCIBLE, CollisionLayer.INVINCIBLE, false);

        // DEFAULT layer interactions
        for (CollisionLayer layer : CollisionLayer.values()) {
            if (layer != CollisionLayer.INVINCIBLE && layer != CollisionLayer.DEFAULT) {
                setLayersCollide(CollisionLayer.DEFAULT, layer, true);
            }
        }

        LOGGER.log(Level.INFO, "Collision rules defined");
    }

    /**
     * Set whether two layers can collide (bidirectional)
     *
     * @param layer1 First collision layer
     * @param layer2 Second collision layer
     * @param canCollide Whether they can collide
     */
    private void setLayersCollide(CollisionLayer layer1, CollisionLayer layer2, boolean canCollide) {
        matrix.get(layer1).put(layer2, canCollide);
        matrix.get(layer2).put(layer1, canCollide);
    }

    /**
     * Check if two collision layers can collide
     *
     * @param layer1 First collision layer
     * @param layer2 Second collision layer
     * @return true if the layers can collide
     */
    public boolean canLayersCollide(CollisionLayer layer1, CollisionLayer layer2) {
        if (layer1 == null || layer2 == null) {
            return false;
        }

        Map<CollisionLayer, Boolean> row = matrix.get(layer1);
        if (row == null) {
            return false;
        }

        Boolean result = row.get(layer2);
        return result != null && result;
    }
}