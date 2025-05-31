package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.collision.BoundarySystem;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for BoundarySystem collision enforcement and wall creation.
 */
@DisplayName("Boundary System Tests")
public class BoundarySystemTest {

    private BoundarySystem boundarySystem;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        boundarySystem = new BoundarySystem();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
    }

    @Test
    @DisplayName("System creates boundary walls on start")
    void testBoundaryWallCreation() {
        int initialEntityCount = world.getEntities().size();

        boundarySystem.start(gameData, world);

        // Should have created 4 boundary walls (top, bottom, left, right)
        assertEquals(initialEntityCount + 4, world.getEntities().size());

        // Verify boundary entities have correct components
        long boundaryCount = world.getEntities().stream()
                .filter(entity -> {
                    ColliderComponent collider = entity.getComponent(ColliderComponent.class);
                    return collider != null && collider.getLayer() == CollisionLayer.BOUNDARY;
                })
                .count();

        assertEquals(4, boundaryCount, "Should have exactly 4 boundary walls");
    }

    @Test
    @DisplayName("System removes boundary walls on stop")
    void testBoundaryWallRemoval() {
        boundarySystem.start(gameData, world);
        int entitiesAfterStart = world.getEntities().size();

        boundarySystem.stop(gameData, world);

        // Should have removed all boundary walls
        long boundaryCount = world.getEntities().stream()
                .filter(entity -> {
                    ColliderComponent collider = entity.getComponent(ColliderComponent.class);
                    return collider != null && collider.getLayer() == CollisionLayer.BOUNDARY;
                })
                .count();

        assertEquals(0, boundaryCount, "Should have no boundary walls after stop");
        assertTrue(world.getEntities().size() < entitiesAfterStart, "Entity count should decrease");
    }

    @Test
    @DisplayName("System enforces boundary collision for player entities")
    void testPlayerBoundaryEnforcement() {
        Entity player = createPlayerEntity(750, 300); // Near right edge
        world.addEntity(player);

        // Move player outside boundary
        TransformComponent transform = player.getComponent(TransformComponent.class);
        transform.setPosition(new Vector2D(850, 300)); // Outside right boundary

        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        physics.setVelocity(new Vector2D(100, 0)); // Moving right

        boundarySystem.process(gameData, world);

        // Player should be clamped to boundary
        Vector2D position = transform.getPosition();
        float expectedMaxX = gameData.getDisplayWidth() - transform.getRadius();
        assertTrue(position.x() <= expectedMaxX, "Player should be clamped to right boundary");

        // Velocity should be stopped in X direction
        Vector2D velocity = physics.getVelocity();
        assertEquals(0, velocity.x(), "X velocity should be zero after hitting boundary");
    }

    @Test
    @DisplayName("System enforces boundary collision for enemy entities")
    void testEnemyBoundaryEnforcement() {
        Entity enemy = createEnemyEntity(-50, 300); // Outside left boundary
        world.addEntity(enemy);

        TransformComponent transform = enemy.getComponent(TransformComponent.class);
        PhysicsComponent physics = enemy.getComponent(PhysicsComponent.class);
        physics.setVelocity(new Vector2D(-50, 0)); // Moving left (further out)

        boundarySystem.process(gameData, world);

        // Enemy should be clamped to boundary
        Vector2D position = transform.getPosition();
        float expectedMinX = transform.getRadius();
        assertTrue(position.x() >= expectedMinX, "Enemy should be clamped to left boundary");

        // Velocity should be stopped
        Vector2D velocity = physics.getVelocity();
        assertEquals(0, velocity.x(), "X velocity should be zero after hitting boundary");
    }

    @Test
    @DisplayName("System ignores entities without required components")
    void testIgnoresIncompleteEntities() {
        // Entity missing PhysicsComponent
        Entity incompleteEntity = EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(850, 300)
                .withRadius(10)
                .with(createColliderComponent(CollisionLayer.PLAYER))
                .build();

        world.addEntity(incompleteEntity);

        // Should not crash
        assertDoesNotThrow(() -> {
            boundarySystem.process(gameData, world);
        }, "System should handle incomplete entities gracefully");

        // Entity position should remain unchanged
        TransformComponent transform = incompleteEntity.getComponent(TransformComponent.class);
        assertEquals(850, transform.getX(), "Incomplete entity position should not change");
    }

    @Test
    @DisplayName("System ignores obstacle entities (asteroids)")
    void testIgnoresObstacleEntities() {
        Entity asteroid = EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(850, 300)
                .withRadius(20)
                .with(createColliderComponent(CollisionLayer.OBSTACLE))
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();

        world.addEntity(asteroid);

        Vector2D originalPosition = asteroid.getComponent(TransformComponent.class).getPosition();

        boundarySystem.process(gameData, world);

        // Asteroid position should not be changed by boundary system
        Vector2D newPosition = asteroid.getComponent(TransformComponent.class).getPosition();
        assertEquals(originalPosition, newPosition, "Asteroid should not be affected by boundary enforcement");
    }

    @Test
    @DisplayName("System handles vertical boundary collisions correctly")
    void testVerticalBoundaryCollisions() {
        Entity player = createPlayerEntity(400, 650); // Below bottom boundary
        world.addEntity(player);

        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        physics.setVelocity(new Vector2D(0, 100)); // Moving down

        boundarySystem.process(gameData, world);

        TransformComponent transform = player.getComponent(TransformComponent.class);
        Vector2D position = transform.getPosition();
        float expectedMaxY = gameData.getDisplayHeight() - transform.getRadius();

        assertTrue(position.y() <= expectedMaxY, "Player should be clamped to bottom boundary");

        Vector2D velocity = physics.getVelocity();
        assertEquals(0, velocity.y(), "Y velocity should be zero after hitting boundary");
    }

    @Test
    @DisplayName("System handles corner collisions correctly")
    void testCornerBoundaryCollisions() {
        Entity player = createPlayerEntity(850, 650); // Outside both right and bottom
        world.addEntity(player);

        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        physics.setVelocity(new Vector2D(50, 50)); // Moving diagonally out

        boundarySystem.process(gameData, world);

        TransformComponent transform = player.getComponent(TransformComponent.class);
        Vector2D position = transform.getPosition();

        float expectedMaxX = gameData.getDisplayWidth() - transform.getRadius();
        float expectedMaxY = gameData.getDisplayHeight() - transform.getRadius();

        assertTrue(position.x() <= expectedMaxX, "Player X should be clamped to right boundary");
        assertTrue(position.y() <= expectedMaxY, "Player Y should be clamped to bottom boundary");

        Vector2D velocity = physics.getVelocity();
        assertEquals(0, velocity.x(), "X velocity should be zero");
        assertEquals(0, velocity.y(), "Y velocity should be zero");
    }

    @Test
    @DisplayName("System has correct priority for late update execution")
    void testSystemPriority() {
        int priority = boundarySystem.getPriority();
        assertEquals(150, priority, "BoundarySystem should have priority 150");
    }

    // Helper methods
    private Entity createPlayerEntity(float x, float y) {
        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(8)
                .with(createColliderComponent(CollisionLayer.PLAYER))
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();
    }

    private Entity createEnemyEntity(float x, float y) {
        return EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(x, y)
                .withRadius(12)
                .with(createColliderComponent(CollisionLayer.ENEMY))
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();
    }

    private ColliderComponent createColliderComponent(CollisionLayer layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);
        return collider;
    }
}