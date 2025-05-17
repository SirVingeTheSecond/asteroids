package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponType;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating enemy entities.
 * Implements the IEnemySPI service interface.
 */
public class EnemyFactory implements IEnemySPI {
    private static final Logger LOGGER = Logger.getLogger(EnemyFactory.class.getName());
    private final Random random = new Random();
    private final EnemyRegistry enemyRegistry;

    // Enemy counts for spawning
    private static final int MAX_ENEMIES = 5;
    private static final float SPAWN_PROBABILITY = 0.01f; // 1% chance per frame

    /**
     * Create a new enemy factory
     */
    public EnemyFactory() {
        this.enemyRegistry = EnemyRegistry.getInstance();
        LOGGER.log(Level.INFO, "EnemyFactory initialized");
    }

    @Override
    public Entity createEnemy(EnemyType type, GameData gameData, World world) {
        LOGGER.log(Level.INFO, "Creating enemy of type: {0}", type);

        // Get enemy configuration from registry
        EnemyConfig config = enemyRegistry.getEnemyConfigByType(type);

        // Generate random position at edge of screen
        float x, y;
        int side = random.nextInt(4);

        switch (side) {
            case 0: // Top
                x = random.nextFloat() * gameData.getDisplayWidth();
                y = 10;
                break;
            case 1: // Right
                x = gameData.getDisplayWidth() - 10;
                y = random.nextFloat() * gameData.getDisplayHeight();
                break;
            case 2: // Bottom
                x = random.nextFloat() * gameData.getDisplayWidth();
                y = gameData.getDisplayHeight() - 10;
                break;
            default: // Left
                x = 10;
                y = random.nextFloat() * gameData.getDisplayHeight();
                break;
        }

        // Create the enemy entity using the configuration
        Entity enemy = EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(x, y)
                .withRotation(random.nextFloat() * 360)
                .withRadius(15)
                .withShape(-10, -10, 10, -10, 10, 10, -10, 10) // Square shape
                .with(createEnemyComponent(config))
                .with(createMovementComponent(config))
                .with(createRendererComponent(type))
                .with(createColliderComponent())
                .with(createWeaponComponent(config))
                .build();

        LOGGER.log(Level.INFO, "Enemy created with ID: {0}", enemy.getID());
        return enemy;
    }

    @Override
    public void spawnEnemies(GameData gameData, World world) {
        // Count current enemies
        int currentEnemies = 0;
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.ENEMY)) {
                currentEnemies++;
            }
        }

        // Spawn new enemies if below maximum
        if (currentEnemies < MAX_ENEMIES && random.nextFloat() < SPAWN_PROBABILITY) {
            // Select a random enemy type
            EnemyType type = EnemyType.values()[random.nextInt(EnemyType.values().length)];

            Entity enemy = createEnemy(type, gameData, world);
            world.addEntity(enemy);

            LOGGER.log(Level.INFO, "Spawned new enemy of type {0}, now {1} enemies",
                    new Object[]{type, currentEnemies + 1});
        }
    }

    @Override
    public boolean shouldFire(Entity enemy, float[] playerPosition) {
        // Skip if entity doesn't have required components
        if (!enemy.hasComponent(EnemyComponent.class) ||
                !enemy.hasComponent(TransformComponent.class) ||
                !enemy.hasComponent(WeaponComponent.class)) {
            return false;
        }

        EnemyComponent enemyComponent = enemy.getComponent(EnemyComponent.class);
        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        WeaponComponent weapon = enemy.getComponent(WeaponComponent.class);

        // Skip if enemy can't fire or weapon is on cooldown
        if (!enemyComponent.canFire() || !weapon.canFire()) {
            return false;
        }

        // Calculate distance to player
        float dx = playerPosition[0] - transform.getX();
        float dy = playerPosition[1] - transform.getY();
        float distanceSquared = dx * dx + dy * dy;

        // Check if player is within firing range
        if (distanceSquared <= enemyComponent.getFireDistance() * enemyComponent.getFireDistance()) {
            // Random chance to fire based on enemy type
            return random.nextFloat() < enemyComponent.getFiringProbability();
        }

        return false;
    }

    /**
     * Create enemy component based on configuration
     */
    private EnemyComponent createEnemyComponent(EnemyConfig config) {
        EnemyComponent component = new EnemyComponent(config.getType());
        component.setHealth(config.getHealth());
        component.setScoreValue(config.getScoreValue());
        component.setFiringProbability(config.getFiringProbability());
        component.setFireDistance(config.getFireDistance());
        return component;
    }

    /**
     * Create movement component based on configuration
     */
    private MovementComponent createMovementComponent(EnemyConfig config) {
        MovementComponent movement = new MovementComponent();

        // Set movement pattern based on enemy type
        switch (config.getType()) {
            case HUNTER:
                movement.setPattern(MovementComponent.MovementPattern.LINEAR);
                break;
            case TURRET:
                movement.setPattern(MovementComponent.MovementPattern.LINEAR);
                break;
            default: // BASIC
                movement.setPattern(MovementComponent.MovementPattern.RANDOM);
                break;
        }

        movement.setSpeed(config.getSpeed());
        movement.setRotationSpeed(config.getRotationSpeed());
        return movement;
    }

    /**
     * Create renderer component for enemy visualization
     */
    private RendererComponent createRendererComponent(EnemyType type) {
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(300); // Enemies above bullets but below player

        // Configure visuals based on type
        switch (type) {
            case HUNTER:
                renderer.setStrokeColor(Color.RED);
                renderer.setFillColor(Color.DARKRED);
                break;
            case TURRET:
                renderer.setStrokeColor(Color.PURPLE);
                renderer.setFillColor(Color.DARKVIOLET);
                break;
            default: // BASIC
                renderer.setStrokeColor(Color.ORANGE);
                renderer.setFillColor(Color.DARKORANGE);
                break;
        }

        renderer.setStrokeWidth(2.0f);
        return renderer;
    }

    /**
     * Create collision component for enemy
     */
    private ColliderComponent createColliderComponent() {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.ENEMY);
        return collider;
    }

    /**
     * Create weapon component based on enemy configuration
     */
    private WeaponComponent createWeaponComponent(EnemyConfig config) {
        WeaponComponent weapon = new WeaponComponent();

        // Configure based on enemy type
        switch (config.getType()) {
            case HUNTER:
                weapon.setFiringPattern(WeaponType.FiringPattern.BURST);
                weapon.setBurstCount(3);
                weapon.setDamage(8.0f);
                weapon.setCooldownTime(90); // Longer cooldown
                break;
            case TURRET:
                weapon.setFiringPattern(WeaponType.FiringPattern.SHOTGUN);
                weapon.setShotCount(3);
                weapon.setSpreadAngle(30.0f);
                weapon.setDamage(5.0f);
                weapon.setCooldownTime(120); // Long cooldown
                break;
            default: // BASIC
                weapon.setFiringPattern(WeaponType.FiringPattern.AUTOMATIC);
                weapon.setDamage(10.0f);
                weapon.setCooldownTime(60);
                break;
        }

        weapon.setProjectileSpeed(200.0f);
        weapon.setBulletType(config.getBulletType());

        return weapon;
    }
}