package dk.sdu.mmmi.cbse.ui;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IScoreSPI;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonui.IUIService;
import dk.sdu.mmmi.cbse.commonui.UIComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for updating UI elements based on game state.
 */
public class UISystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(UISystem.class.getName());

    private IUIService uiService;
    private IScoreSPI scoreSPI;
    private boolean hudInitialized = false;

    public UISystem() {
        this.uiService = ServiceLoader.load(IUIService.class).findFirst().orElse(null);
        this.scoreSPI = ServiceLoader.load(IScoreSPI.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "UISystem initialized with score service integration");
    }

    @Override
    public int getPriority() {
        return 200;
    }

    @Override
    public void update(GameData gameData, World world) {
        // Refresh services if not available
        refreshServices();

        if (uiService == null) {
            return;
        }

        if (!hudInitialized) {
            uiService.createHUD(gameData, world);
            hudInitialized = true;
            LOGGER.log(Level.INFO, "HUD initialized with microservice score display");
        }

        Entity player = findPlayer(world);
        if (player != null) {
            updateUIElements(player, world);
        }
    }

    /**
     * Refresh service references if they weren't available during initialization
     */
    private void refreshServices() {
        if (uiService == null) {
            uiService = ServiceLoader.load(IUIService.class).findFirst().orElse(null);
        }
        if (scoreSPI == null) {
            scoreSPI = ServiceLoader.load(IScoreSPI.class).findFirst().orElse(null);
            if (scoreSPI != null) {
                LOGGER.log(Level.INFO, "Score service became available: {0}", scoreSPI.getServiceInfo());
            }
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
                case SCORE_DISPLAY:
                    updateScoreDisplay(uiComponent);
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

    /**
     * Update score display using the authoritative microservice score
     */
    private void updateScoreDisplay(UIComponent uiComponent) {
        if (scoreSPI != null) {
            try {
                int currentScore = scoreSPI.getCurrentScore();
                String scoreText = String.format("Score: %d", currentScore);
                uiComponent.setDisplayText(scoreText);

                LOGGER.log(Level.FINEST, "Score updated from microservice: {0}", currentScore);
            } catch (Exception e) {
                uiComponent.setDisplayText("Score: Error");
                LOGGER.log(Level.WARNING, "Failed to get score from microservice", e);
            }
        } else {
            uiComponent.setDisplayText("Score: NA");
            LOGGER.log(Level.FINE, "Score service not available for UI update");
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