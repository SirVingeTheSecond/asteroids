module Tests {
    uses dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
    uses dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
    uses dk.sdu.mmmi.cbse.commonmovement.IMovementSPI;
    uses dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
    uses dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
    uses dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;

    requires javafx.graphics;
    requires java.logging;
    requires spring.context;
    requires org.junit.jupiter.api;
    requires org.junit.platform.suite.api;
    requires org.mockito;
    requires org.mockito.junit.jupiter;

    requires Common;
    requires CommonPhysics;
    requires CommonPlayer;
    requires CommonWeapon;
    requires CommonCollision;
    requires CommonAsteroid;
    requires CommonBullet;
    requires CommonEnemy;
    requires Collision;
    requires Player;
    requires Physics;
    requires Core;
    requires CommonDifficulty;
    requires Enemy;
    requires CommonMovement;
    requires Weapon;

    opens dk.sdu.mmmi.cbse.tests.components to org.junit.platform.commons;
    opens dk.sdu.mmmi.cbse.tests.contracts to org.junit.platform.commons;
    opens dk.sdu.mmmi.cbse.tests.services to org.junit.platform.commons;
    opens dk.sdu.mmmi.cbse.tests.systems to org.junit.platform.commons, org.mockito;
    opens dk.sdu.mmmi.cbse.tests.integration to org.junit.platform.commons, org.mockito;
}