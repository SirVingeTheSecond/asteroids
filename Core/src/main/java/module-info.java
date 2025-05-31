module Core {
    requires Common;
    requires java.logging;
    requires java.desktop;
    requires javafx.graphics;
    requires javafx.controls;

    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    uses dk.sdu.mmmi.cbse.common.services.IUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
    uses dk.sdu.mmmi.cbse.common.services.ILateUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IRenderingContext;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;

    exports dk.sdu.mmmi.cbse.core;
    exports dk.sdu.mmmi.cbse.core.input;
    exports dk.sdu.mmmi.cbse.core.utils;
    exports dk.sdu.mmmi.cbse.core.events;

    provides dk.sdu.mmmi.cbse.common.services.IEventService
            with dk.sdu.mmmi.cbse.core.events.EventService;
}