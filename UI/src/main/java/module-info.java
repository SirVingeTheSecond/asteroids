module UI {
    requires Common;
    requires CommonUI;
    requires CommonPlayer;
    requires CommonWeapon;
    requires java.logging;
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.commonui.IUIService;

    exports dk.sdu.mmmi.cbse.ui;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.ui.UISystem;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.ui.UIPlugin;
    provides dk.sdu.mmmi.cbse.commonui.IUIService
            with dk.sdu.mmmi.cbse.ui.UIService;
}