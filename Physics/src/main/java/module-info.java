module Physics {
    requires Common;
    requires CommonPhysics;
    requires CommonCollision;
    requires java.logging;
    requires Core;

    uses dk.sdu.mmmi.cbse.commoncollision.ICollisionSPI;

    exports dk.sdu.mmmi.cbse.physics;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.physics.PhysicsSystem;
    provides dk.sdu.mmmi.cbse.common.services.IFixedUpdate
            with dk.sdu.mmmi.cbse.physics.PhysicsSystem;
    provides dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI
            with dk.sdu.mmmi.cbse.physics.PhysicsService;
}