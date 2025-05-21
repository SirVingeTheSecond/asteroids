package dk.sdu.mmmi.cbse.renderer;

import dk.sdu.mmmi.cbse.common.services.IRenderingContext;
import javafx.scene.canvas.GraphicsContext;

import java.util.logging.Level;
import java.util.logging.Logger;

public class RenderingContext implements IRenderingContext {
    private static final Logger LOGGER = Logger.getLogger(RenderingContext.class.getName());
    private static final RenderingContext INSTANCE = new RenderingContext();

    private GraphicsContext graphicsContext;

    // This constructor is used by ServiceLoader
    public RenderingContext() {
        if (INSTANCE != null && this != INSTANCE) {
            if (this.graphicsContext != null) {
                INSTANCE.setGraphicsContext(this.graphicsContext);
            }
        }
    }

    public static RenderingContext getInstance() {
        return INSTANCE;
    }

    @Override
    public void setGraphicsContext(GraphicsContext context) {
        if (this != INSTANCE) {
            INSTANCE.setGraphicsContext(context);
            return;
        }

        this.graphicsContext = context;
        LOGGER.log(Level.INFO, "Graphics context set on RenderingContext");
    }

    @Override
    public GraphicsContext getGraphicsContext() {
        if (this != INSTANCE) {
            return INSTANCE.getGraphicsContext();
        }
        return graphicsContext;
    }
}