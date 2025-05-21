package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.services.IRenderingContext;
import javafx.scene.canvas.GraphicsContext;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Singleton implementation of IRenderingContext.
 */
// Is this still relevant for the architecture?
public class RenderingContext implements IRenderingContext {
    private static final Logger LOGGER = Logger.getLogger(RenderingContext.class.getName());
    private static final RenderingContext INSTANCE = new RenderingContext();

    private GraphicsContext graphicsContext;

    public RenderingContext() {

    }

    public static RenderingContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void setGraphicsContext(GraphicsContext context) {
        this.graphicsContext = context;
        LOGGER.log(Level.INFO, "Graphics context set on RenderingContext");
    }

    @Override
    public GraphicsContext getGraphicsContext() {
        return graphicsContext;
    }
}