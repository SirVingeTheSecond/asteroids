module CommonCollision {
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    requires Common;
    requires java.logging;
    requires CommonAsteroid;
    requires CommonBullet;
    requires CommonEnemy;
    requires CommonPlayer;
    requires CommonPhysics;

    exports dk.sdu.mmmi.cbse.commoncollision;
}