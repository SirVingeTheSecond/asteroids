import dk.sdu.mmmi.cbse.PlayerPlugin;
import dk.sdu.mmmi.cbse.PlayerSystem;

module Player {
    requires Common;
    requires CommonPlayer;
    requires CommonWeapon;
    requires CommonCollision;
    requires CommonPhysics;
    requires Core;
    requires java.logging;
    requires CommonMovement;
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    uses dk.sdu.mmmi.cbse.common.services.IScoreSPI;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
    uses dk.sdu.mmmi.cbse.commonmovement.IMovementSPI;

    exports dk.sdu.mmmi.cbse;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with PlayerPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with PlayerSystem;
}