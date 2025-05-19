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

    private GraphicsContext context;
    private IRendererSPI renderer;
    private IDebugRendererSPI debugRenderer;

    public RenderSystem() {
        loadServices();
        LOGGER.log(Level.INFO, "RenderSystem initialized");
    }

    /**
     * Load renderer services using ServiceLoader.
     */
    private void loadServices() {
        try {
            this.renderer = ServiceLoader.load(IRendererSPI.class).findFirst().orElse(null);
            this.debugRenderer = ServiceLoader.load(IDebugRendererSPI.class).findFirst().orElse(null);

            if (this.renderer == null) {
                LOGGER.log(Level.WARNING, "No IRendererSPI implementation found");
            } else {
                LOGGER.log(Level.FINE, "Loaded renderer: {0}", renderer.getClass().getName());
            }

            if (this.debugRenderer == null) {
                LOGGER.log(Level.FINE, "No IDebugRendererSPI implementation found");
            } else {
                LOGGER.log(Level.FINE, "Loaded debug renderer: {0}", debugRenderer.getClass().getName());
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error loading renderer services", e);
        }
    }

    /**
     * Set graphics context for rendering
     *
     * @param context JavaFX graphics context
     */
    public void setGraphicsContext(GraphicsContext context) {
        this.context = context;

        if (renderer == null) {
            // Try to load renderer again
            loadServices();
        }

        if (renderer != null) {
            renderer.setGraphicsContext(context);
            LOGGER.log(Level.FINE, "Graphics context set for renderer");
        } else {
            LOGGER.log(Level.WARNING, "Cannot set graphics context: renderer not available");
        }
    }

    @Override
    public void process(GameData gameData, World world) {
        if (context == null) {
            LOGGER.log(Level.WARNING, "Cannot render: graphics context not set");
            return;
        }

        if (renderer == null) {
            // Try to load renderer one more time
            loadServices();
            if (renderer == null) {
                LOGGER.log(Level.WARNING, "Cannot render: renderer not available");
                return;
            } else {
                renderer.setGraphicsContext(context);
            }
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
        if (debugRenderer != null && gameData.isDebugMode()) {
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