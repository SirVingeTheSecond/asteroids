package dk.sdu.mmmi.cbse.commonasteroid;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Interface for asteroid splitting functionality.
 */
public interface IAsteroidSplitter {
    /**
     * Creates new asteroids from a split asteroid.
     *
     * @param entity The asteroid entity to split
     * @param world The game world
     */

    void createSplitAsteroid(Entity entity, World world);
}