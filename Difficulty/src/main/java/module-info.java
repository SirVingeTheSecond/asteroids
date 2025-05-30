module Difficulty {
    requires Common;
    requires CommonDifficulty;
    requires Core;
    requires java.logging;

    uses dk.sdu.mmmi.cbse.common.services.IEventService;
    uses dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;

    exports dk.sdu.mmmi.cbse.difficulty;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.difficulty.DifficultySystem;
    provides dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService
            with dk.sdu.mmmi.cbse.difficulty.DifficultyService;
}