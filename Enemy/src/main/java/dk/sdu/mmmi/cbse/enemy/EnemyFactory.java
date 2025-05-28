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
 * Factory for creating Enemies with different behaviors.
 */
public class EnemyFactory implements IEnemySPI {
    private static final Logger LOGGER = Logger.getLogger(EnemyFactory.class.getName());
    private final Random random = new Random();
    private final EnemyRegistry enemyRegistry;

    // Spawn config
    private static final int MAX_ENEMIES = 5;
    private static final float SPAWN_PROBABILITY = 0.006f;
    private static final float EDGE_SPAWN_MARGIN = 60.0f;   // Outside screen for HUNTERS
    private static final float BOUNDARY_MARGIN = 120.0f;    // Inside screen for TURRETS

    public EnemyFactory() {
        this.enemyRegistry = EnemyRegistry.getInstance();
        LOGGER.log(Level.INFO, "EnemyFactory initialized for HUNTER and TURRET enemies");
    }

    @Override
    public Entity createEnemy(EnemyType type, GameData gameData, World world) {
        EnemyConfig config = enemyRegistry.getEnemyConfigByType(type);
        float[] position = generateSpawnPosition(type, gameData);

        Entity enemy = EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(position[0], position[1])
                .withRotation(random.nextFloat() * 360)
                .withRadius(15)
                .withShape(createEnemyShape(type))
                .with(createEnemyComponent(config))
                .with(createMovementComponent(config))
                .with(createRendererComponent(type))
                .with(createColliderComponent(type))
                .with(createCollisionResponse())
                .with(createWeaponComponent(config))
                .build();

        LOGGER.log(Level.INFO, "Created {0} enemy at ({1}, {2}) with {3} collision layer",
                new Object[]{type, position[0], position[1],
                        type == EnemyType.HUNTER ? "INVINCIBLE" : "ENEMY"});
        return enemy;
    }

    @Override
    public void spawnEnemies(GameData gameData, World world) {
        int currentEnemies = countCurrentEnemies(world);

        if (currentEnemies < MAX_ENEMIES && random.nextFloat() < SPAWN_PROBABILITY) {
            // 60% HUNTER, 40% TURRET
            EnemyType type = random.nextFloat() < 0.6f ? EnemyType.HUNTER : EnemyType.TURRET;

            Entity enemy = createEnemy(type, gameData, world);
            world.addEntity(enemy);

            LOGGER.log(Level.INFO, "Spawned {0} enemy ({1} total enemies)",
                    new Object[]{type, currentEnemies + 1});
        }
    }

    @Override
    public boolean shouldFire(Entity enemy, float[] playerPosition) {
        EnemyComponent enemyComp = enemy.getComponent(EnemyComponent.class);
        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        WeaponComponent weapon = enemy.getComponent(WeaponComponent.class);

        if (enemyComp == null || transform == null || weapon == null || !weapon.canFire()) {
            return false;
        }

        // Calculate distance to player
        float dx = playerPosition[0] - transform.getX();
        float dy = playerPosition[1] - transform.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > enemyComp.getFireDistance()) {
            return false;
        }

        switch (enemyComp.getType()) {
            case HUNTER:
                float proximityBonus = Math.max(0, 1.0f - (distance / enemyComp.getFireDistance()));
                return random.nextFloat() < (enemyComp.getFiringProbability() + proximityBonus * 0.008f);

            case TURRET:
                return random.nextFloat() < enemyComp.getFiringProbability();

            default:
                return false;
        }
    }

    /**
     * Generate spawn position based on enemy type
     */
    private float[] generateSpawnPosition(EnemyType type, GameData gameData) {
        switch (type) {
            case TURRET:
                // Spawn within boundaries as defensive installations
                float x = BOUNDARY_MARGIN + random.nextFloat() *
                        (gameData.getDisplayWidth() - 2 * BOUNDARY_MARGIN);
                float y = BOUNDARY_MARGIN + random.nextFloat() *
                        (gameData.getDisplayHeight() - 2 * BOUNDARY_MARGIN);
                return new float[]{x, y};

            case HUNTER:
            default:
                // Spawn outside boundaries as approaching threats
                return generateEdgeSpawnPosition(gameData);
        }
    }

    private float[] generateEdgeSpawnPosition(GameData gameData) {
        int side = random.nextInt(4);
        switch (side) {
            case 0: // Top
                return new float[]{
                        random.nextFloat() * gameData.getDisplayWidth(),
                        -EDGE_SPAWN_MARGIN
                };
            case 1: // Right
                return new float[]{
                        gameData.getDisplayWidth() + EDGE_SPAWN_MARGIN,
                        random.nextFloat() * gameData.getDisplayHeight()
                };
            case 2: // Bottom
                return new float[]{
                        random.nextFloat() * gameData.getDisplayWidth(),
                        gameData.getDisplayHeight() + EDGE_SPAWN_MARGIN
                };
            default: // Left
                return new float[]{
                        -EDGE_SPAWN_MARGIN,
                        random.nextFloat() * gameData.getDisplayHeight()
                };
        }
    }

    private double[] createEnemyShape(EnemyType type) {
        switch (type) {
            case HUNTER:
                // triangle
                return new double[]{-8, -8, 12, 0, -8, 8};

            case TURRET:
                // octagonal
                return new double[]{-8, -4, -4, -8, 4, -8, 8, -4, 8, 4, 4, 8, -4, 8, -8, 4};

            default:
                return new double[]{-10, -10, 10, -10, 10, 10, -10, 10};
        }
    }

    private EnemyComponent createEnemyComponent(EnemyConfig config) {
        EnemyComponent component = new EnemyComponent(config.getType());
        component.setHealth(config.getHealth());
        component.setScoreValue(config.getScoreValue());
        component.setFiringProbability(config.getFiringProbability());
        component.setFireDistance(config.getFireDistance());
        return component;
    }

    private MovementComponent createMovementComponent(EnemyConfig config) {
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.LINEAR);
        movement.setSpeed(config.getSpeed());
        movement.setRotationSpeed(config.getRotationSpeed());
        return movement;
    }

    private RendererComponent createRendererComponent(EnemyType type) {
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(RenderLayer.ENEMY);
        renderer.setFilled(true);

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
        }

        return renderer;
    }

    private ColliderComponent createColliderComponent(EnemyType type) {
        ColliderComponent collider = new ColliderComponent();

        switch (type) {
            case HUNTER:
                collider.setLayer(CollisionLayer.INVINCIBLE);
                LOGGER.log(Level.FINE, "HUNTER set to INVINCIBLE layer (ignores boundaries)");
                break;

            case TURRET:
                collider.setLayer(CollisionLayer.ENEMY);
                LOGGER.log(Level.FINE, "TURRET set to ENEMY layer (normal collisions)");
                break;
        }

        return collider;
    }

    private CollisionResponseComponent createCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        response.addHandler(EntityType.PLAYER, (self, player, context) -> {
            CollisionResult result = CollisionHandlers.handlePlayerDamage(player, 1, context);
            result.addRemoval(self);
            return result;
        });

        response.addHandler(EntityType.ENEMY, CollisionHandlers.IGNORE_COLLISION_HANDLER);
        response.addHandler(EntityType.OBSTACLE, CollisionHandlers.IGNORE_COLLISION_HANDLER);

        return response;
    }

    private WeaponComponent createWeaponComponent(EnemyConfig config) {
        WeaponComponent weapon = new WeaponComponent();
        weapon.setBulletType(config.getBulletType());

        switch (config.getType()) {
            case HUNTER:
                weapon.setFiringPattern(Weapon.FiringPattern.BURST);
                weapon.setBurstCount(3);
                weapon.setDamage(1.0f);
                weapon.setCooldownTime(1.5f);
                weapon.setBurstDelay(0.1f);
                weapon.setProjectileSpeed(280.0f);
                break;

            case TURRET:
                weapon.setFiringPattern(Weapon.FiringPattern.SHOTGUN);
                weapon.setShotCount(3);
                weapon.setSpreadAngle(30.0f);
                weapon.setDamage(1.0f);
                weapon.setCooldownTime(2.0f);
                weapon.setProjectileSpeed(250.0f);
                break;
        }

        return weapon;
    }

    private int countCurrentEnemies(World world) {
        int count = 0;
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.ENEMY)) {
                count++;
            }
        }
        return count;
    }
}