module Movement {
    requires Core;
    requires Common;
    requires java.logging;

    exports dk.sdu.mmmi.cbse.movementsystem;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate with
            dk.sdu.mmmi.cbse.movementsystem.MovementSystem;

    provides dk.sdu.mmmi.cbse.common.services.ILateUpdate with
            dk.sdu.mmmi.cbse.movementsystem.ScreenWrapSystem;
}