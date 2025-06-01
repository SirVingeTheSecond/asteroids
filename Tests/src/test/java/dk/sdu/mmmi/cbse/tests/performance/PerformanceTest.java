package dk.sdu.mmmi.cbse.tests.performance;

import dk.sdu.mmmi.cbse.collision.CollisionDetector;
import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Performance tests for game systems.
 * These are somewhat qualitative tests as an example and should are not really representative of actual performance.
 */
@DisplayName("Performance Tests")
class PerformanceTest {

    private CollisionDetector collisionDetector;
    private GameData gameData;
    private World world;
    private Random random;

    @BeforeEach
    void setUp() {
        collisionDetector = new CollisionDetector();
        gameData = new GameData();
        gameData.setDisplayWidth(1920);
        gameData.setDisplayHeight(1080);
        world = new World();
        random = new Random(42); // Fixed seed for reproducible tests
    }

    @Test
    @DisplayName("Collision detection should scale efficiently with entity count")
    @Timeout(value = 5, unit = TimeUnit.SECONDS)
    void collisionDetectionShouldScaleEfficiently() {
        // Test with increasing entity counts
        int[] entityCounts = {10, 50, 100, 200, 500};

        for (int count : entityCounts) {
            world = new World(); // Reset world

            // Create entities
            for (int i = 0; i < count; i++) {
                Entity entity = createRandomEntity();
                world.addEntity(entity);
            }

            // Measure collision detection time
            long startTime = System.nanoTime();
            List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
            long endTime = System.nanoTime();

            long durationMs = (endTime - startTime) / 1_000_000;

            // Should complete within reasonable time (scaled by entity count)
            // With spatial partitioning, this should be roughly O(n) instead of O(n^2)
            assertTrue(durationMs < (count * 2), // I think 2ms per entity is VERY generous for modern hardware
                    String.format("Collision detection took %dms for %d entities", durationMs, count));

            // Verify results are reasonable
            assertTrue(collisions.size() >= 0);
            assertTrue(collisions.size() < count * count / 4); // check on collision count
        }
    }

    @Test
    @DisplayName("World entity management should perform efficiently")
    @Timeout(value = 2, unit = TimeUnit.SECONDS)
    void worldEntityManagementShouldPerformEfficiently() {
        int entityCount = 10000;

        // Test performance of adding entity
        long startTime = System.nanoTime();
        for (int i = 0; i < entityCount; i++) {
            Entity entity = new Entity();
            world.addEntity(entity);
        }
        long addTime = System.nanoTime() - startTime;

        // Should add 10k entities quickly
        assertTrue(addTime / 1_000_000 < 100, // Less than 100ms
                String.format("Adding %d entities took %dms", entityCount, addTime / 1_000_000));

        // Verify count
        assertEquals(entityCount, world.getEntities().size());

        // Test entity removal performance
        Entity[] entities = world.getEntities().toArray(new Entity[0]);
        startTime = System.nanoTime();
        for (Entity entity : entities) {
            world.removeEntity(entity);
        }
        long removeTime = System.nanoTime() - startTime;

        // Should remove 10k entities quickly
        assertTrue(removeTime / 1_000_000 < 100, // Less than 100ms
                String.format("Removing %d entities took %dms", entityCount, removeTime / 1_000_000));

        // Verify removal
        assertEquals(0, world.getEntities().size());
    }

    @Test
    @DisplayName("Component access should be efficient")
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
    void componentAccessShouldBeEfficient() {
        int entityCount = 1000;
        int accessCount = 10000;

        // Create entities with components
        Entity[] entities = new Entity[entityCount];
        for (int i = 0; i < entityCount; i++) {
            entities[i] = createEntityWithComponents();
        }

        // Test component access performance
        long startTime = System.nanoTime();
        for (int i = 0; i < accessCount; i++) {
            Entity entity = entities[i % entityCount];
            TransformComponent transform = entity.getComponent(TransformComponent.class);
            assertNotNull(transform);

            // Access component data
            Vector2D position = transform.getPosition();
            float rotation = transform.getRotation();
            assertTrue(position != null && rotation >= 0);
        }
        long accessTime = System.nanoTime() - startTime;

        // Should complete component accesses quickly
        assertTrue(accessTime / 1_000_000 < 50, // Less than 50ms for 10k accesses
                String.format("%d component accesses took %dms", accessCount, accessTime / 1_000_000));
    }

    @Test
    @DisplayName("Memory usage should be reasonable for large entity counts")
    void memoryUsageShouldBeReasonableForLargeEntityCounts() {
        Runtime runtime = Runtime.getRuntime();

        // Force garbage collection and measure baseline
        runtime.gc();
        long baselineMemory = runtime.totalMemory() - runtime.freeMemory();

        // Create many entities
        int entityCount = 5000;
        for (int i = 0; i < entityCount; i++) {
            Entity entity = createEntityWithComponents();
            world.addEntity(entity);
        }

        // Measure memory after entity creation
        runtime.gc();
        long afterCreationMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryUsed = afterCreationMemory - baselineMemory;

        // Should use reasonable amount of memory per entity
        long memoryPerEntity = memoryUsed / entityCount;
        assertTrue(memoryPerEntity < 10000, // Less than 10KB per entity (very generous)
                String.format("Memory usage: %d bytes per entity", memoryPerEntity));

        // Clean up
        world = new World();
        runtime.gc();
        long afterCleanupMemory = runtime.totalMemory() - runtime.freeMemory();

        // Memory should be released (within reasonable bounds due to GC timing)
        assertTrue(afterCleanupMemory < afterCreationMemory,
                "Memory should be released after cleanup");
    }

    private Entity createRandomEntity() {
        Entity entity = new Entity();

        // Add transform at random position
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(
                random.nextFloat() * gameData.getDisplayWidth(),
                random.nextFloat() * gameData.getDisplayHeight()
        ));
        transform.setRadius(5 + random.nextFloat() * 15); // 5-20 radius
        entity.addComponent(transform);

        // Add collider
        ColliderComponent collider = new ColliderComponent();
        CollisionLayer[] layers = CollisionLayer.values();
        collider.setLayer(layers[random.nextInt(layers.length)]);
        entity.addComponent(collider);

        return entity;
    }

    private Entity createEntityWithComponents() {
        Entity entity = new Entity();

        // Add multiple components to test component management performance
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(100, 100));
        entity.addComponent(transform);

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);
        entity.addComponent(collider);

        return entity;
    }
}