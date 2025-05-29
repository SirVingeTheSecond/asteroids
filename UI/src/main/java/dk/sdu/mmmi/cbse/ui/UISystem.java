package dk.sdu.mmmi.cbse.ui;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonui.IUIService;
import dk.sdu.mmmi.cbse.commonui.UIComponent;
import dk.sdu.mmmi.cbse.commonui.UIType;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for updating UI elements based on game state.
 * Monitors player state and updates UI text accordingly.
 */
public class UISystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(UISystem.class.getName());

    private IUIService uiService;
    private boolean hudInitialized = false;

    public UISystem() {
        this.uiService = ServiceLoader.load(IUIService.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "UISystem initialized");
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void update(GameData gameData, World world) {
        if (uiService == null) {
            uiService = ServiceLoader.load(IUIService.class).findFirst().orElse(null);
            if (uiService == null) {
                return;
            }
        }

        if (!hudInitialized) {
            uiService.createHUD(gameData, world);
            hudInitialized = true;
            LOGGER.log(Level.INFO, "HUD initialized");
        }

        Entity player = findPlayer(world);
        if (player != null) {
            updateUIElements(player, world);
        }
    }

    private void updateUIElements(Entity player, World world) {
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        WeaponComponent weaponComponent = player.getComponent(WeaponComponent.class);

        if (playerComponent == null) {
            return;
        }

        for (Entity uiEntity : world.getEntities()) {
            UIComponent uiComponent = uiEntity.getComponent(UIComponent.class);
            if (uiComponent == null || !uiComponent.isAutoUpdate()) {
                continue;
            }

            switch (uiComponent.getUIType()) {
                case HEALTH_DISPLAY:
                    updateHealthDisplay(uiComponent, playerComponent);
                    break;
                case LIVES_DISPLAY:
                    updateLivesDisplay(uiComponent, playerComponent);
                    break;
                case WEAPON_DISPLAY:
                    updateWeaponDisplay(uiComponent, weaponComponent);
                    break;
            }
        }
    }

    private void updateHealthDisplay(UIComponent uiComponent, PlayerComponent playerComponent) {
        StringBuilder healthText = new StringBuilder("Health: ");
        int currentHealth = playerComponent.getCurrentHealth();
        int maxHealth = playerComponent.getMaxHealth();

        for (int i = 0; i < maxHealth; i++) {
            if (i < currentHealth) {
                healthText.append("♥ ");
            } else {
                healthText.append("♡ ");
            }
        }

        uiComponent.setDisplayText(healthText.toString());
    }

    private void updateLivesDisplay(UIComponent uiComponent, PlayerComponent playerComponent) {
        uiComponent.setDisplayText("Lives: " + playerComponent.getLives());
    }

    private void updateWeaponDisplay(UIComponent uiComponent, WeaponComponent weaponComponent) {
        if (weaponComponent != null) {
            String weaponText = "Weapon: " + weaponComponent.getBulletType().toUpperCase();
            uiComponent.setDisplayText(weaponText);
        } else {
            uiComponent.setDisplayText("Weapon: NONE");
        }
    }

    private Entity findPlayer(World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                return entity;
            }
        }
        return null;
    }
}