module Core {
    requires Common;
    requires java.logging;
    requires javafx.graphics;

    // Spring Framework requirements
    requires spring.context;
    requires spring.core;
    requires spring.beans;

    uses dk.sdu.mmmi.cbse.common.services.IUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IFixedUpdate;
    uses dk.sdu.mmmi.cbse.common.services.ILateUpdate;
    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    uses dk.sdu.mmmi.cbse.common.services.IRenderingContext;

    provides dk.sdu.mmmi.cbse.common.services.IEventService
            with dk.sdu.mmmi.cbse.core.events.EventService;

    exports dk.sdu.mmmi.cbse.core;
    exports dk.sdu.mmmi.cbse.core.input;
    exports dk.sdu.mmmi.cbse.core.utils;
    exports dk.sdu.mmmi.cbse.core.events;
    exports dk.sdu.mmmi.cbse.core.config;
    exports dk.sdu.mmmi.cbse.core.services;

    // Open packages to Spring Framework for reflection
    opens dk.sdu.mmmi.cbse.core.config to spring.core, spring.beans, spring.context;
    opens dk.sdu.mmmi.cbse.core.services to spring.core, spring.beans, spring.context;
    opens dk.sdu.mmmi.cbse.core to spring.core, spring.beans, spring.context;
}