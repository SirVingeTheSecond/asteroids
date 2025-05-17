package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.Pair;

import java.util.List;

/**
 * Service Provider Interface for collision functionality.
 */
public interface ICollisionSPI {
    /**
     * Detect collisions between entities in the world
     *
     * @param gameData Current game state
     * @param world Game world with entities
     * @return List of collision pairs
     */
    List<Pair<Entity, Entity>> detectCollisions(GameData gameData, World world);

    /**
     * Check if two entities are colliding
     *
     * @param entity1 First entity
     * @param entity2 Second entity
     * @return true if colliding
     */
    boolean isColliding(Entity entity1, Entity entity2);

    /**
     * Check if two collision layers can collide
     *
     * @param layer1 First collision layer
     * @param layer2 Second collision layer
     * @return true if layers can collide
     */
    boolean canLayersCollide(CollisionLayer layer1, CollisionLayer layer2);
}