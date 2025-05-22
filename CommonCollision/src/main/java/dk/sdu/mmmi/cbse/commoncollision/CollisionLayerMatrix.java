package dk.sdu.mmmi.cbse.commoncollision;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages collisions between different collision layers.
 */
public class CollisionLayerMatrix {
    private static final Logger LOGGER = Logger.getLogger(CollisionLayerMatrix.class.getName());
    private static final CollisionLayerMatrix INSTANCE = new CollisionLayerMatrix();

    private final Map<CollisionLayer, Set<CollisionLayer>> collisionMatrix = new EnumMap<>(CollisionLayer.class);

    private CollisionLayerMatrix() {
        initializeDefaultMatrix();
        LOGGER.log(Level.INFO, "CollisionLayerMatrix initialized");
    }

    public static CollisionLayerMatrix getInstance() {
        return INSTANCE;
    }

    private void initializeDefaultMatrix() {
        // Initialize sets for each layer
        for (CollisionLayer layer : CollisionLayer.values()) {
            collisionMatrix.put(layer, new HashSet<>());
        }

        // Set up default collision relationships

        // PLAYER collides with ENEMY, ENEMY_PROJECTILE, OBSTACLE
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY_PROJECTILE, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.OBSTACLE, true);
        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.TRIGGER, true);

        // ENEMY collides with PLAYER, PLAYER_PROJECTILE, OBSTACLE
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.PLAYER_PROJECTILE, true);
        setLayersCollide(CollisionLayer.ENEMY, CollisionLayer.OBSTACLE, true);

        // PLAYER_PROJECTILE collides with ENEMY, OBSTACLE
        setLayersCollide(CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.OBSTACLE, true);

        setLayersCollide(CollisionLayer.PLAYER, CollisionLayer.PLAYER_PROJECTILE, false);

        // ENEMY_PROJECTILE collides with PLAYER, OBSTACLE
        setLayersCollide(CollisionLayer.ENEMY_PROJECTILE, CollisionLayer.OBSTACLE, true);

        // DEFAULT collides with everything except INVINCIBLE
        for (CollisionLayer layer : CollisionLayer.values()) {
            if (layer != CollisionLayer.INVINCIBLE) {
                setLayersCollide(CollisionLayer.DEFAULT, layer, true);
            }
        }
    }

    public void setLayersCollide(CollisionLayer layer1, CollisionLayer layer2, boolean canCollide) {
        Set<CollisionLayer> collisionsLayer1 = collisionMatrix.get(layer1);
        Set<CollisionLayer> collisionsLayer2 = collisionMatrix.get(layer2);

        if (canCollide) {
            collisionsLayer1.add(layer2);
            collisionsLayer2.add(layer1);
        } else {
            collisionsLayer1.remove(layer2);
            collisionsLayer2.remove(layer1);
        }
    }

    public boolean canLayersCollide(CollisionLayer layer1, CollisionLayer layer2) {
        if (layer1 == CollisionLayer.INVINCIBLE || layer2 == CollisionLayer.INVINCIBLE) {
            return false;
        }

        Set<CollisionLayer> collisionsLayer1 = collisionMatrix.get(layer1);
        return collisionsLayer1 != null && collisionsLayer1.contains(layer2);
    }

    public Set<CollisionLayer> getCollidingLayers(CollisionLayer layer) {
        return collisionMatrix.getOrDefault(layer, new HashSet<>());
    }
}