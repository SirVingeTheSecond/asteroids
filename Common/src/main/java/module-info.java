module Common {
    requires java.logging;
    requires javafx.graphics;
    exports dk.sdu.mmmi.cbse.common.data;
    exports dk.sdu.mmmi.cbse.common.components;
    exports dk.sdu.mmmi.cbse.common.services;
    exports dk.sdu.mmmi.cbse.common.events;
    exports dk.sdu.mmmi.cbse.common.utils;
    exports dk.sdu.mmmi.cbse.common;

    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    uses dk.sdu.mmmi.cbse.common.services.ILateUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IUIUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
}