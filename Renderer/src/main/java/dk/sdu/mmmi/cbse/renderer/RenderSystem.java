package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
import dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
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
public class RenderSystem implements IPostProcessingService {
    private static final Logger LOGGER = Logger.getLogger(RenderSystem.class.getName());

    private final IRendererSPI entityRenderer;
    private final IDebugRendererSPI debugRenderer;

    /**
     * Create a new render system
     */
    public RenderSystem() {
        // Load renderer services
        this.entityRenderer = ServiceLoader.load(IRendererSPI.class).findFirst().orElse(null);
        this.debugRenderer = ServiceLoader.load(IDebugRendererSPI.class).findFirst().orElse(null);

        LOGGER.log(Level.INFO, "RenderSystem initialized");
    }

    @Override
    public void process(GameData gameData, World world) {
        GraphicsContext context = RenderingContext.getInstance().getGraphicsContext();
        if (context == null) {
            LOGGER.log(Level.WARNING, "Cannot render: graphics context not set");
            return;
        }

        if (entityRenderer == null) {
            LOGGER.log(Level.WARNING, "Cannot render: entity renderer not available");
            return;
        }

        // Clear the screen
        entityRenderer.clear(gameData.getDisplayWidth(), gameData.getDisplayHeight());

        // Render all entities in sorted order
        renderEntities(world);

        // Render debug information if enabled
        if (debugRenderer != null && gameData.isDebugMode()) {
            debugRenderer.render(gameData, world);
        }

        // Display FPS if debug mode enabled
        if (gameData.isDebugMode()) {
            renderFpsCounter(context, gameData);
        }
    }

    /**
     * Render all entities in sorted order by render layer and Y position
     */
    private void renderEntities(World world) {
        // Get all entities with renderer components
        List<Entity> renderableEntities = getRenderableEntities(world);

        // Sort by render layer (low to high) and then by Y position
        renderableEntities.sort(Comparator.comparingInt(e ->
                        e.getComponent(RendererComponent.class).getRenderLayer())
                .thenComparingDouble(e ->
                        e.getComponent(dk.sdu.mmmi.cbse.common.components.TransformComponent.class).getY()));

        // Render all entities
        int renderedCount = 0;
        for (Entity entity : renderableEntities) {
            if (entityRenderer.render(entity)) {
                renderedCount++;
            }
        }

        LOGGER.log(Level.FINEST, "Rendered {0} entities", renderedCount);
    }

    /**
     * Get all entities with renderer components
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
     */
    private void renderFpsCounter(GraphicsContext context, GameData gameData) {
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