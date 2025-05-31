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
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating Enemies.
 * Integrates with IDifficultyService for dynamic spawn rates and enemy distribution.
 */
public class EnemyFactory implements IEnemySPI {
    private static final Logger LOGGER = Logger.getLogger(EnemyFactory.class.getName());
    private final Random random = new Random();
    private final EnemyRegistry enemyRegistry;
    private IDifficultyService difficultyService;

    // Fallback configuration when difficulty service unavailable
    private static final int FALLBACK_MAX_ENEMIES = 3;
    private static final float FALLBACK_SPAWN_PROBABILITY = 0.004f;
    private static final float FALLBACK_TURRET_CHANCE = 0.2f; // 20% turrets

    // Spawn position configuration
    private static final float EDGE_SPAWN_MARGIN = 60.0f;   // Outside screen for HUNTERS
    private static final float BOUNDARY_MARGIN = 120.0f;    // Inside screen for TURRETS

    // Difficulty-based turret spawning
    private static final float MIN_TURRET_CHANCE = 0.1f;    // 10% at difficulty 0.0
    private static final float MAX_TURRET_CHANCE = 0.5f;    // 50% at difficulty 2.0+

    public EnemyFactory() {
        this.enemyRegistry = EnemyRegistry.getInstance();
        this.difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);

        if (difficultyService == null) {
            LOGGER.log(Level.WARNING, "IDifficultyService not available - using fallback enemy spawning");
        }

        LOGGER.log(Level.INFO, "EnemyFactory initialized with difficulty scaling support");
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
        // Refresh difficulty service if not available
        if (difficultyService == null) {
            difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
        }

        int currentEnemies = countCurrentEnemies(world);
        int maxEnemies = getMaxEnemyCount();
        float spawnProbability = getSpawnProbability();

        if (currentEnemies < maxEnemies && random.nextFloat() < spawnProbability) {
            EnemyType type = selectEnemyType();

            Entity enemy = createEnemy(type, gameData, world);
            world.addEntity(enemy);

            LOGGER.log(Level.INFO, "Spawned {0} enemy ({1}/{2} enemies) at difficulty {3}",
                    new Object[]{type, currentEnemies + 1, maxEnemies, getCurrentDifficulty()});
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

        // Apply difficulty-based firing rate modifier
        float baseProbability = enemyComp.getFiringProbability();
        float difficultyModifier = getDifficultyFiringRateModifier();
        float adjustedProbability = baseProbability * difficultyModifier;

        switch (enemyComp.getType()) {
            case HUNTER:
                float proximityBonus = Math.max(0, 1.0f - (distance / enemyComp.getFireDistance()));
                return random.nextFloat() < (adjustedProbability + proximityBonus * 0.008f);

            case TURRET:
                return random.nextFloat() < adjustedProbability;

            default:
                return false;
        }
    }

    /**
     * Get maximum enemy count based on difficulty
     */
    private int getMaxEnemyCount() {
        if (difficultyService != null) {
            return difficultyService.getMaxEnemyCount();
        }
        return FALLBACK_MAX_ENEMIES;
    }

    /**
     * Get spawn probability with difficulty scaling
     */
    private float getSpawnProbability() {
        if (difficultyService != null) {
            float baseRate = 0.004f; // Base spawn rate
            float multiplier = difficultyService.getEnemySpawnMultiplier();
            return baseRate * multiplier;
        }
        return FALLBACK_SPAWN_PROBABILITY;
    }

    /**
     * Get current difficulty level for logging
     */
    private float getCurrentDifficulty() {
        if (difficultyService != null) {
            return difficultyService.getCurrentDifficulty();
        }
        return 0.0f;
    }

    /**
     * Get difficulty-based firing rate modifier
     */
    private float getDifficultyFiringRateModifier() {
        if (difficultyService != null) {
            // Invert the firing rate multiplier: higher difficulty = more frequent firing
            // DifficultyService returns intervals, we want frequency
            float intervalMultiplier = difficultyService.getHunterFiringRateMultiplier();
            return 1.0f / Math.max(0.1f, intervalMultiplier);
        }
        return 1.0f;
    }

    /**
     * Select enemy type based on difficulty scaling
     */
    private EnemyType selectEnemyType() {
        float turretChance = calculateTurretChance();

        if (random.nextFloat() < turretChance) {
            LOGGER.log(Level.FINE, "Selected TURRET (chance: {0})", turretChance);
            return EnemyType.TURRET;
        } else {
            LOGGER.log(Level.FINE, "Selected HUNTER (turret chance was: {0})", turretChance);
            return EnemyType.HUNTER;
        }
    }

    /**
     * Calculate turret spawn chance based on difficulty
     */
    private float calculateTurretChance() {
        if (difficultyService != null) {
            float difficulty = difficultyService.getCurrentDifficulty();
            // Linear scaling from MIN_TURRET_CHANCE to MAX_TURRET_CHANCE over difficulty 0.0 to 2.0
            float progress = Math.min(difficulty / 2.0f, 1.0f);
            float turretChance = MIN_TURRET_CHANCE + (progress * (MAX_TURRET_CHANCE - MIN_TURRET_CHANCE));

            LOGGER.log(Level.FINEST, "Difficulty {0} -> Turret chance: {1}",
                    new Object[]{difficulty, turretChance});

            return turretChance;
        }

        return FALLBACK_TURRET_CHANCE;
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