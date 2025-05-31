import dk.sdu.mmmi.cbse.AsteroidFactory;
import dk.sdu.mmmi.cbse.AsteroidPlugin;

module Asteroid {
    requires java.logging;
    requires Common;
    requires CommonAsteroid;
    requires CommonCollision;
    requires javafx.graphics;
    requires Core;
    requires CommonPlayer;
    requires CommonPhysics;
    requires CommonDifficulty;

    uses dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    uses dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;

    exports dk.sdu.mmmi.cbse;

    provides dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI
            with AsteroidFactory;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with AsteroidPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.AsteroidSystem,
                    dk.sdu.mmmi.cbse.AsteroidSpawningSystem;
}