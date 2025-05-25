package dk.sdu.mmmi.cbse.movementsystem;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that wraps specific entities around screen edges.
 * Only asteroids wrap - other entities use boundary collision system.
 */
public class ScreenWrapSystem implements ILateUpdate {
    private static final Logger LOGGER = Logger.getLogger(ScreenWrapSystem.class.getName());

    @Override
    public int getPriority() {
        return 200; // Run after physics and collision systems
    }

    @Override
    public void process(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            if (!entity.hasComponent(TransformComponent.class)) {
                continue;
            }

            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent == null) {
                continue;
            }

            // Only wrap asteroids - other entities should use boundary system
            if (!tagComponent.hasType(EntityType.ASTEROID)) {
                continue;
            }

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            boolean wrapped = handleScreenWrap(transform, gameData);

            if (wrapped) {
                LOGGER.log(Level.FINE, "Asteroid wrapped at screen edge: {0}",
                        transform.getPosition());
            }
        }
    }

    /**
     * Wrap entity position if it goes off-screen
     *
     * @param transform Entity transform
     * @param gameData Game data with screen dimensions
     * @return true if position was wrapped
     */
    private boolean handleScreenWrap(TransformComponent transform, GameData gameData) {
        float x = transform.getX();
        float y = transform.getY();
        float width = gameData.getDisplayWidth();
        float height = gameData.getDisplayHeight();
        float radius = transform.getRadius();

        boolean wrapped = false;

        // Add buffer for smooth wrapping (entity completely off-screen before wrapping)
        float buffer = radius * 1.5f;

        // Horizontal wrapping
        if (x + buffer < 0) {
            x = width + buffer;
            wrapped = true;
        } else if (x - buffer > width) {
            x = -buffer;
            wrapped = true;
        }

        // Vertical wrapping
        if (y + buffer < 0) {
            y = height + buffer;
            wrapped = true;
        } else if (y - buffer > height) {
            y = -buffer;
            wrapped = true;
        }

        // If position changed, update the transform
        if (wrapped) {
            transform.setPosition(new Vector2D(x, y));
        }

        return wrapped;
    }
}