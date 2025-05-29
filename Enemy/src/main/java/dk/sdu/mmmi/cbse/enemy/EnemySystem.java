package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
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
 * System managing Enemy behaviors.
 */
public class EnemySystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(EnemySystem.class.getName());

    private IEnemySPI enemySPI;
    private IWeaponSPI weaponSPI;

    // HUNTER
    private static final float HUNTER_MIN_DISTANCE = 70.0f;    // Minimum distance from player
    private static final float HUNTER_SPEED = 90.0f;
    private static final float HUNTER_ROTATION_SPEED = 150.0f;

    // TURRET
    private static final float TURRET_ROTATION_SPEED = 80.0f;

    public EnemySystem() {
        this.enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);

        if (enemySPI == null) {
            LOGGER.log(Level.SEVERE, "IEnemySPI not found! Enemy system will not work.");
        } else {
            LOGGER.log(Level.INFO, "EnemySystem initialized");
        }
    }

    @Override
    public int getPriority() {
        return 75; // Run before MovementSystem to set movement parameters
    }

    @Override
    public void update(GameData gameData, World world) {
        if (enemySPI == null) {
            enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        }
        if (weaponSPI == null) {
            weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        }

        if (enemySPI != null) {
            enemySPI.spawnEnemies(gameData, world);
        }

        Vector2D playerPosition = findPlayerPosition(world);
        if (playerPosition == null) {
            return; // No player found
        }

        for (Entity enemy : world.getEntities()) {
            if (isEnemy(enemy)) {
                processEnemy(enemy, playerPosition, gameData, world);
            }
        }
    }

    /**
     * Process individual enemy behavior
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
     * HUNTER: Actively tracking and hunting the player
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

        rotateTowardTarget(transform, targetAngle, HUNTER_ROTATION_SPEED);

        MovementComponent movement = hunter.getComponent(MovementComponent.class);
        if (movement != null) {
            movement.setPattern(MovementComponent.MovementPattern.LINEAR);

            if (distance > HUNTER_MIN_DISTANCE) {
                movement.setSpeed(HUNTER_SPEED);
                transform.setRotation(targetAngle);
            } else {
                movement.setSpeed(0f);
            }
        }

        handleEnemyFiring(hunter, enemyComp, weapon, playerPosition, gameData, world);

        LOGGER.log(Level.FINEST, "HUNTER {0}: distance={1}, angle={2}",
                new Object[]{hunter.getID(), (int)distance, (int)targetAngle});
    }

    /**
     * TURRET: Stationary rotation tracking
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

        rotateTowardTarget(transform, targetAngle, TURRET_ROTATION_SPEED);

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
     * Handle enemy firing behavior
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

        // Check if should fire
        float[] playerPos = {playerPosition.x(), playerPosition.y()};
        if (enemySPI != null && enemySPI.shouldFire(enemy, playerPos)) {
            List<Entity> bullets = weaponSPI.shoot(enemy, gameData, weapon.getBulletType());
            for (Entity bullet : bullets) {
                world.addEntity(bullet);
            }

            LOGGER.log(Level.FINE, "{0} fired {1} projectiles",
                    new Object[]{enemyComp.getType(), bullets.size()});
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