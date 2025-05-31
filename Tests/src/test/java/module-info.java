module Tests {
    uses dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
    uses dk.sdu.mmmi.cbse.common.services.IEventService;
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
    requires javafx.graphics;
    requires java.logging;
    requires CommonDifficulty;
    requires Enemy;
    requires spring.context;

    exports dk.sdu.mmmi.cbse.tests.utils;

    opens dk.sdu.mmmi.cbse.tests.components to org.junit.platform.commons;
    opens dk.sdu.mmmi.cbse.tests.systems to org.junit.platform.commons, org.mockito;
    opens dk.sdu.mmmi.cbse.tests.contracts to org.junit.platform.commons;
    opens dk.sdu.mmmi.cbse.tests.integration to org.junit.platform.commons, org.mockito;
    opens dk.sdu.mmmi.cbse.tests.performance to org.junit.platform.commons;

    opens dk.sdu.mmmi.cbse.tests.suites to org.junit.platform.commons, org.junit.platform.suite.engine;
}