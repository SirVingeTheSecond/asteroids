package dk.sdu.mmmi.cbse.core.ui;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUIUpdate;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for rendering game UI elements.
 */
public class GameUISystem implements IUIUpdate {
    private static final Logger LOGGER = Logger.getLogger(GameUISystem.class.getName());

    // UI Colors
    private static final Color UI_BACKGROUND = Color.color(0, 0, 0, 0.7);
    private static final Color UI_TEXT = Color.WHITE;
    private static final Color UI_ACCENT = Color.CYAN;
    private static final Color HEALTH_GREEN = Color.LIME;
    private static final Color HEALTH_YELLOW = Color.YELLOW;
    private static final Color HEALTH_RED = Color.RED;

    // UI Layout
    private static final double MARGIN = 15.0;
    private static final double LINE_HEIGHT = 20.0;
    private static final double PANEL_PADDING = 10.0;

    public GameUISystem() {

        LOGGER.log(Level.INFO, "GameUISystem initialized");
    }

    @Override
    public int getPriority() {
        return 1000; // High priority to render on top
    }

    @Override
    public void updateUI(GameData gameData, World world, GraphicsContext context) {

    }
}