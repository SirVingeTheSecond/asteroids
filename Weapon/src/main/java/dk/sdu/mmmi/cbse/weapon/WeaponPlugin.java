package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IGamePluginService;

/**
 * Plugin for bullet system.
 * Manages bullet system lifecycle.
 */
public class WeaponPlugin implements IGamePluginService {

    @Override
    public void start(GameData gameData, World world) {
        // The BulletSystem is instantiated as a service
        // No need to manually create it here
    }

    @Override
    public void stop(GameData gameData, World world) {
        // Remove all bullets
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && tagComponent.hasType(EntityType.BULLET)) {
                world.removeEntity(entity);
            }
        }
    }
}