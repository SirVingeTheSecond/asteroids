package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing weapon states.
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
    public void process(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            WeaponComponent weapon = entity.getComponent(WeaponComponent.class);
            if (weapon == null) {
                continue;
            }

            weapon.updateCooldown();

            // ToDo: Anything missing in here?
        }
    }
}