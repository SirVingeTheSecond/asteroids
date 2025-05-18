module Core {
    uses dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
    uses dk.sdu.mmmi.cbse.common.services.IProcessingService;
    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    requires Common;
    requires java.logging;
    requires javafx.graphics;
    exports dk.sdu.mmmi.cbse.core;
    exports dk.sdu.mmmi.cbse.core.input;
    exports dk.sdu.mmmi.cbse.core.utils;
}