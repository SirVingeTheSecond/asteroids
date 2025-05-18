package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.common.services.IRendererSPI;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for rendering entities on screen.
 * Processes all entities with renderer components.
 */
public class RenderSystem implements IProcessingService {
    private static final Logger LOGGER = Logger.getLogger(RenderSystem.class.getName());

    private GraphicsContext context;
    private final IRendererSPI renderer;
    private final IDebugRendererSPI debugRenderer;

    /**
     * Create a new render system
     */
    public RenderSystem() {
        this.renderer = new EntityRenderer();
        this.debugRenderer = ServiceLoader.load(IDebugRendererSPI.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "RenderSystem initialized");
    }

    /**
     * Set graphics context for rendering
     *
     * @param context JavaFX graphics context
     */
    public void setGraphicsContext(GraphicsContext context) {
        this.context = context;
        this.renderer.setGraphicsContext(context);
        LOGGER.log(Level.FINE, "Graphics context set");
    }

    @Override
    public void process(GameData gameData, World world) {
        if (context == null) {
            LOGGER.log(Level.WARNING, "Cannot render: graphics context not set");
            return;
        }

        // Clear canvas
        renderer.clear(gameData.getDisplayWidth(), gameData.getDisplayHeight());

        // Get all entities with renderer components
        List<Entity> renderableEntities = getRenderableEntities(world);

        // Sort by render layer (low to high)
        renderableEntities.sort(Comparator.comparingInt(
                e -> e.getComponent(RendererComponent.class).getRenderLayer()));

        // Render all entities
        int renderedCount = 0;
        for (Entity entity : renderableEntities) {
            if (renderer.render(entity)) {
                renderedCount++;
            }
        }

        // Render debug information if enabled
        if (debugRenderer != null && debugRenderer.isEnabled() && gameData.isDebugMode()) {
            debugRenderer.render(context, gameData, world);
        }

        // Display FPS if debug mode enabled
        if (gameData.isDebugMode()) {
            renderFpsCounter(gameData);
        }

        LOGGER.log(Level.FINEST, "Rendered {0} entities", renderedCount);
    }

    /**
     * Get all entities with renderer components
     *
     * @param world Game world with entities
     * @return List of renderable entities
     */
    private List<Entity> getRenderableEntities(World world) {
        List<Entity> renderableEntities = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            if (entity.hasComponent(RendererComponent.class)) {
                renderableEntities.add(entity);
            }
        }

        return renderableEntities;
    }

    /**
     * Render FPS counter in debug mode
     *
     * @param gameData Game data with timing information
     */
    private void renderFpsCounter(GameData gameData) {
        float fps = 1.0f / Math.max(0.0001f, gameData.getDeltaTime());

        // Save context state
        context.save();

        // Reset transform
        context.setTransform(1, 0, 0, 1, 0, 0);

        // Draw FPS counter
        context.setFill(Color.WHITE);
        context.fillText(String.format("FPS: %.1f", fps), 10, 20);

        // Restore context state
        context.restore();
    }
}