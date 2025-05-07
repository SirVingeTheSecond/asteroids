package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPostEntityProcessingService;

/**
 * System that wraps entities around screen edges.
 * Runs as a post-processor after movement is applied.
 */
public class ScreenWrapSystem implements IPostEntityProcessingService {

    @Override
    public void process(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            // Skip entities without transform component
            if (!entity.hasComponent(TransformComponent.class)) {
                continue;
            }

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            handleScreenWrap(transform, gameData);
        }
    }

    /**
     * Wrap entity position if it goes off-screen
     */
    private void handleScreenWrap(TransformComponent transform, GameData gameData) {
        float x = transform.getX();
        float y = transform.getY();
        float width = gameData.getDisplayWidth();
        float height = gameData.getDisplayHeight();

        // Wrap position
        if (x < 0) x = width;
        else if (x > width) x = 0;

        if (y < 0) y = height;
        else if (y > height) y = 0;

        // If position changed, update the transform
        if (x != transform.getX() || y != transform.getY()) {
            transform.setPosition(new Vector2D(x, y));
        }
    }
}