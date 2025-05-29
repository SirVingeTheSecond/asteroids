package dk.sdu.mmmi.cbse.commonui;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service Provider Interface for UI functionality.
 */
public interface IUIService {
    /**
     * Create the heads-up display
     *
     * @param gameData Current game state
     * @param world Game world
     */
    void createHUD(GameData gameData, World world);

    /**
     * Update UI based on player state
     *
     * @param player Player entity
     */
    void updatePlayerUI(Entity player);

    /**
     * Show game over screen
     */
    void showGameOver();

    /**
     * Hide game over screen
     */
    void hideGameOver();
}