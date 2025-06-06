module Renderer {
    requires Common;
    requires CommonUI;
    requires java.logging;
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.common.services.IRenderingContext;

    exports dk.sdu.mmmi.cbse.renderer;

    provides dk.sdu.mmmi.cbse.common.services.IRenderingContext
            with dk.sdu.mmmi.cbse.renderer.RenderingContext;

    provides dk.sdu.mmmi.cbse.common.services.ILateUpdate
            with dk.sdu.mmmi.cbse.renderer.RenderSystem;
}