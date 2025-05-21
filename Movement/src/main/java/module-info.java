import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IUpdate;

module Movement {
    requires Common;
    requires java.logging;
    requires Core;

    exports dk.sdu.mmmi.cbse.movementsystem;

    provides IUpdate with
            dk.sdu.mmmi.cbse.movementsystem.MovementSystem;

    provides ILateUpdate with
            dk.sdu.mmmi.cbse.movementsystem.ScreenWrapSystem;
}