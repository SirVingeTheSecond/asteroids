module Movement {
    requires Core;
    requires Common;
    requires CommonPhysics;
    requires CommonMovement;
    requires java.logging;
    requires CommonEnemy;

    exports dk.sdu.mmmi.cbse.movementsystem;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate with
            dk.sdu.mmmi.cbse.movementsystem.MovementSystem;

    provides dk.sdu.mmmi.cbse.common.services.IFixedUpdate with
            dk.sdu.mmmi.cbse.movementsystem.MovementSystem;

    provides dk.sdu.mmmi.cbse.common.services.ILateUpdate with
            dk.sdu.mmmi.cbse.movementsystem.ScreenWrapSystem;

    provides dk.sdu.mmmi.cbse.commonmovement.IMovementSPI with
            dk.sdu.mmmi.cbse.movementsystem.MovementService;
}