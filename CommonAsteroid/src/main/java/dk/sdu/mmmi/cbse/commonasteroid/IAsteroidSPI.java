package dk.sdu.mmmi.cbse.commonasteroid;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service interface for asteroid creation and management.
 * Provides functionality to create and split asteroids.
 */
public interface IAsteroidSPI {
    /**
     * Create a new asteroid entity
     *
     * @param gameData Current game data
     * @param world The game world to add the asteroid to
     * @return New asteroid entity
     */
    Entity createAsteroid(GameData gameData, World world);

    /**
     * Create smaller asteroids from a destroyed parent asteroid
     *
     * @param asteroid The parent asteroid being split
     * @param world The game world to add the new asteroids to
     */
    void createSplitAsteroid(Entity asteroid, World world);
}