module Collision {
    uses dk.sdu.mmmi.cbse.common.services.IEventService;

    requires javafx.graphics;
    requires Common;
    requires java.logging;
    requires CommonCollision;
    requires CommonPlayer;
    requires CommonBullet;
    requires CommonEnemy;
    requires CommonAsteroid;

    exports dk.sdu.mmmi.cbse.collision;

    provides dk.sdu.mmmi.cbse.common.services.ILateUpdate
            with dk.sdu.mmmi.cbse.collision.CollisionSystem;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.collision.CollisionPlugin;

    provides dk.sdu.mmmi.cbse.commoncollision.ICollisionSPI
            with dk.sdu.mmmi.cbse.collision.CollisionSystem;
}