module Enemy {
    uses dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    requires Common;
    requires CommonEnemy;
    requires CommonWeapon;
    requires CommonCollision;

    requires java.logging;
    requires javafx.graphics;
    requires Core;
    requires CommonPhysics;

    exports dk.sdu.mmmi.cbse.enemy;

    provides dk.sdu.mmmi.cbse.commonenemy.IEnemySPI
            with dk.sdu.mmmi.cbse.enemy.EnemyFactory;

    provides dk.sdu.mmmi.cbse.common.services.IUpdate
            with dk.sdu.mmmi.cbse.enemy.EnemySystem;

    provides dk.sdu.mmmi.cbse.common.services.IPluginService
            with dk.sdu.mmmi.cbse.enemy.EnemyPlugin;
}