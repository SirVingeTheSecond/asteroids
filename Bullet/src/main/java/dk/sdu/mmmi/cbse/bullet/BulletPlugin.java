package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for bullet system.
 */
public class BulletPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(BulletPlugin.class.getName());

    @Override
    public void start(GameData gameData, World world) {
        // No use :)
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "BulletPlugin stopping - removing all bullets");

        // Remove all bullets
        int removedCount = 0;
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent != null && tagComponent.hasType(EntityType.BULLET)) {
                world.removeEntity(entity);
                removedCount++;
            }
        }

        LOGGER.log(Level.INFO, "Removed {0} bullets", removedCount);
    }
}