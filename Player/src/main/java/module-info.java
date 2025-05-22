module Player {
    requires java.logging;
    requires Common;
    requires javafx.graphics;
    requires CommonPlayer;
    requires CommonWeapon;
    requires CommonCollision;
    requires Core;

    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;

    exports dk.sdu.mmmi.cbse.player;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.player.PlayerPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.player.PlayerSystem;
}