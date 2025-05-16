package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.core.Time;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for processing bullet movement.
 */
public class BulletSystem implements IProcessingService {
    private static final Logger LOGGER = Logger.getLogger(BulletSystem.class.getName());

    @Override
    public void process(GameData gameData, World world) {
        float deltaTime = (float) Time.getDeltaTime();
        List<Entity> bulletsToRemove = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent == null || !tagComponent.hasType(EntityType.BULLET)) {
                continue;
            }

            // Get required components
            BulletComponent bulletComponent = entity.getComponent(BulletComponent.class);
            TransformComponent transform = entity.getComponent(TransformComponent.class);

            if (bulletComponent == null || transform == null) {
                LOGGER.log(Level.WARNING, "Bullet entity {0} missing required components", entity.getID());
                continue;
            }

            // Basic movement - apply velocity based on forward direction and speed
            Vector2D forward = transform.getForward();
            Vector2D velocity = forward.scale(bulletComponent.getSpeed() * deltaTime);
            transform.translate(velocity);

            // Check if bullet has expired or is out of bounds
            if (isOutOfBounds(transform, gameData)) {
                bulletsToRemove.add(entity);
            }
        }

        // Remove expired bullets
        for (Entity bullet : bulletsToRemove) {
            world.removeEntity(bullet);
        }
    }

    /**
     * Check if bullet is out of the game area
     *
     * @param transform Bullet's transform component
     * @param gameData Game data containing screen dimensions
     * @return true if bullet is out of bounds
     */
    private boolean isOutOfBounds(TransformComponent transform, GameData gameData) {
        float x = transform.getX();
        float y = transform.getY();
        float margin = 50.0f;

        return x < -margin || x > gameData.getDisplayWidth() + margin ||
                y < -margin || y > gameData.getDisplayHeight() + margin;
    }
}