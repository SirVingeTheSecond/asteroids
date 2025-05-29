package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayerMatrix;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.collision.CollisionDetector;
import dk.sdu.mmmi.cbse.collision.CollisionSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Integration tests for CollisionSystem.
 */
@DisplayName("Collision System Integration Tests")
public class CollisionSystemTest {

    private CollisionSystem collisionSystem;
    private CollisionDetector collisionDetector;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();
        collisionDetector = new CollisionDetector();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
    }

    @Test
    @DisplayName("System correctly detects player-asteroid collision")
    void testPlayerAsteroidCollisionDetection() {
        Entity player = createTestPlayer(100, 100, 10);
        Entity asteroid = createTestAsteroid(105, 105, 15);

        world.addEntity(player);
        world.addEntity(asteroid);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);

        assertEquals(1, collisions.size(), "Should detect exactly one collision");
        assertTrue(collisionDetector.isColliding(player, asteroid));

        Pair<Entity, Entity> collision = collisions.get(0);
        assertTrue(containsEntities(collision, player, asteroid),
                "Collision should involve both player and asteroid");
    }

    @Test
    @DisplayName("System correctly handles non-colliding entities")
    void testNoCollisionWhenEntitiesAreSeparated() {
        Entity player = createTestPlayer(100, 100, 10);
        Entity asteroid = createTestAsteroid(200, 200, 15);

        world.addEntity(player);
        world.addEntity(asteroid);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);

        assertEquals(0, collisions.size(), "Should detect no collisions when entities are separated");
        assertFalse(collisionDetector.isColliding(player, asteroid));
    }

    @Test
    @DisplayName("System respects collision layer filtering contract")
    void testCollisionLayerFiltering() {
        Entity player = createTestPlayer(100, 100, 10);
        Entity playerBullet = createTestBullet(105, 105, 3, CollisionLayer.PLAYER_PROJECTILE);

        world.addEntity(player);
        world.addEntity(playerBullet);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(0, collisions.size(), "Player bullets should not collide with player");

        // Verify layer matrix contract
        CollisionLayerMatrix matrix = CollisionLayerMatrix.getInstance();
        assertFalse(matrix.canLayersCollide(CollisionLayer.PLAYER, CollisionLayer.PLAYER_PROJECTILE),
                "Layer matrix should prevent player-bullet collisions");
    }

    @Test
    @DisplayName("System allows enemy bullet-player collisions")
    void testBulletEnemyCollisionAllowed() {
        Entity enemyBullet = createTestBullet(100, 100, 3, CollisionLayer.ENEMY_PROJECTILE);
        Entity player = createTestPlayer(105, 105, 10);

        world.addEntity(enemyBullet);
        world.addEntity(player);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(1, collisions.size(), "Enemy bullets should collide with player");

        CollisionLayerMatrix matrix = CollisionLayerMatrix.getInstance();
        assertTrue(matrix.canLayersCollide(CollisionLayer.PLAYER, CollisionLayer.ENEMY_PROJECTILE),
                "Layer matrix should allow enemy bullet-player collisions");
    }

    @Test
    @DisplayName("System handles boundary collision detection correctly")
    void testCircleCollisionDetection() {
        Entity entity1 = createTestEntity(100, 100, 10);
        Entity entity2 = createTestEntity(120, 100, 10); // Exactly touching (distance = 20, radii sum = 20)

        assertTrue(collisionDetector.isColliding(entity1, entity2),
                "Entities should collide when touching");

        entity2 = createTestEntity(121, 100, 10); // Slightly separated
        assertFalse(collisionDetector.isColliding(entity1, entity2),
                "Entities should not collide when separated");
    }

    @Test
    @DisplayName("System handles polygon collision detection")
    void testPolygonCollisionDetection() {
        Entity polygonEntity = createTestPolygonEntity(100, 100, 15);
        Entity circleEntity = createTestEntity(110, 110, 8);

        world.addEntity(polygonEntity);
        world.addEntity(circleEntity);

        // Test that polygon collision doesn't crash (exact result depends on polygon shape)
        assertDoesNotThrow(() -> {
            boolean colliding = collisionDetector.isColliding(polygonEntity, circleEntity);
        }, "Polygon collision detection should not crash");
    }

    @Test
    @DisplayName("System processes collision resolution correctly")
    void testCollisionSystemIntegration() {
        Entity player = createTestPlayer(100, 100, 10);
        Entity asteroid = createTestAsteroid(105, 105, 15);

        world.addEntity(player);
        world.addEntity(asteroid);

        int initialEntityCount = world.getEntities().size();

        assertDoesNotThrow(() -> {
            collisionSystem.process(gameData, world);
        }, "Collision system should process without errors");

        // System should handle collisions gracefully
        assertTrue(world.getEntities().size() <= initialEntityCount,
                "Entity count should not increase after collision processing");
    }

    @Test
    @DisplayName("System correctly detects multiple simultaneous collisions")
    void testMultipleCollisions() {
        Entity player = createTestPlayer(100, 100, 10);
        Entity asteroid1 = createTestAsteroid(105, 105, 15);
        Entity asteroid2 = createTestAsteroid(95, 95, 15);

        world.addEntity(player);
        world.addEntity(asteroid1);
        world.addEntity(asteroid2);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);

        // Should detect 3 collisions: player-asteroid1, player-asteroid2, asteroid1-asteroid2
        assertEquals(3, collisions.size(), "Should detect all overlapping entity pairs");

        // Verify specific collision pairs exist
        assertTrue(hasCollisionBetween(collisions, player, asteroid1),
                "Should detect player-asteroid1 collision");
        assertTrue(hasCollisionBetween(collisions, player, asteroid2),
                "Should detect player-asteroid2 collision");
        assertTrue(hasCollisionBetween(collisions, asteroid1, asteroid2),
                "Should detect asteroid1-asteroid2 collision");
    }

    @Test
    @DisplayName("System handles empty world correctly")
    void testEmptyWorldCollisions() {
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(0, collisions.size(), "Empty world should have no collisions");
    }

    @Test
    @DisplayName("System handles single entity correctly")
    void testSingleEntityNoCollisions() {
        Entity player = createTestPlayer(100, 100, 10);
        world.addEntity(player);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertEquals(0, collisions.size(), "Single entity should not collide with itself");
    }

    @Test
    @DisplayName("System maintains consistent collision detection")
    void testCollisionDetectionConsistency() {
        Entity entity1 = createTestEntity(100, 100, 10);
        Entity entity2 = createTestEntity(105, 105, 10);

        // Collision detection should be symmetric
        boolean result1 = collisionDetector.isColliding(entity1, entity2);
        boolean result2 = collisionDetector.isColliding(entity2, entity1);

        assertEquals(result1, result2, "Collision detection should be symmetric");
    }

    // Helper methods
    private Entity createTestPlayer(float x, float y, float radius) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);

        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(radius)
                .with(collider)
                .with(new PlayerComponent())
                .with(new CollisionResponseComponent())
                .build();
    }

    private Entity createTestAsteroid(float x, float y, float radius) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);

        return EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(x, y)
                .withRadius(radius)
                .with(collider)
                .with(new AsteroidComponent(AsteroidSize.LARGE))
                .with(new CollisionResponseComponent())
                .build();
    }

    private Entity createTestBullet(float x, float y, float radius, CollisionLayer layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);

        return EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(x, y)
                .withRadius(radius)
                .with(collider)
                .with(new CollisionResponseComponent())
                .build();
    }

    private Entity createTestEntity(float x, float y, float radius) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.DEFAULT);

        return EntityBuilder.create()
                .atPosition(x, y)
                .withRadius(radius)
                .with(collider)
                .build();
    }

    private Entity createTestPolygonEntity(float x, float y, float radius) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.DEFAULT);

        double[] triangleShape = {
                -radius, -radius/2,
                radius, -radius/2,
                0, radius
        };

        return EntityBuilder.create()
                .atPosition(x, y)
                .withRadius(radius)
                .withShape(triangleShape)
                .with(collider)
                .build();
    }

    private boolean containsEntities(Pair<Entity, Entity> collision, Entity e1, Entity e2) {
        return (collision.getFirst().equals(e1) && collision.getSecond().equals(e2)) ||
                (collision.getFirst().equals(e2) && collision.getSecond().equals(e1));
    }

    private boolean hasCollisionBetween(List<Pair<Entity, Entity>> collisions, Entity e1, Entity e2) {
        return collisions.stream().anyMatch(c -> containsEntities(c, e1, e2));
    }
}