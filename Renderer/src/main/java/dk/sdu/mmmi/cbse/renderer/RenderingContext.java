package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.services.IRenderingContext;
import javafx.scene.canvas.GraphicsContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Implementation of IRenderingContext using a registry pattern.
 * Maintains a static state to share the GraphicsContext across all instances.
 */
public class RenderingContext implements IRenderingContext {
    private static final Logger LOGGER = Logger.getLogger(RenderingContext.class.getName());

    private static GraphicsContext sharedContext;

    @Override
    public void setGraphicsContext(GraphicsContext context) {
        if (context == null) {
            LOGGER.log(Level.WARNING, "Attempt to set null GraphicsContext");
            return;
        }

        sharedContext = context;
        LOGGER.log(Level.INFO, "Graphics context set on RenderingContext");
    }

    @Override
    public GraphicsContext getGraphicsContext() {
        if (sharedContext == null) {
            LOGGER.log(Level.FINE, "Graphics context requested but not yet set");
        }
        return sharedContext;
    }

    /**
     * Check if the graphics context has been initialized
     *
     * @return true if the context is available
     */
    public static boolean isInitialized() {
        return sharedContext != null;
    }

    /**
     * Clear the graphics context registry
     * Mainly useful for testing or shutdown
     */
    public static void clearContext() {
        sharedContext = null;
        LOGGER.log(Level.FINE, "Graphics context cleared");
    }
}