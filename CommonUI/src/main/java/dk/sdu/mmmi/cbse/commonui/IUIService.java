package dk.sdu.mmmi.cbse.commonui;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

/**
 * Service Provider Interface for UI functionality.
 */
public interface IUIService {
    /**
     * Initialize and create the heads-up display elements
     *
     * @param gameData Current game state
     * @param world Game world to add UI entities to
     */
    void createHUD(GameData gameData, World world);

    /**
     * Update UI visibility state
     *
     * @param visible true to show UI, false to hide
     */
    void setUIVisible(boolean visible);
}