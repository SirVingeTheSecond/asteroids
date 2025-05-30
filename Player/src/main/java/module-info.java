module Player {
    requires Common;
    requires javafx.graphics;
    requires CommonPlayer;
    requires CommonWeapon;
    requires CommonCollision;
    requires CommonPhysics;
    requires Core;
    requires java.logging;

    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    uses dk.sdu.mmmi.cbse.common.services.IScoreSPI;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;

    exports dk.sdu.mmmi.cbse.player;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.player.PlayerPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.player.PlayerSystem;
}