package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for bullet system.
 * Manages bullet system lifecycle.
 */
public class WeaponPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(WeaponPlugin.class.getName());

    @Override
    public void start(GameData gameData, World world) {

    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "WeaponPlugin stopping - removing all weapon components");

        // Remove all weapon components
        for (Entity entity : world.getEntities()) {
            entity.removeComponent(WeaponComponent.class);
        }
    }
}