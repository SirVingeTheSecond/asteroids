module Asteroid {
    requires java.logging;
    requires Common;
    requires CommonAsteroid;
    requires CommonCollision;
    requires javafx.graphics;
    requires Core;
    requires CommonPlayer;
    requires CommonPhysics;

    uses dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;

    exports dk.sdu.mmmi.cbse.asteroid;

    provides dk.sdu.mmmi.cbse.commonasteroid.IAsteroidSPI
            with dk.sdu.mmmi.cbse.asteroid.AsteroidFactory;
    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.asteroid.AsteroidPlugin;
    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.asteroid.AsteroidSystem;
}