package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;

import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of standard collision handlers for common game scenarios.
 */
public class CollisionHandlers {
    private static final Logger LOGGER = Logger.getLogger(CollisionHandlers.class.getName());

    // Flicker durations
    private static final float ASTEROID_DAMAGE_FLICKER_DURATION = 0.3f;
    private static final float PLAYER_DAMAGE_FLICKER_DURATION = 1.0f;

    private CollisionHandlers() {

    }

    /**
     * Handler for bullets hitting damageable entities (asteroids, enemies, players)
     */
    public static final ICollisionHandler BULLET_DAMAGE_HANDLER = (bullet, target, context) -> {
        BulletComponent bulletComponent = bullet.getComponent(BulletComponent.class);
        if (bulletComponent == null) {
            return CollisionResult.none();
        }

        // Prevent bullets from hitting their shooter
        UUID shooterID = bulletComponent.getShooterID();
        if (shooterID != null && shooterID.toString().equals(target.getID())) {
            return CollisionResult.none();
        }

        CollisionResult result = new CollisionResult();

        // Apply damage based on target type
        if (context.hasType(target, EntityType.ASTEROID)) {
            result.combine(handleAsteroidDamage(target, 1, context));
        } else if (context.hasType(target, EntityType.ENEMY)) {
            result.combine(handleEnemyDamage(target, bulletComponent.getDamage(), bulletComponent.getSource(), context));
        } else if (context.hasType(target, EntityType.PLAYER)) {
            result.combine(handlePlayerDamage(target, 1, context));
        }

        // Handle bullet destruction/piercing
        if (bulletComponent.isPiercing()) {
            bulletComponent.incrementPierceCount();
            if (bulletComponent.isPierceCountExceeded()) {
                result.addRemoval(bullet);
            }
        } else {
            result.addRemoval(bullet);
        }

        return result;
    };

    /**
     * Handler for direct entity damage (player-asteroid, player-enemy collisions)
     */
    public static final ICollisionHandler DIRECT_DAMAGE_HANDLER = (self, other, context) -> {
        CollisionResult result = new CollisionResult();

        // Apply 1 damage to self if it's a player
        if (context.hasType(self, EntityType.PLAYER)) {
            result.combine(handlePlayerDamage(self, 1, context));
        }

        // Remove other entity if it's an enemy
        if (context.hasType(other, EntityType.ENEMY)) {
            result.addRemoval(other);
        }

        return result;
    };

    /**
     * Handler for entities that should be removed on any collision
     */
    public static final ICollisionHandler REMOVE_ON_COLLISION_HANDLER = (self, other, context) -> CollisionResult.remove(self);

    /**
     * Handler for entities that ignore collisions
     */
    public static final ICollisionHandler IGNORE_COLLISION_HANDLER = (self, other, context) -> CollisionResult.none();

    /**
     * Handle damage to asteroids
     */
    public static CollisionResult handleAsteroidDamage(Entity asteroid, int damage, CollisionContext context) {
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        if (asteroidComponent == null) {
            return CollisionResult.none();
        }

        LOGGER.log(Level.INFO, "Asteroid {0} taking {1} damage. Health before: {2}/{3}",
                new Object[]{asteroid.getID(), damage, asteroidComponent.getCurrentHealth(),
                        asteroidComponent.getMaxHealth()});

        boolean destroyed = asteroidComponent.takeDamage(damage);

        if (destroyed) {
            LOGGER.log(Level.INFO, "Asteroid {0} destroyed! Publishing split event", asteroid.getID());

            CollisionResult result = CollisionResult.remove(asteroid);

            // Publish split event
            if (context.getEventService() != null) {
                result.addAction(() -> context.getEventService().publish(new AsteroidSplitEvent(asteroid)));
            }

            return result;
        } else {
            LOGGER.log(Level.INFO, "Asteroid {0} damaged! Health now: {1}/{2}. Starting flicker.",
                    new Object[]{asteroid.getID(), asteroidComponent.getCurrentHealth(),
                            asteroidComponent.getMaxHealth()});

            return CollisionResult.action(() ->
                    FlickerUtility.startDamageFlicker(asteroid, ASTEROID_DAMAGE_FLICKER_DURATION));
        }
    }

    /**
     * Handle damage to enemies
     */
    public static CollisionResult handleEnemyDamage(Entity enemy, float damage,
                                                    BulletComponent.BulletSource source,
                                                    CollisionContext context) {
        EnemyComponent enemyComponent = enemy.getComponent(EnemyComponent.class);
        if (enemyComponent == null) {
            return CollisionResult.none();
        }

        boolean eliminated = enemyComponent.damage(damage);
        if (eliminated) {
            LOGGER.log(Level.INFO, "Enemy destroyed, score: {0}", enemyComponent.getScoreValue());

            CollisionResult result = CollisionResult.remove(enemy);

            // Determine cause of destruction and publish event
            EnemyDestroyedEvent.DestructionCause cause =
                    source == BulletComponent.BulletSource.PLAYER ?
                            EnemyDestroyedEvent.DestructionCause.PLAYER_BULLET :
                            EnemyDestroyedEvent.DestructionCause.OTHER;

            if (context.getEventService() != null) {
                result.addAction(() -> context.getEventService().publish(
                        new EnemyDestroyedEvent(enemy, cause, enemyComponent.getScoreValue())));
            }

            return result;
        }

        return CollisionResult.none();
    }

    /**
     * Handle damage to player
     */
    public static CollisionResult handlePlayerDamage(Entity player, int damage, CollisionContext context) {
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        if (playerComponent == null || playerComponent.isInvulnerable()) {
            return CollisionResult.none();
        }

        boolean eliminated = playerComponent.takeDamage(damage);

        if (eliminated) {
            LOGGER.log(Level.INFO, "Player eliminated! Game Over.");
            return CollisionResult.none(); // Don't remove player entity immediately
        } else {
            LOGGER.log(Level.INFO, "Player hit! Health: {0}/{1}, Lives: {2}",
                    new Object[]{playerComponent.getCurrentHealth(), playerComponent.getMaxHealth(),
                            playerComponent.getLives()});

            return CollisionResult.action(() ->
                    FlickerUtility.startDamageFlicker(player, PLAYER_DAMAGE_FLICKER_DURATION));
        }
    }

    /**
     * Create a custom damage handler with specific damage amount
     */
    public static ICollisionHandler createDamageHandler(int damage) {
        return (self, other, context) -> {
            if (context.hasType(self, EntityType.PLAYER)) {
                return handlePlayerDamage(self, damage, context);
            }
            return CollisionResult.none();
        };
    }

    /**
     * Create a conditional handler that only executes if a condition is met
     */
    public static ICollisionHandler createConditionalHandler(
            BiPredicate<Entity, Entity> condition,
            ICollisionHandler handler) {
        return (self, other, context) -> {
            if (condition.test(self, other)) {
                return handler.handle(self, other, context);
            }
            return CollisionResult.none();
        };
    }

    /**
     * Create a player damage handler that only damages if player is not invulnerable
     */
    public static ICollisionHandler createPlayerDamageHandler(int damage) {
        return (self, player, context) -> {
            PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
            if (playerComponent != null && !playerComponent.isInvulnerable()) {
                return handlePlayerDamage(player, damage, context);
            }
            return CollisionResult.none();
        };
    }

    /**
     * Create a handler that damages player and removes the attacking entity
     */
    public static ICollisionHandler createPlayerDamageAndRemoveSelfHandler(int damage) {
        return (self, player, context) -> {
            CollisionResult result = createPlayerDamageHandler(damage).handle(self, player, context);
            result.addRemoval(self);
            return result;
        };
    }

    /**
     * Create a handler for enemy bullets hitting players
     */
    public static final ICollisionHandler ENEMY_BULLET_HANDLER = (bullet, player, context) -> {
        BulletComponent bulletComponent = bullet.getComponent(BulletComponent.class);
        if (bulletComponent != null && bulletComponent.getSource() == BulletComponent.BulletSource.ENEMY) {
            return handlePlayerDamage(player, 1, context);
        }
        return CollisionResult.none();
    };
}