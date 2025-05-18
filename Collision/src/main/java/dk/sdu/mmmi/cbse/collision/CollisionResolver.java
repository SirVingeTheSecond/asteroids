package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.events.EnemyDestroyedEvent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collision resolution system.
 */
public class CollisionResolver {
    private static final Logger LOGGER = Logger.getLogger(CollisionResolver.class.getName());

    private final IEventService eventService;

    public CollisionResolver() {
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "CollisionResolver initialized with EventService: {0}",
                eventService != null ? "available" : "not available");
    }

    /**
     * Resolve all collisions in the current frame
     */
    public List<Entity> resolveCollisions(List<Pair<Entity, Entity>> collisions, GameData gameData, World world) {
        List<Entity> entitiesToRemove = new ArrayList<>();

        for (Pair<Entity, Entity> collision : collisions) {
            Entity entity1 = collision.getFirst();
            Entity entity2 = collision.getSecond();

            // Process collision between specific entity types
            if (resolveCollision(entity1, entity2, entitiesToRemove)) {
                LOGGER.log(Level.FINE, "Resolved collision between {0} and {1}",
                        new Object[]{entity1.getID(), entity2.getID()});
            }
        }

        return entitiesToRemove;
    }

    /**
     * Resolve a specific collision between two entities
     */
    private boolean resolveCollision(Entity entity1, Entity entity2, List<Entity> entitiesToRemove) {
        // Get entity types
        TagComponent tag1 = entity1.getComponent(TagComponent.class);
        TagComponent tag2 = entity2.getComponent(TagComponent.class);

        if (tag1 == null || tag2 == null) {
            return false;
        }

        // Handle bullet collisions first
        if (tag1.hasType(EntityType.BULLET) || tag2.hasType(EntityType.BULLET)) {
            return handleBulletCollision(
                    tag1.hasType(EntityType.BULLET) ? entity1 : entity2,
                    tag1.hasType(EntityType.BULLET) ? entity2 : entity1,
                    entitiesToRemove);
        }

        // Handle player-enemy collisions
        if ((tag1.hasType(EntityType.PLAYER) && tag2.hasType(EntityType.ENEMY)) ||
                (tag1.hasType(EntityType.ENEMY) && tag2.hasType(EntityType.PLAYER))) {
            return handlePlayerEnemyCollision(
                    tag1.hasType(EntityType.PLAYER) ? entity1 : entity2,
                    tag1.hasType(EntityType.PLAYER) ? entity2 : entity1,
                    entitiesToRemove);
        }

        // Handle player-asteroid collisions
        if ((tag1.hasType(EntityType.PLAYER) && tag2.hasType(EntityType.ASTEROID)) ||
                (tag1.hasType(EntityType.ASTEROID) && tag2.hasType(EntityType.PLAYER))) {
            return handlePlayerAsteroidCollision(
                    tag1.hasType(EntityType.PLAYER) ? entity1 : entity2,
                    tag1.hasType(EntityType.PLAYER) ? entity2 : entity1,
                    entitiesToRemove);
        }

        return false;
    }

    /**
     * Handle collision between a bullet and another entity
     */
    private boolean handleBulletCollision(Entity bullet, Entity target, List<Entity> entitiesToRemove) {
        BulletComponent bulletComponent = bullet.getComponent(BulletComponent.class);
        TagComponent targetTag = target.getComponent(TagComponent.class);

        if (bulletComponent == null || targetTag == null) {
            return false;
        }

        BulletComponent.BulletSource source = bulletComponent.getSource();
        boolean isPlayerBullet = source == BulletComponent.BulletSource.PLAYER;

        // Player bullets hit enemies and asteroids
        if (isPlayerBullet) {
            if (targetTag.hasType(EntityType.ENEMY)) {
                handleBulletEnemyCollision(bullet, target, bulletComponent, entitiesToRemove);
                return true;
            } else if (targetTag.hasType(EntityType.ASTEROID)) {
                entitiesToRemove.add(target);

                // Fire asteroid split event
                if (eventService != null) {
                    eventService.publish(new AsteroidSplitEvent(target));
                    LOGGER.log(Level.FINE, "Published AsteroidSplitEvent for asteroid {0}",
                            target.getID());
                } else {
                    LOGGER.log(Level.WARNING, "EventService not available, cannot publish AsteroidSplitEvent");
                }
                return true;
            }
        }
        // Enemy bullets hit player
        else if (targetTag.hasType(EntityType.PLAYER)) {
            handleBulletPlayerCollision(bullet, target, bulletComponent, entitiesToRemove);
            return true;
        }

        // Check if bullet should be destroyed
        if (bulletComponent.isPiercing()) {
            bulletComponent.incrementPierceCount();
            if (bulletComponent.isPierceCountExceeded()) {
                entitiesToRemove.add(bullet);
            }
        } else {
            // Non-piercing bullets are destroyed on impact
            entitiesToRemove.add(bullet);
        }

        return true;
    }

    /**
     * Handle bullet hitting an enemy
     */
    private void handleBulletEnemyCollision(Entity bullet, Entity enemy,
                                            BulletComponent bulletComponent,
                                            List<Entity> entitiesToRemove) {
        EnemyComponent enemyComponent = enemy.getComponent(EnemyComponent.class);
        if (enemyComponent != null) {
            boolean eliminated = enemyComponent.damage(bulletComponent.getDamage());
            if (eliminated) {
                entitiesToRemove.add(enemy);
                LOGGER.log(Level.INFO, "Enemy destroyed, score: {0}", enemyComponent.getScoreValue());

                // Determine cause of destruction, too janky?
                EnemyDestroyedEvent.DestructionCause cause =
                        bulletComponent.getSource() == BulletComponent.BulletSource.PLAYER ?
                                EnemyDestroyedEvent.DestructionCause.PLAYER_BULLET :
                                EnemyDestroyedEvent.DestructionCause.OTHER; // Should not be OTHER, too floaty.

                // Publish enemy destroyed event
                if (eventService != null) {
                    eventService.publish(new EnemyDestroyedEvent(
                            enemy,
                            cause,
                            enemyComponent.getScoreValue()
                    ));
                }
            }
        }
    }

    /**
     * Handle bullet hitting the player
     */
    private void handleBulletPlayerCollision(Entity bullet, Entity player,
                                             BulletComponent bulletComponent,
                                             List<Entity> entitiesToRemove) {
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        if (playerComponent != null && !playerComponent.isInvulnerable()) {
            boolean eliminated = playerComponent.damage();
            if (eliminated) {
                LOGGER.log(Level.INFO, "Player lost a life, remaining: {0}", playerComponent.getLives());
            }
        }
    }

    /**
     * Handle collision between player and enemy
     */
    private boolean handlePlayerEnemyCollision(Entity player, Entity enemy, List<Entity> entitiesToRemove) {
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

        if (playerComponent != null && !playerComponent.isInvulnerable()) {
            boolean eliminated = playerComponent.damage();
            if (eliminated) {
                LOGGER.log(Level.INFO, "Player lost a life from enemy collision, remaining: {0}",
                        playerComponent.getLives());
            }

            // Enemy is destroyed on collision
            entitiesToRemove.add(enemy);
            return true;
        }

        return false;
    }

    /**
     * Handle collision between player and asteroid
     */
    private boolean handlePlayerAsteroidCollision(Entity player, Entity asteroid, List<Entity> entitiesToRemove) {
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

        if (playerComponent != null && !playerComponent.isInvulnerable()) {
            // Player takes damage
            boolean killed = playerComponent.damage();
            if (killed) {
                LOGGER.log(Level.INFO, "Player lost a life from asteroid collision, remaining: {0}",
                        playerComponent.getLives());
            }

            // Asteroid is destroyed or split
            entitiesToRemove.add(asteroid);
            return true;
        }

        return false;
    }
}