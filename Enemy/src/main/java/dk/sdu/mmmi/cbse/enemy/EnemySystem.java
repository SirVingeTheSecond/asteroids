package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.commondifficulty.events.DifficultyChangedEvent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System managing Enemy behaviors with difficulty-based scaling.
 * Applies real-time difficulty multipliers to enemy movement and combat.
 */
public class EnemySystem implements IUpdate, IEventListener<DifficultyChangedEvent> {
    private static final Logger LOGGER = Logger.getLogger(EnemySystem.class.getName());

    private IEnemySPI enemySPI;
    private IWeaponSPI weaponSPI;
    private IDifficultyService difficultyService;
    private IEventService eventService;

    // Base enemy configuration (before difficulty scaling)
    private static final float BASE_HUNTER_MIN_DISTANCE = 70.0f;
    private static final float BASE_HUNTER_SPEED = 90.0f;
    private static final float BASE_HUNTER_ROTATION_SPEED = 150.0f;
    private static final float BASE_TURRET_ROTATION_SPEED = 80.0f;

    // Fallback configuration when difficulty service unavailable
    private static final float FALLBACK_HUNTER_SPEED_MULTIPLIER = 0.8f; // Slightly slower
    private static final float FALLBACK_HUNTER_FIRING_MULTIPLIER = 1.2f; // Slightly slower firing

    // Current difficulty multipliers (cached for performance)
    private float hunterSpeedMultiplier = 1.0f;
    private float hunterFiringRateMultiplier = 1.0f;
    private float currentDifficulty = 0.0f;

    public EnemySystem() {
        this.enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        this.difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        initializeDifficultySystem();

        if (enemySPI == null) {
            LOGGER.log(Level.SEVERE, "IEnemySPI not found! Enemy system will not work.");
        } else {
            LOGGER.log(Level.INFO, "EnemySystem initialized with difficulty scaling support");
        }
    }

    /**
     * Initialize difficulty system and subscribe to events
     */
    private void initializeDifficultySystem() {
        if (difficultyService != null) {
            // Initialize with current difficulty values
            updateDifficultyMultipliers();
            LOGGER.log(Level.INFO, "EnemySystem using difficulty scaling - initial difficulty: {0}", currentDifficulty);
        } else {
            // Use fallback multipliers
            hunterSpeedMultiplier = FALLBACK_HUNTER_SPEED_MULTIPLIER;
            hunterFiringRateMultiplier = FALLBACK_HUNTER_FIRING_MULTIPLIER;
            LOGGER.log(Level.WARNING, "IDifficultyService not available - using fallback enemy scaling");
        }

        // Subscribe to difficulty change events
        if (eventService != null) {
            eventService.subscribe(DifficultyChangedEvent.class, this);
            LOGGER.log(Level.INFO, "EnemySystem subscribed to DifficultyChangedEvent");
        }
    }

    @Override
    public int getPriority() {
        return 75; // Run before MovementSystem to set movement parameters
    }

    @Override
    public void update(GameData gameData, World world) {
        // Refresh services if not available
        refreshServices();

        // Spawn new enemies using difficulty-scaled parameters
        if (enemySPI != null) {
            enemySPI.spawnEnemies(gameData, world);
        }

        Vector2D playerPosition = findPlayerPosition(world);
        if (playerPosition == null) {
            return; // No player found
        }

        // Process all enemies with difficulty scaling
        for (Entity enemy : world.getEntities()) {
            if (isEnemy(enemy)) {
                processEnemy(enemy, playerPosition, gameData, world);
            }
        }
    }

    /**
     * Handle difficulty change events for real-time scaling
     */
    @Override
    public void onEvent(DifficultyChangedEvent event) {
        updateDifficultyMultipliers();

        LOGGER.log(Level.INFO, "EnemySystem difficulty updated - Level: {0}, Hunter Speed: {1}x, Firing Rate: {2}x",
                new Object[]{currentDifficulty, hunterSpeedMultiplier, hunterFiringRateMultiplier});
    }

    /**
     * Update difficulty multipliers from the difficulty service
     */
    private void updateDifficultyMultipliers() {
        if (difficultyService != null) {
            currentDifficulty = difficultyService.getCurrentDifficulty();
            hunterSpeedMultiplier = difficultyService.getHunterSpeedMultiplier();
            hunterFiringRateMultiplier = difficultyService.getHunterFiringRateMultiplier();
        }
    }

    /**
     * Refresh service references if they weren't available during initialization
     */
    private void refreshServices() {
        if (enemySPI == null) {
            enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        }
        if (weaponSPI == null) {
            weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        }
        if (difficultyService == null) {
            difficultyService = ServiceLoader.load(IDifficultyService.class).findFirst().orElse(null);
            if (difficultyService != null) {
                updateDifficultyMultipliers();
                LOGGER.log(Level.INFO, "IDifficultyService became available - switching to difficulty scaling");
            }
        }
    }

    /**
     * Process individual enemy behavior with difficulty scaling
     */
    private void processEnemy(Entity enemy, Vector2D playerPosition, GameData gameData, World world) {
        EnemyComponent enemyComp = enemy.getComponent(EnemyComponent.class);
        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        WeaponComponent weapon = enemy.getComponent(WeaponComponent.class);

        if (enemyComp == null || transform == null) {
            return;
        }

        if (weapon != null) {
            weapon.updateCooldown(Time.getDeltaTimeF());
        }

        switch (enemyComp.getType()) {
            case HUNTER:
                processHunterBehavior(enemy, enemyComp, transform, weapon, playerPosition, gameData, world);
                break;
            case TURRET:
                processTurretBehavior(enemy, enemyComp, transform, weapon, playerPosition, gameData, world);
                break;
        }
    }

    /**
     * HUNTER: Actively tracking and hunting the player with difficulty scaling
     */
    private void processHunterBehavior(Entity hunter, EnemyComponent enemyComp, TransformComponent transform,
                                       WeaponComponent weapon, Vector2D playerPosition, GameData gameData, World world) {

        Vector2D hunterPos = transform.getPosition();
        Vector2D toPlayer = playerPosition.subtract(hunterPos);
        float distance = toPlayer.magnitude();

        if (distance < 0.1f) {
            return; // Too close to calculate direction
        }

        float targetAngle = (float) Math.toDegrees(Math.atan2(toPlayer.y(), toPlayer.x()));

        // Apply difficulty scaling to rotation speed
        float scaledRotationSpeed = BASE_HUNTER_ROTATION_SPEED * getRotationSpeedMultiplier();
        rotateTowardTarget(transform, targetAngle, scaledRotationSpeed);

        MovementComponent movement = hunter.getComponent(MovementComponent.class);
        if (movement != null) {
            movement.setPattern(MovementComponent.MovementPattern.LINEAR);

            if (distance > BASE_HUNTER_MIN_DISTANCE) {
                // Apply difficulty scaling to movement speed
                float scaledSpeed = BASE_HUNTER_SPEED * hunterSpeedMultiplier;
                movement.setSpeed(scaledSpeed);
                transform.setRotation(targetAngle);
            } else {
                movement.setSpeed(0f);
            }
        }

        handleEnemyFiring(hunter, enemyComp, weapon, playerPosition, gameData, world);

        LOGGER.log(Level.FINEST, "HUNTER {0}: distance={1}, angle={2}, speed={3}x, firing={4}x",
                new Object[]{hunter.getID(), (int)distance, (int)targetAngle,
                        hunterSpeedMultiplier, hunterFiringRateMultiplier});
    }

    /**
     * TURRET: Stationary rotation tracking (no difficulty scaling for movement)
     */
    private void processTurretBehavior(Entity turret, EnemyComponent enemyComp, TransformComponent transform,
                                       WeaponComponent weapon, Vector2D playerPosition, GameData gameData, World world) {

        Vector2D turretPos = transform.getPosition();
        Vector2D toPlayer = playerPosition.subtract(turretPos);
        float distance = toPlayer.magnitude();

        if (distance < 0.1f) {
            return;
        }

        float targetAngle = (float) Math.toDegrees(Math.atan2(toPlayer.y(), toPlayer.x()));

        // Turrets maintain constant rotation speed (no difficulty scaling for rotation)
        rotateTowardTarget(transform, targetAngle, BASE_TURRET_ROTATION_SPEED);

        MovementComponent movement = turret.getComponent(MovementComponent.class);
        if (movement != null) {
            movement.setSpeed(0.0f);
            movement.setPattern(MovementComponent.MovementPattern.LINEAR);
        }

        handleEnemyFiring(turret, enemyComp, weapon, playerPosition, gameData, world);

        LOGGER.log(Level.FINEST, "TURRET {0}: angle={1}",
                new Object[]{turret.getID(), (int)targetAngle});
    }

    /**
     * Get rotation speed multiplier for hunters
     * Slightly faster rotation at higher difficulties for better tracking
     */
    private float getRotationSpeedMultiplier() {
        if (difficultyService != null) {
            float difficulty = difficultyService.getCurrentDifficulty();
            // Rotation speed increases slightly with difficulty (1.0x to 1.3x)
            return 1.0f + (Math.min(difficulty / 2.0f, 1.0f) * 0.3f);
        }
        return 1.0f;
    }

    /**
     * Smooth rotation toward target angle
     */
    private void rotateTowardTarget(TransformComponent transform, float targetAngle, float rotationSpeed) {
        float currentAngle = transform.getRotation();
        float angleDiff = normalizeAngle(targetAngle - currentAngle);

        float deltaTime = Time.getDeltaTimeF();
        float maxRotation = rotationSpeed * deltaTime;

        if (Math.abs(angleDiff) <= maxRotation) {
            transform.setRotation(targetAngle);
        } else {
            float direction = angleDiff > 0 ? 1.0f : -1.0f;
            transform.setRotation(currentAngle + direction * maxRotation);
        }
    }

    /**
     * Handle enemy firing behavior with difficulty scaling
     */
    private void handleEnemyFiring(Entity enemy, EnemyComponent enemyComp, WeaponComponent weapon,
                                   Vector2D playerPosition, GameData gameData, World world) {

        if (weapon == null || weaponSPI == null || !weapon.canFire()) {
            return;
        }

        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        if (transform == null) {
            return;
        }

        float distance = playerPosition.subtract(transform.getPosition()).magnitude();
        if (distance > enemyComp.getFireDistance()) {
            return;
        }

        // Check if should fire (with difficulty scaling handled by EnemyFactory.shouldFire)
        float[] playerPos = {playerPosition.x(), playerPosition.y()};
        if (enemySPI != null && enemySPI.shouldFire(enemy, playerPos)) {
            List<Entity> bullets = weaponSPI.shoot(enemy, gameData, weapon.getBulletType());
            for (Entity bullet : bullets) {
                world.addEntity(bullet);
            }

            LOGGER.log(Level.FINE, "{0} fired {1} projectiles (difficulty: {2})",
                    new Object[]{enemyComp.getType(), bullets.size(), currentDifficulty});
        }
    }

    private float normalizeAngle(float angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private boolean isEnemy(Entity entity) {
        TagComponent tag = entity.getComponent(TagComponent.class);
        return tag != null && tag.hasType(EntityType.ENEMY);
    }

    private Vector2D findPlayerPosition(World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                TransformComponent transform = entity.getComponent(TransformComponent.class);
                if (transform != null) {
                    return transform.getPosition();
                }
            }
        }
        return null;
    }
}