package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IRenderingContext;
import javafx.scene.canvas.GraphicsContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System responsible for rendering all entities with RendererComponent.
 * Runs as a post-processor after all entity updates.
 */
public class RenderSystem implements ILateUpdate {
    private static final Logger LOGGER = Logger.getLogger(RenderSystem.class.getName());
    private static final int RENDER_PRIORITY = Integer.MAX_VALUE;

    private IRenderingContext renderingContext;

    public RenderSystem() {
        renderingContext = ServiceLoader.load(IRenderingContext.class).findFirst().orElse(null);
    }

    @Override
    public void process(GameData gameData, World world) {
        if (renderingContext == null) {
            renderingContext = ServiceLoader.load(IRenderingContext.class).findFirst().orElse(null);

            if (renderingContext == null) {
                LOGGER.log(Level.SEVERE, "No IRenderingContext implementation available");
                return;
            }
        }

        GraphicsContext context = renderingContext.getGraphicsContext();
        if (context == null) {
            LOGGER.log(Level.WARNING, "No GraphicsContext available for rendering");
            return;
        }

        // Clear the canvas
        context.clearRect(0, 0, gameData.getDisplayWidth(), gameData.getDisplayHeight());

        // Collect entities with renderer and transform components
        List<Entity> renderableEntities = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            if (entity.hasComponent(RendererComponent.class) &&
                    entity.hasComponent(TransformComponent.class)) {
                renderableEntities.add(entity);
            }
        }

        // Sort entities by render layer (lower values are rendered first, higher values on top)
        renderableEntities.sort(Comparator.comparingInt(e ->
                e.getComponent(RendererComponent.class).getRenderLayer().getValue()));

        for (Entity entity : renderableEntities) {
            EntityRenderer.renderEntity(entity, context, gameData.isDebugMode());
        }

        if (gameData.isDebugMode()) {
            renderDebugInfo(context, gameData, world);
        }
    }

    /**
     * Render debug information on screen
     */
    private void renderDebugInfo(GraphicsContext context, GameData gameData, World world) {
        context.save();
        context.setFill(javafx.scene.paint.Color.WHITE);
        context.fillText("FPS: " + (int)(1.0 / gameData.getDeltaTime()), 10, 20);
        context.fillText("Entities: " + world.getEntities().size(), 10, 40);
        context.fillText("Delta Time: " + String.format("%.4f", gameData.getDeltaTime()), 10, 60);
        context.restore();
    }

    @Override
    public int getPriority() {
        return RENDER_PRIORITY;
    }
}