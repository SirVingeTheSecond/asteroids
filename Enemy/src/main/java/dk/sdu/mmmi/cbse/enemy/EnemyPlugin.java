// EnemyPlugin.java (updated)
package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.enemy.EnemyBehavior;
import dk.sdu.mmmi.cbse.common.enemy.EnemyProperties;
import dk.sdu.mmmi.cbse.common.enemy.IEnemyFactory;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;
import dk.sdu.mmmi.cbse.common.services.IGamePluginService;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

public class EnemyPlugin implements IEnemyFactory, IGamePluginService {
    private final List<Entity> enemies = new ArrayList<>();
    private final IGameEventService eventService;
    private final EnemyFactory enemyFactory;

    public EnemyPlugin() {
        this.eventService = ServiceLoader.load(IGameEventService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IGameEventService implementation found"));

        this.enemyFactory = new EnemyFactory();
    }

    @Override
    public void start(GameData gameData, World world) {
        // Create different types of enemies
        Entity patroller = createEnemy(gameData, EnemyBehavior.PATROL, createPatrollerProperties());
        world.addEntity(patroller);
        enemies.add(patroller);

        Entity aggressor = createEnemy(gameData, EnemyBehavior.AGGRESSIVE, createAggressorProperties());
        world.addEntity(aggressor);
        enemies.add(aggressor);

        Entity sniper = createEnemy(gameData, EnemyBehavior.SNIPER, createSniperProperties());
        world.addEntity(sniper);
        enemies.add(sniper);
    }

    @Override
    public void stop(GameData gameData, World world) {
        for (Entity enemy : enemies) {
            world.removeEntity(enemy);
        }
        enemies.clear();
    }

    @Override
    public Entity createEnemy(GameData gameData, EnemyBehavior behavior, EnemyProperties properties) {
        return enemyFactory.createEnemy(gameData, behavior, properties);
    }

    private EnemyProperties createPatrollerProperties() {
        EnemyProperties props = new EnemyProperties();
        props.setHealth(50);
        props.setDamage(10);
        props.setSpeed(1.0f);
        props.setShootingRange(150);
        props.setScoreValue(100);
        props.setDetectionRange(200);
        return props;
    }

    private EnemyProperties createAggressorProperties() {
        EnemyProperties props = new EnemyProperties();
        props.setHealth(75);
        props.setDamage(15);
        props.setSpeed(2.0f);
        props.setShootingRange(100);
        props.setScoreValue(150);
        props.setDetectionRange(300);
        return props;
    }

    private EnemyProperties createSniperProperties() {
        EnemyProperties props = new EnemyProperties();
        props.setHealth(40);
        props.setDamage(20);
        props.setSpeed(0.5f);
        props.setShootingRange(400);
        props.setScoreValue(200);
        props.setDetectionRange(500);
        return props;
    }
}