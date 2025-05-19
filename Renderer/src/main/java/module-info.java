module Renderer {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
    uses dk.sdu.mmmi.cbse.common.services.IRendererSPI;

    exports dk.sdu.mmmi.cbse.renderer;

    provides dk.sdu.mmmi.cbse.common.services.IRenderingContext
            with dk.sdu.mmmi.cbse.renderer.RenderingContext;
    provides dk.sdu.mmmi.cbse.common.services.IPostProcessingService
            with dk.sdu.mmmi.cbse.renderer.RenderSystem;
    provides dk.sdu.mmmi.cbse.common.services.IRendererSPI
            with dk.sdu.mmmi.cbse.renderer.EntityRenderer;
    provides dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI
            with dk.sdu.mmmi.cbse.renderer.DebugRenderer;
}