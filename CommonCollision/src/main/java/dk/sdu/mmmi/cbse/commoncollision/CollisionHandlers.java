package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;

import java.util.ServiceLoader;
import java.util.UUID;
import java.util.function.BiPredicate;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collection of collision handlers.
 */
public class CollisionHandlers {
    private static final Logger LOGGER = Logger.getLogger(CollisionHandlers.class.getName());

    // Flicker durations
    private static final float ASTEROID_DAMAGE_FLICKER_DURATION = 0.3f;
    private static final float PLAYER_DAMAGE_FLICKER_DURATION = 1f;

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
            result.combine(handleAsteroidDamageFromBullet(target, 1, bullet, context));
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
     * Handler for asteroid-asteroid collisions
     */
    public static final ICollisionHandler ASTEROID_ASTEROID_COLLISION_HANDLER = (asteroid1, asteroid2, context) -> {
        CollisionResult result = new CollisionResult();

        IPhysicsSPI physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        if (physicsSPI == null) {
            LOGGER.log(Level.WARNING, "PhysicsSPI not available for asteroid collision");
            return result;
        }

        TransformComponent transform1 = asteroid1.getComponent(TransformComponent.class);
        TransformComponent transform2 = asteroid2.getComponent(TransformComponent.class);

        if (transform1 == null || transform2 == null) {
            return result;
        }

        // Get physics components
        PhysicsComponent physics1 = asteroid1.getComponent(PhysicsComponent.class);
        PhysicsComponent physics2 = asteroid2.getComponent(PhysicsComponent.class);

        if (physics1 == null || physics2 == null) {
            return result;
        }

        // Calculate collision normal and verify collision
        Vector2D pos1 = transform1.getPosition();
        Vector2D pos2 = transform2.getPosition();
        Vector2D collisionDirection = pos2.subtract(pos1);

        float distance = collisionDirection.magnitude();
        float minDistance = transform1.getRadius() + transform2.getRadius();

        // Check if actually colliding
        if (distance >= minDistance || distance < 0.001f) {
            return result; // Not colliding or too close to calculate
        }

        Vector2D collisionNormal = collisionDirection.normalize();

        // Step 1: Solid Separation - ensure no penetration
        float penetration = minDistance - distance;
        if (penetration > 0) {
            float separationBuffer = 4.0f; // This is a very generous buffer
            float totalSeparation = penetration + separationBuffer;

            float totalMass = physics1.getMass() + physics2.getMass();
            float separation1 = totalSeparation * (physics2.getMass() / totalMass);
            float separation2 = totalSeparation * (physics1.getMass() / totalMass);

            transform1.translate(collisionNormal.scale(-separation1));
            transform2.translate(collisionNormal.scale(separation2));
        }

        // Step 2: Inspired by 8-ball physics
        Vector2D vel1 = physicsSPI.getVelocity(asteroid1);
        Vector2D vel2 = physicsSPI.getVelocity(asteroid2);

        float speed1 = vel1.magnitude();
        float speed2 = vel2.magnitude();

        // If either asteroid is nearly stationary, give it some energy
        if (speed1 < 10.0f) {
            speed1 = 50.0f;
            vel1 = new Vector2D(1, 0).scale(speed1); // Default direction
        }
        if (speed2 < 10.0f) {
            speed2 = 50.0f;
            vel2 = new Vector2D(-1, 0).scale(speed2); // Opposite direction
        }

        // Decompose velocities into normal and tangential components
        float vel1Normal = vel1.dot(collisionNormal);
        float vel2Normal = vel2.dot(collisionNormal);

        Vector2D tangent = new Vector2D(-collisionNormal.y(), collisionNormal.x());
        float vel1Tangent = vel1.dot(tangent);
        float vel2Tangent = vel2.dot(tangent);

        // Calculate new normal velocities using elastic collision formula
        // ToDo: Show formula here.
        float mass1 = physics1.getMass();
        float mass2 = physics2.getMass();
        float totalMass = mass1 + mass2;

        float newVel1Normal = ((mass1 - mass2) * vel1Normal + 2 * mass2 * vel2Normal) / totalMass;
        float newVel2Normal = ((mass2 - mass1) * vel2Normal + 2 * mass1 * vel1Normal) / totalMass;

        // Apply "kick" for more dramatic bounces
        float kickMultiplier = 1.2f;
        newVel1Normal *= kickMultiplier;
        newVel2Normal *= kickMultiplier;

        // Reconstruct velocity vectors
        Vector2D newVel1 = collisionNormal.scale(newVel1Normal).add(tangent.scale(vel1Tangent));
        Vector2D newVel2 = collisionNormal.scale(newVel2Normal).add(tangent.scale(vel2Tangent));

        // Step 3: Preserve original speeds
        // Scale the new velocities to maintain original speeds
        float newSpeed1 = newVel1.magnitude();
        float newSpeed2 = newVel2.magnitude();

        if (newSpeed1 > 0.001f) {
            newVel1 = newVel1.normalize().scale(speed1);
        }
        if (newSpeed2 > 0.001f) {
            newVel2 = newVel2.normalize().scale(speed2);
        }

        // Step 4: Some modifications
        // Slightly increase speeds for more dynamic gameplay
        float energyBoost = 1.05f; // 5% speed increase per collision
        newVel1 = newVel1.scale(energyBoost);
        newVel2 = newVel2.scale(energyBoost);

        // Cap maximum speeds to prevent crazy acceleration
        float maxSpeed = 350.0f;
        if (newVel1.magnitude() > maxSpeed) {
            newVel1 = newVel1.normalize().scale(maxSpeed);
        }
        if (newVel2.magnitude() > maxSpeed) {
            newVel2 = newVel2.normalize().scale(maxSpeed);
        }

        // Step 5: Apply new velocities!
        physicsSPI.setVelocity(asteroid1, newVel1);
        physicsSPI.setVelocity(asteroid2, newVel2);

        // Add some random angular velocity because visuals :D
        float angularImpact1 = (float)(Math.random() - 0.5) * 120.0f; // +-60 deg/s
        float angularImpact2 = (float)(Math.random() - 0.5) * 120.0f;

        physicsSPI.setAngularVelocity(asteroid1,
                physicsSPI.getAngularVelocity(asteroid1) + angularImpact1);
        physicsSPI.setAngularVelocity(asteroid2,
                physicsSPI.getAngularVelocity(asteroid2) + angularImpact2);

        return result;
    };

    /**
     * Handler for bullet-bullet collisions (player vs enemy bullets)
     */
    public static final ICollisionHandler BULLET_BULLET_COLLISION_HANDLER = (bullet1, bullet2, context) -> {
        BulletComponent bulletComp1 = bullet1.getComponent(BulletComponent.class);
        BulletComponent bulletComp2 = bullet2.getComponent(BulletComponent.class);

        if (bulletComp1 == null || bulletComp2 == null) {
            return CollisionResult.none();
        }

        // Only destroy if they're from different sources (player vs enemy)
        if (bulletComp1.getSource() != bulletComp2.getSource()) {
            return CollisionResult.remove(bullet1, bullet2);
        }

        return CollisionResult.none();
    };

    /**
     * Handler for direct entity damage (player-asteroid, player-enemy collisions)
     */
    public static final ICollisionHandler DIRECT_DAMAGE_HANDLER = (self, other, context) -> {
        CollisionResult result = new CollisionResult();

        if (context.hasType(self, EntityType.PLAYER)) {
            result.combine(handlePlayerDamage(self, 1, context));
        }

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
     * Handle damage to asteroids from bullets
     */
    public static CollisionResult handleAsteroidDamageFromBullet(Entity asteroid, int damage, Entity bullet, CollisionContext context) {
        AsteroidComponent asteroidComponent = asteroid.getComponent(AsteroidComponent.class);
        if (asteroidComponent == null) {
            return CollisionResult.none();
        }

        LOGGER.log(Level.INFO, "Asteroid {0} taking {1} damage from bullet. Health before: {2}/{3}",
                new Object[]{asteroid.getID(), damage, asteroidComponent.getCurrentHealth(),
                        asteroidComponent.getMaxHealth()});

        boolean destroyed = asteroidComponent.takeDamage(damage);

        if (destroyed) {
            LOGGER.log(Level.INFO, "Asteroid {0} destroyed by bullet! Publishing split event", asteroid.getID());

            CollisionResult result = CollisionResult.remove(asteroid);

            Vector2D bulletVelocity = getBulletVelocity(bullet);
            Vector2D impactPoint = getImpactPoint(asteroid, bullet);

            if (context.getEventService() != null) {
                result.addAction(() -> context.getEventService().publish(
                        new AsteroidSplitEvent(asteroid, bulletVelocity, impactPoint)));
            }

            return result;
        } else {
            LOGGER.log(Level.INFO, "Asteroid {0} damaged by bullet! Health now: {1}/{2}. Starting flicker.",
                    new Object[]{asteroid.getID(), asteroidComponent.getCurrentHealth(),
                            asteroidComponent.getMaxHealth()});

            return CollisionResult.action(() ->
                    FlickerUtility.startDamageFlicker(asteroid, ASTEROID_DAMAGE_FLICKER_DURATION));
        }
    }

    /**
     * Get bullet velocity for splitting calculations
     */
    private static Vector2D getBulletVelocity(Entity bullet) {
        IPhysicsSPI physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        if (physicsSPI != null && physicsSPI.hasPhysics(bullet)) {
            Vector2D velocity = physicsSPI.getVelocity(bullet);
            if (velocity.magnitudeSquared() > 0.001f) {
                return velocity;
            }
        }

        // Fallback: calculate from transform and movement component
        TransformComponent transform = bullet.getComponent(TransformComponent.class);
        if (transform != null) {
            Vector2D forward = transform.getForward();
            BulletComponent bulletComp = bullet.getComponent(BulletComponent.class);
            if (bulletComp != null) {
                return forward.scale(bulletComp.getSpeed());
            }
            // if no component
            return forward.scale(300.0f);
        }

        // If this happens...
        return new Vector2D(300.0f, 0.0f);
    }

    /**
     * Get impact point for splitting calculations
     */
    private static Vector2D getImpactPoint(Entity asteroid, Entity bullet) {
        TransformComponent asteroidTransform = asteroid.getComponent(TransformComponent.class);
        TransformComponent bulletTransform = bullet.getComponent(TransformComponent.class);

        if (asteroidTransform != null && bulletTransform != null) {
            // Calculate impact point as the point on asteroid closest to bullet
            Vector2D asteroidPos = asteroidTransform.getPosition();
            Vector2D bulletPos = bulletTransform.getPosition();
            Vector2D direction = bulletPos.subtract(asteroidPos);

            if (direction.magnitudeSquared() > 0.001f) {
                direction = direction.normalize();
                float asteroidRadius = asteroidTransform.getRadius();
                return asteroidPos.add(direction.scale(asteroidRadius));
            }
        }

        // Fallback to asteroid center if calculation fails
        return asteroidTransform != null ? asteroidTransform.getPosition() : Vector2D.zero();
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