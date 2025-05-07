package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;

public class PlayerSystem implements IEntityProcessingService {

    @Override
    public void process(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent == null || !tagComponent.hasTag(TagComponent.TAG_PLAYER)) {
                continue;
            }

            if (!entity.hasComponent(TransformComponent.class) ||
                    !entity.hasComponent(MovementComponent.class)) {
                continue;
            }

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            MovementComponent movement = entity.getComponent(MovementComponent.class);

            // Apply movement based on current state
            if (movement.isAccelerating()) {
                // Calculate movement based on rotation
                double radians = Math.toRadians(transform.getRotation());
                double deltaX = Math.cos(radians) * movement.getSpeed();
                double deltaY = Math.sin(radians) * movement.getSpeed();

                // Update position
                transform.setX(transform.getX() + deltaX);
                transform.setY(transform.getY() + deltaY);
            }

            // Handle screen boundaries
            handleScreenBoundaries(transform, gameData);
        }
    }

    private void handleScreenBoundaries(TransformComponent transform, GameData gameData) {
        // Screen wrapping
        if (transform.getX() < 0) {
            transform.setX(gameData.getDisplayWidth());
        } else if (transform.getX() > gameData.getDisplayWidth()) {
            transform.setX(0);
        }

        if (transform.getY() < 0) {
            transform.setY(gameData.getDisplayHeight());
        } else if (transform.getY() > gameData.getDisplayHeight()) {
            transform.setY(0);
        }
    }
}