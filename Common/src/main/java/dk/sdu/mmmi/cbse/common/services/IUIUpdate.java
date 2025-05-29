package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for systems that render User Interface elements.
 * UI updates run after all other processing to overlay information on the game.
 */
public interface IUIUpdate {
    /**
     * Render UI elements to the graphics context
     *
     * @param gameData Current game state
     * @param world Game world containing entities
     * @param context Graphics context for rendering UI
     */
    void updateUI(GameData gameData, World world, GraphicsContext context);

    /**
     * Get the priority of this UI update system.
     * Lower values are processed first (background UI).
     * Higher values are processed last (overlay UI).
     *
     * @return Priority value
     */
    int getPriority();
}