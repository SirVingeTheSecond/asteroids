package dk.sdu.mmmi.cbse.ui;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commonui.IUIService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Service implementation for UI functionality.
 * Handles creation and management of UI elements.
 */
public class UIService implements IUIService {
    private static final Logger LOGGER = Logger.getLogger(UIService.class.getName());

    private final UIEntityFactory uiFactory;

    public UIService() {
        this.uiFactory = new UIEntityFactory();
        LOGGER.log(Level.INFO, "UIService initialized");
    }

    @Override
    public void createHUD(GameData gameData, World world) {
        world.addEntity(uiFactory.createHealthDisplay(gameData));
        world.addEntity(uiFactory.createLivesDisplay(gameData));
        world.addEntity(uiFactory.createWeaponDisplay(gameData));

        LOGGER.log(Level.INFO, "HUD created with health, lives, and weapon displays");
    }

    @Override
    public void setUIVisible(boolean visible) {
        LOGGER.log(Level.INFO, "UI visibility set to: {0}", visible);
    }
}