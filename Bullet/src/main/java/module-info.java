module Bullet {
    requires Core;
    requires Common;
    requires CommonBullet;
    requires CommonCollision;
    requires CommonWeapon;
    requires java.logging;
    requires javafx.graphics;

    exports dk.sdu.mmmi.cbse.bullet;

    provides dk.sdu.mmmi.cbse.commonbullet.IBulletSPI
            with dk.sdu.mmmi.cbse.bullet.BulletFactory;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.bullet.BulletSystem;
}