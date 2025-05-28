package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * UPDATED: Handle burst timing properly
 */
public class WeaponSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(WeaponSystem.class.getName());

    public WeaponSystem() {
        LOGGER.log(Level.INFO, "WeaponSystem initialized");
    }

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        float deltaTime = Time.getDeltaTimeF();

        for (Entity entity : world.getEntities()) {
            WeaponComponent weapon = entity.getComponent(WeaponComponent.class);
            if (weapon == null) {
                continue;
            }

            // Update cooldowns
            weapon.updateCooldown(deltaTime);

            // Handle burst firing timing
            if (weapon.getFiringPattern() == Weapon.FiringPattern.BURST) {
                handleBurstTiming(weapon);
            }
        }
    }

    /**
     * Handle burst timing - allow firing next bullet in burst when delay is complete
     */
    private void handleBurstTiming(WeaponComponent weapon) {
        // If we're in middle of burst and delay is complete, allow next shot
        if (weapon.getCurrentBurstCount() > 0 &&
                weapon.getCurrentBurstCount() < weapon.getBurstCount() &&
                weapon.isBurstDelayComplete()) {

            // Reset main cooldown to allow next burst bullet
            weapon.setCurrentCooldown(0.0f);
        }
    }
}