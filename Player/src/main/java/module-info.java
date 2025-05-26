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
    requires javafx.graphics;

    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;

    exports dk.sdu.mmmi.cbse;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with PlayerPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with PlayerSystem;
}