package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.collision.CollisionDetector;
import dk.sdu.mmmi.cbse.collision.CollisionResolver;
import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.commoncollision.*;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Integration tests for Collision Detection and Resolution
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Collision Detection-Resolution Integration Tests")
class CollisionIntegrationTest {

    @Mock
    // Shouldn't this be used?
    private IEventService mockEventService;

    private CollisionDetector collisionDetector;
    private CollisionResolver collisionResolver;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        collisionDetector = new CollisionDetector();
        collisionResolver = new CollisionResolver();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
    }

    @Test
    @DisplayName("Should detect and resolve player-enemy collision with damage")
    void shouldDetectAndResolvePlayerEnemyCollisionWithDamage() {
        // Create player with health
        Entity player = createPlayerEntity(100, 100, 3);

        // Create enemy
        Entity enemy = createEnemyEntity(105, 100);

        world.addEntity(player);
        world.addEntity(enemy);

        // Detect collisions
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(1, collisions.size());

        // Resolve collisions
        List<Entity> entitiesToRemove = collisionResolver.resolveCollisions(collisions, gameData, world);

        // Verify player took damage
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        assertEquals(2, playerComponent.getCurrentHealth()); // Should have lost 1 health

        // Verify enemy marked for removal
        assertTrue(entitiesToRemove.contains(enemy));
    }

    @Test
    @DisplayName("Should handle player collision with invulnerability")
    void shouldHandlePlayerCollisionWithInvulnerability() {
        // Create invulnerable player
        Entity player = createPlayerEntity(100, 100, 3);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        playerComponent.setInvulnerable(true);

        // Create enemy
        Entity enemy = createEnemyEntity(105, 100);

        world.addEntity(player);
        world.addEntity(enemy);

        // Detect and resolve collisions
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        List<Entity> entitiesToRemove = collisionResolver.resolveCollisions(collisions, gameData, world);

        // Verify player took no damage due to invulnerability
        assertEquals(3, playerComponent.getCurrentHealth());

        // Enemy should still be removed due to collision
        assertTrue(entitiesToRemove.contains(enemy));
    }

    @Test
    @DisplayName("Should handle multiple simultaneous collisions")
    // ToDo: ERROR: Entity missing FlickerComponent for damage flicker
    void shouldHandleMultipleSimultaneousCollisions() {
        // Create player
        Entity player = createPlayerEntity(100, 100, 3);

        // Create multiple enemies colliding with player
        Entity enemy1 = createEnemyEntity(105, 100);
        Entity enemy2 = createEnemyEntity(95, 100);

        world.addEntity(player);
        world.addEntity(enemy1);
        world.addEntity(enemy2);

        // Detect collisions
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(2, collisions.size());

        // Resolve collisions
        List<Entity> entitiesToRemove = collisionResolver.resolveCollisions(collisions, gameData, world);

        // Verify player took damage from both collisions
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        assertEquals(1, playerComponent.getCurrentHealth()); // Lost 2 health

        // Verify both enemies marked for removal
        assertEquals(2, entitiesToRemove.size());
        assertTrue(entitiesToRemove.contains(enemy1));
        assertTrue(entitiesToRemove.contains(enemy2));
    }

    @Test
    @DisplayName("Should ignore collisions between incompatible layers")
    void shouldIgnoreCollisionsBetweenIncompatibleLayers() {
        // Create player
        Entity player = createPlayerEntity(100, 100, 3);

        // Create player bullet (should not collide with player)
        Entity playerBullet = createEntity(105, 100, 5, CollisionLayer.PLAYER_PROJECTILE, EntityType.BULLET);

        world.addEntity(player);
        world.addEntity(playerBullet);

        // Should detect no collisions due to layer incompatibility
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(0, collisions.size());
    }

    @Test
    @DisplayName("Should handle collision response actions correctly")
    void shouldHandleCollisionResponseActionsCorrectly() {
        // Create entities with custom collision response
        Entity entity1 = createEntity(100, 100, 10, CollisionLayer.PLAYER, EntityType.PLAYER);
        Entity entity2 = createEntity(105, 100, 10, CollisionLayer.ENEMY, EntityType.ENEMY);

        // Add custom collision response with action
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.ENEMY, (self, other, context) -> {
            return CollisionResult.action(() -> {
                // Custom action - mark something happened
                self.addComponent(new TagComponent(EntityType.ASTEROID)); // Use as marker
            });
        });
        entity1.addComponent(response);

        world.addEntity(entity1);
        world.addEntity(entity2);

        // Detect and resolve collisions
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        collisionResolver.resolveCollisions(collisions, gameData, world);

        // Verify custom action was executed
        TagComponent tag = entity1.getComponent(TagComponent.class);
        assertTrue(tag.hasType(EntityType.ASTEROID)); // Our marker
    }

    private Entity createPlayerEntity(float x, float y, int health) {
        Entity player = createEntity(x, y, 10, CollisionLayer.PLAYER, EntityType.PLAYER);

        // Add player-specific components
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setMaxHealth(health);
        player.addComponent(playerComponent);

        // Add player collision response
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.ENEMY, CollisionHandlers.DIRECT_DAMAGE_HANDLER);
        player.addComponent(response);

        return player;
    }

    private Entity createEnemyEntity(float x, float y) {
        Entity enemy = createEntity(x, y, 8, CollisionLayer.ENEMY, EntityType.ENEMY);

        // Add enemy collision response
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.PLAYER, (self, other, context) -> {
            CollisionResult result = CollisionHandlers.handlePlayerDamage(other, 1, context);
            result.addRemoval(self);
            return result;
        });
        enemy.addComponent(response);

        return enemy;
    }

    private Entity createEntity(float x, float y, float radius, CollisionLayer layer, EntityType type) {
        Entity entity = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(x, y));
        transform.setRadius(radius);
        entity.addComponent(transform);

        // Collider
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);
        entity.addComponent(collider);

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(type);
        entity.addComponent(tag);

        return entity;
    }
}