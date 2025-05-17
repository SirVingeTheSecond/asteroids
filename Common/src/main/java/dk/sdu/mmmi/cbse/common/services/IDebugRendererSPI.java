package dk.sdu.mmmi.cbse.common.services;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import javafx.scene.canvas.GraphicsContext;

/**
 * Service interface for debug visualization.
 * Allows systems to draw debug information.
 */
public interface IDebugRendererSPI {
    /**
     * Enable or disable debug rendering
     *
     * @param enabled true to enable debug rendering
     */
    void setEnabled(boolean enabled);

    /**
     * Check if debug rendering is enabled
     *
     * @return true if debug rendering is enabled
     */
    boolean isEnabled();

    /**
     * Render debug visualization
     *
     * @param gc Graphics context to draw on
     * @param gameData Current game state
     * @param world Game world with entities
     */
    void render(GraphicsContext gc, GameData gameData, World world);
}