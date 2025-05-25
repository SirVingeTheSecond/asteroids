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
 * System that wraps entities around screen edges.
 * Excludes players when boundary collision system is active.
 */
public class ScreenWrapSystem implements ILateUpdate {
    private static final Logger LOGGER = Logger.getLogger(ScreenWrapSystem.class.getName());

    // Configuration flag - set to false when using boundary collision system
    private static final boolean ENABLE_PLAYER_WRAP = false;

    @Override
    public int getPriority() {
        return 100;
    }

    @Override
    public void process(GameData gameData, World world) {
        for (Entity entity : world.getEntities()) {
            if (!entity.hasComponent(TransformComponent.class)) {
                continue;
            }

            TagComponent tagComponent = entity.getComponent(TagComponent.class);

            // Skip bullets - they should be destroyed when out of bounds, not wrapped
            if (tagComponent != null && tagComponent.hasType(EntityType.BULLET)) {
                continue;
            }

            // Skip players when boundary collision is enabled (controlled by flag)
            if (!ENABLE_PLAYER_WRAP && tagComponent != null && tagComponent.hasType(EntityType.PLAYER)) {
                continue;
            }

            TransformComponent transform = entity.getComponent(TransformComponent.class);
            boolean wrapped = handleScreenWrap(transform, gameData);

            if (wrapped) {
                LOGGER.log(Level.FINE, "Entity wrapped at screen edge: {0}",
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

        boolean wrapped = false;

        // Wrap position
        if (x < 0) {
            x = width;
            wrapped = true;
        } else if (x > width) {
            x = 0;
            wrapped = true;
        }

        if (y < 0) {
            y = height;
            wrapped = true;
        } else if (y > height) {
            y = 0;
            wrapped = true;
        }

        // If position changed, update the transform
        if (wrapped) {
            transform.setPosition(new Vector2D(x, y));
        }

        return wrapped;
    }

    /**
     * Enable or disable player wrapping
     * @param enable true to enable player wrapping (disable boundary collision)
     */
    public static void setPlayerWrapEnabled(boolean enable) {
        // ToDo: This would need to be implemented as a proper configuration system
        LOGGER.log(Level.INFO, "Player wrap setting: {0} (requires restart)", enable);
    }
}