import dk.sdu.mmmi.cbse.common.services.ILateUpdate;

module Renderer {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    exports dk.sdu.mmmi.cbse.renderer;

    provides dk.sdu.mmmi.cbse.common.services.IRenderingContext
            with dk.sdu.mmmi.cbse.renderer.RenderingContext;
}