package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for bullet cleanup (removing out-of-bounds bullets).
 */
public class BulletSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(BulletSystem.class.getName());

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void update(GameData gameData, World world) {
        List<Entity> bulletsToRemove = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent == null || !tagComponent.hasType(EntityType.BULLET)) {
                continue;
            }

            // Only check for out-of-bounds bullets
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            if (transform == null) {
                continue;
            }

            // Check if bullet is out of bounds
            if (isOutOfBounds(transform, gameData)) {
                bulletsToRemove.add(entity);
            }
        }

        // Remove expired bullets
        for (Entity bullet : bulletsToRemove) {
            world.removeEntity(bullet);
            LOGGER.log(Level.FINE, "Removed bullet: {0}", bullet.getID());
        }
    }

    private boolean isOutOfBounds(TransformComponent transform, GameData gameData) {
        float x = transform.getX();
        float y = transform.getY();
        float margin = 50.0f;

        return x < -margin // Left of display
                || x > gameData.getDisplayWidth() + margin // Right of display
                || y < -margin // Top of display
                || y > gameData.getDisplayHeight() + margin; // Bottom of display
    }
}