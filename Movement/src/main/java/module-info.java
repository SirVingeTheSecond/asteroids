module Movement {
    requires Common;
    requires java.logging;
    requires Core;

    exports dk.sdu.mmmi.cbse.movementsystem;

    provides dk.sdu.mmmi.cbse.common.services.IProcessingService with
            dk.sdu.mmmi.cbse.movementsystem.MovementSystem;

    provides dk.sdu.mmmi.cbse.common.services.IPostProcessingService with
            dk.sdu.mmmi.cbse.movementsystem.ScreenWrapSystem;
}