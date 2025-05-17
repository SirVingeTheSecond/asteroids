module Renderer {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    exports dk.sdu.mmmi.cbse.renderer;

    provides dk.sdu.mmmi.cbse.common.services.IProcessingService
            with dk.sdu.mmmi.cbse.renderer.RenderSystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.renderer.RenderPlugin;
}