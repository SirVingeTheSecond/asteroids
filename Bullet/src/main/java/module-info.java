module Bullet {
    requires java.logging;
    requires Common;
    requires CommonBullet;
    requires CommonCollision;
    requires Core;

    exports dk.sdu.mmmi.cbse.bullet;

    provides dk.sdu.mmmi.cbse.commonbullet.IBulletSPI
            with dk.sdu.mmmi.cbse.bullet.BulletFactory;
}