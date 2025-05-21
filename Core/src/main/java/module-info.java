import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;

module Core {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    uses ILateUpdate;
    uses IUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    uses dk.sdu.mmmi.cbse.common.services.IRendererSPI;

    provides dk.sdu.mmmi.cbse.common.services.IEventService
            with dk.sdu.mmmi.cbse.core.events.EventService;

    exports dk.sdu.mmmi.cbse.core;
    exports dk.sdu.mmmi.cbse.core.input;
    exports dk.sdu.mmmi.cbse.core.utils;
    exports dk.sdu.mmmi.cbse.core.events;
}