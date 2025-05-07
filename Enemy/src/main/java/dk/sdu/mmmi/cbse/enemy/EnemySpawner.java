package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.enemy.EnemyBehavior;
import dk.sdu.mmmi.cbse.common.enemy.EnemyProperties;
import dk.sdu.mmmi.cbse.common.enemy.IEnemyFactory;
import dk.sdu.mmmi.cbse.common.enemy.IEnemySpawner;

import java.util.Random;
import java.util.ServiceLoader;

public class EnemySpawner implements IEnemySpawner {
    private int maxEnemies = 5;
    private float spawnInterval = 300;
    private float difficultyMultiplier = 1.0f;
    private float spawnTimer = 0;
    private final Random rnd = new Random();
    private final IEnemyFactory enemyFactory;

    public EnemySpawner() {
        // Load EnemyFactory in constructor using ServiceLoader
        this.enemyFactory = ServiceLoader.load(IEnemyFactory.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IEnemyFactory implementation found"));
    }

    @Override
    public void spawnEnemies(GameData gameData, World world) {
        // Count current enemies using tag component
        long enemyCount = world.getEntities().stream()
                .filter(e -> {
                    TagComponent tc = e.getComponent(TagComponent.class);
                    return tc != null && tc.hasTag(TagComponent.TAG_ENEMY);
                })
                .count();

        if (enemyCount >= maxEnemies) {
            return;
        }

        if (spawnTimer <= 0) {
            EnemyBehavior behavior = chooseEnemyBehavior();
            EnemyProperties properties = createPropertiesForBehavior(behavior);
            applyDifficultyMultiplier(properties);
            world.addEntity(enemyFactory.createEnemy(gameData, behavior, properties));
            spawnTimer = spawnInterval;
        } else {
            spawnTimer--;
        }
    }

    @Override
    public void setSpawnConfig(int maxEnemies, float spawnInterval, float difficultyMultiplier) {
        this.maxEnemies = maxEnemies;
        this.spawnInterval = spawnInterval;
        this.difficultyMultiplier = difficultyMultiplier;
    }

    private EnemyBehavior chooseEnemyBehavior() {
        float diff = difficultyMultiplier;
        if (diff < 1.5f) {
            // Early game: mostly patrollers
            return rnd.nextFloat() < 0.7f ? EnemyBehavior.PATROL : EnemyBehavior.AGGRESSIVE;
        } else if (diff < 2.5f) {
            // Mid game: mix of types
            float roll = rnd.nextFloat();
            if (roll < 0.4f) return EnemyBehavior.PATROL;
            if (roll < 0.7f) return EnemyBehavior.AGGRESSIVE;
            return EnemyBehavior.SNIPER;
        } else {
            // Late game: mostly aggressive and snipers
            return rnd.nextFloat() < 0.6f ? EnemyBehavior.AGGRESSIVE : EnemyBehavior.SNIPER;
        }
    }

    private void applyDifficultyMultiplier(EnemyProperties properties) {
        properties.setHealth(properties.getHealth() * difficultyMultiplier);
        properties.setDamage(properties.getDamage() * difficultyMultiplier);
        properties.setScoreValue((int)(properties.getScoreValue() * difficultyMultiplier));
    }

    private EnemyProperties createPropertiesForBehavior(EnemyBehavior behavior) {
        EnemyProperties props = new EnemyProperties();
        switch (behavior) {
            case PATROL:
                props.setHealth(50);
                props.setDamage(10);
                props.setSpeed(1.0f);
                props.setShootingRange(150);
                props.setScoreValue(100);
                props.setDetectionRange(200);
                break;
            case AGGRESSIVE:
                props.setHealth(75);
                props.setDamage(15);
                props.setSpeed(2.0f);
                props.setShootingRange(100);
                props.setScoreValue(150);
                props.setDetectionRange(300);
                break;
            case DEFENSIVE:
                props.setHealth(60);
                props.setDamage(12);
                props.setSpeed(1.5f);
                props.setShootingRange(200);
                props.setScoreValue(125);
                props.setDetectionRange(250);
                break;
            case SNIPER:
                props.setHealth(40);
                props.setDamage(20);
                props.setSpeed(0.5f);
                props.setShootingRange(400);
                props.setScoreValue(200);
                props.setDetectionRange(500);
                break;
        }
        return props;
    }
}