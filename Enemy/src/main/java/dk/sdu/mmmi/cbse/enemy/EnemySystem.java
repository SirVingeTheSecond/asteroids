package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that processes enemy behavior.
 */
public class EnemySystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(EnemySystem.class.getName());

    private IEnemySPI enemySPI;
    private IWeaponSPI weaponSPI;

    public EnemySystem() {
        this.enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);

        LOGGER.log(Level.INFO, "EnemySystem initialized");
    }

    @Override
    public void process(GameData gameData, World world) {
        if (enemySPI == null) {
            enemySPI = ServiceLoader.load(IEnemySPI.class).findFirst().orElse(null);
        }

        if (weaponSPI == null) {
            weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        }

        if (enemySPI != null) {
            enemySPI.spawnEnemies(gameData, world);
        }

        Entity player = findPlayer(world);
        float[] playerPosition = null;

        if (player != null) {
            TransformComponent playerTransform = player.getComponent(TransformComponent.class);
            if (playerTransform != null) {
                playerPosition = new float[]{playerTransform.getX(), playerTransform.getY()};
            }
        }

        // Process each enemy
        for (Entity enemy : world.getEntities()) {
            // Skip non-enemy entities
            TagComponent tag = enemy.getComponent(TagComponent.class);
            if (tag == null || !tag.hasType(EntityType.ENEMY)) {
                continue;
            }

            processEnemy(enemy, playerPosition, gameData, world);
        }
    }

    /**
     * Process an individual enemy
     *
     * @param enemy Enemy entity
     * @param playerPosition Player position (may be null)
     * @param gameData Game data
     * @param world Game world
     */
    private void processEnemy(Entity enemy, float[] playerPosition, GameData gameData, World world) {
        EnemyComponent enemyComponent = enemy.getComponent(EnemyComponent.class);
        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        MovementComponent movement = enemy.getComponent(MovementComponent.class);
        WeaponComponent weapon = enemy.getComponent(WeaponComponent.class);

        // Skip if missing required components
        if (enemyComponent == null || transform == null) {
            return;
        }

        // For hunter enemies, adjust movement toward player
        if (enemyComponent.getType() == EnemyType.HUNTER &&
                movement != null && playerPosition != null) {

            // Calculate direction to player
            float dx = playerPosition[0] - transform.getX();
            float dy = playerPosition[1] - transform.getY();
            float angle = (float) Math.toDegrees(Math.atan2(dy, dx));

            // Smoothly rotate toward player
            float currentRotation = transform.getRotation();
            float angleDiff = angle - currentRotation;

            // Handle angle wrapping
            if (angleDiff > 180) angleDiff -= 360;
            if (angleDiff < -180) angleDiff += 360;

            // Gradually rotate toward player
            float rotationRate = movement.getRotationSpeed();
            if (Math.abs(angleDiff) > rotationRate) {
                float newRotation = currentRotation + (angleDiff > 0 ? rotationRate : -rotationRate);
                transform.setRotation(newRotation);
            } else {
                transform.setRotation(angle);
            }
        }

        // Handle weapon firing
        if (weaponSPI != null && weapon != null && playerPosition != null) {
            // Check if enemy should fire
            boolean shouldFire = enemySPI != null &&
                    enemySPI.shouldFire(enemy, playerPosition);

            if (shouldFire != weapon.isFiring()) {
                weapon.setFiring(shouldFire);
            }
            
            if (weapon.isFiring() && weapon.canFire()) {
                weaponSPI.shoot(enemy, gameData, weapon.getBulletType());
            }
        }
    }

    /**
     * Find player entity in world
     *
     * @param world Game world
     * @return Player entity or null if not found
     */
    private Entity findPlayer(World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                return entity;
            }
        }
        return null;
    }
}