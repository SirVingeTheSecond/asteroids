module Common {
    requires java.logging;
    exports dk.sdu.mmmi.cbse.common.data;
    exports dk.sdu.mmmi.cbse.common.components;
    exports dk.sdu.mmmi.cbse.common.services;
    exports dk.sdu.mmmi.cbse.common.events;
    exports dk.sdu.mmmi.cbse.common.collision;
    exports dk.sdu.mmmi.cbse.common.utils;
    exports dk.sdu.mmmi.cbse.common.bullet;
    exports dk.sdu.mmmi.cbse.common.enemy;
    exports dk.sdu.mmmi.cbse.common.asteroids;
    exports dk.sdu.mmmi.cbse.common;

    uses dk.sdu.mmmi.cbse.common.services.IPluginService;
    uses dk.sdu.mmmi.cbse.common.services.IProcessingService;
    uses dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
    uses dk.sdu.mmmi.cbse.common.services.IGameEventService;
    uses dk.sdu.mmmi.cbse.common.bullet.BulletSPI;
    uses dk.sdu.mmmi.cbse.common.enemy.IEnemyFactory;
    uses dk.sdu.mmmi.cbse.common.enemy.IEnemySpawner;
    uses dk.sdu.mmmi.cbse.common.asteroids.IAsteroidSplitter;
}