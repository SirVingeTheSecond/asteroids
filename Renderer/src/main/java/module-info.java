module Renderer {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI;
    uses dk.sdu.mmmi.cbse.common.services.IRendererSPI;

    exports dk.sdu.mmmi.cbse.renderer;

    provides dk.sdu.mmmi.cbse.common.services.IPostProcessingService
            with dk.sdu.mmmi.cbse.renderer.RenderSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.renderer.RenderPlugin;

    provides dk.sdu.mmmi.cbse.common.services.IRendererSPI
            with dk.sdu.mmmi.cbse.renderer.EntityRenderer;
    provides dk.sdu.mmmi.cbse.common.services.IDebugRendererSPI
            with dk.sdu.mmmi.cbse.renderer.DebugRenderer;
}