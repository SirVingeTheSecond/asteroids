package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.*;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating enemy entities.
 */
public class EnemyFactory implements IEnemySPI {
    private static final Logger LOGGER = Logger.getLogger(EnemyFactory.class.getName());
    private final Random random = new Random();
    private final EnemyRegistry enemyRegistry;

    // Enemy counts for spawning
    private static final int MAX_ENEMIES = 6; // Increased for more action
    private static final float SPAWN_PROBABILITY = 0.008f; // Slightly increased spawn rate

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

        Entity enemy = EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(x, y)
                .withRotation(random.nextFloat() * 360)
                .withRadius(15)
                .withShape(createEnemyShape(type)) // Different shapes for different types
                .with(createEnemyComponent(config))
                .with(createMovementComponent(config))
                .with(createRendererComponent(type))
                .with(createColliderComponent())
                .with(createEnemyCollisionResponse()) // Fixed collision response
                .with(createWeaponComponent(config))
                .build();

        LOGGER.log(Level.INFO, "Enemy created with ID: {0} of type: {1}",
                new Object[]{enemy.getID(), type});
        return enemy;
    }

    /**
     * Create enemy-specific shapes
     */
    private double[] createEnemyShape(EnemyType type) {
        switch (type) {
            case HUNTER:
                // Triangle shape pointing forward (aggressive looking)
                return new double[]{-8, -8, 12, 0, -8, 8};
            case TURRET:
                // Octagonal shape (more defensive looking)
                return new double[]{-8, -4, -4, -8, 4, -8, 8, -4, 8, 4, 4, 8, -4, 8, -8, 4};
            default: // BASIC
                // Square shape
                return new double[]{-10, -10, 10, -10, 10, 10, -10, 10};
        }
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
            // Prefer HUNTER and TURRET types over BASIC (75% chance for advanced types)
            EnemyType type;
            float typeRoll = random.nextFloat();
            if (typeRoll < 0.4f) {
                type = EnemyType.HUNTER;
            } else if (typeRoll < 0.75f) {
                type = EnemyType.TURRET;
            } else {
                type = EnemyType.BASIC;
            }

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
            // Different firing strategies based on enemy type
            switch (enemyComponent.getType()) {
                case HUNTER:
                    // Hunters fire more aggressively when close
                    float closenessBonus = Math.max(0, 1.0f - (distanceSquared / (enemyComponent.getFireDistance() * enemyComponent.getFireDistance())));
                    return random.nextFloat() < (enemyComponent.getFiringProbability() + closenessBonus * 0.01f);

                case TURRET:
                    // Turrets fire in predictable bursts
                    return random.nextFloat() < enemyComponent.getFiringProbability();

                default:
                    return random.nextFloat() < enemyComponent.getFiringProbability();
            }
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
                movement.setSpeed(config.getSpeed());
                movement.setRotationSpeed(config.getRotationSpeed() * 2.0f);
                break;
            case TURRET:
                movement.setPattern(MovementComponent.MovementPattern.LINEAR);
                movement.setSpeed(config.getSpeed() * 0.5f);
                movement.setRotationSpeed(config.getRotationSpeed());
                break;
            default: // BASIC
                movement.setPattern(MovementComponent.MovementPattern.RANDOM);
                movement.setSpeed(config.getSpeed());
                movement.setRotationSpeed(config.getRotationSpeed());
                break;
        }

        return movement;
    }

    /**
     * Create renderer component for enemy visualization
     */
    private RendererComponent createRendererComponent(EnemyType type) {
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(RenderLayer.ENEMY);

        switch (type) {
            case HUNTER:
                renderer.setStrokeColor(Color.CRIMSON);
                renderer.setFillColor(Color.DARKRED);
                renderer.setStrokeWidth(2.5f);
                break;
            case TURRET:
                renderer.setStrokeColor(Color.BLUEVIOLET);
                renderer.setFillColor(Color.INDIGO);
                renderer.setStrokeWidth(2.0f);
                break;
            default: // BASIC
                renderer.setStrokeColor(Color.ORANGE);
                renderer.setFillColor(Color.DARKORANGE);
                renderer.setStrokeWidth(2.0f);
                break;
        }

        renderer.setFilled(true);
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
     * Create collision response component for enemies - FIXED
     */
    private CollisionResponseComponent createEnemyCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        // Enemies damage player on collision but are also destroyed
        response.addHandler(EntityType.PLAYER, (self, player, context) -> {
            CollisionResult result = CollisionHandlers.handlePlayerDamage(player, 1, context);
            result.addRemoval(self); // Enemy is destroyed when hitting player
            return result;
        });

        // Enemies ignore other enemies (no friendly collisions)
        response.addHandler(EntityType.ENEMY, CollisionHandlers.IGNORE_COLLISION_HANDLER);

        // Enemies ignore asteroids (pass through them)
        response.addHandler(EntityType.OBSTACLE, CollisionHandlers.IGNORE_COLLISION_HANDLER);

        return response;
    }

    /**
     * Create weapon component based on enemy configuration
     */
    private WeaponComponent createWeaponComponent(EnemyConfig config) {
        WeaponComponent weapon = new WeaponComponent();

        switch (config.getType()) {
            case HUNTER:
                weapon.setFiringPattern(Weapon.FiringPattern.BURST);
                weapon.setBurstCount(3);
                weapon.setDamage(8.0f);
                weapon.setCooldownTime(1.2f);
                weapon.setBurstDelay(0.04f);
                break;
            case TURRET:
                weapon.setFiringPattern(Weapon.FiringPattern.SHOTGUN);
                weapon.setShotCount(3);
                weapon.setSpreadAngle(25.0f);
                weapon.setDamage(6.0f);
                weapon.setCooldownTime(1.8f);
                break;
            default: // BASIC
                weapon.setFiringPattern(Weapon.FiringPattern.AUTOMATIC);
                weapon.setDamage(10.0f);
                weapon.setCooldownTime(1.0f);
                break;
        }

        weapon.setProjectileSpeed(220.0f);
        weapon.setBulletType(config.getBulletType());
        return weapon;
    }
}