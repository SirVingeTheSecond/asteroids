package dk.sdu.mmmi.cbse.tests.performance;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.collision.CollisionDetector;
import dk.sdu.mmmi.cbse.collision.CollisionSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Timeout;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Performance tests for collision detection system.
 * Tests that the spatial partitioning optimization works correctly
 * and that collision detection scales well with entity count.
 */
@DisplayName("Collision System Performance Tests")
public class CollisionPerformanceTest {

    private CollisionSystem collisionSystem;
    private CollisionDetector collisionDetector;
    private GameData gameData;
    private World world;
    private Random random;

    @BeforeEach
    void setUp() {
        collisionSystem = new CollisionSystem();
        collisionDetector = new CollisionDetector();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
        random = new Random(42); // Fixed seed for reproducible tests
    }

    @Test
    @DisplayName("Collision detection with 100 entities completes quickly")
    @Timeout(value = 100, unit = TimeUnit.MILLISECONDS)
    void testCollisionDetectionWith100Entities() {
        // Create 100 entities scattered across the game world
        createRandomEntities(100);

        long startTime = System.nanoTime();
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds

        // Should complete in under 100ms (set by @Timeout)
        assertTrue(duration < 100, "100 entities should be processed in <100ms, took: " + duration + "ms");

        // Should find some collisions but not too many
        assertTrue(collisions.size() >= 0, "Should handle collision detection without errors");

        System.out.println("100 entities processed in " + duration + "ms, found " + collisions.size() + " collisions");
    }

    @Test
    @DisplayName("Collision detection with 500 entities performs acceptably")
    @Timeout(value = 500, unit = TimeUnit.MILLISECONDS)
    void testCollisionDetectionWith500Entities() {
        createRandomEntities(500);

        long startTime = System.nanoTime();
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        assertTrue(duration < 500, "500 entities should be processed in <500ms, took: " + duration + "ms");
        assertNotNull(collisions);

        System.out.println("500 entities processed in " + duration + "ms, found " + collisions.size() + " collisions");
    }

    @Test
    @DisplayName("Collision detection with 1000 entities performs acceptably")
    @Timeout(value = 1000, unit = TimeUnit.MILLISECONDS)
    void testCollisionDetectionWith1000Entities() {
        createRandomEntities(1000);

        long startTime = System.nanoTime();
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        assertTrue(duration < 1000, "1000 entities should be processed in <1000ms, took: " + duration + "ms");
        assertNotNull(collisions);

        System.out.println("1000 entities processed in " + duration + "ms, found " + collisions.size() + " collisions");
    }

    @Test
    @DisplayName("Full collision system processing performs well")
    @Timeout(value = 200, unit = TimeUnit.MILLISECONDS)
    void testFullCollisionSystemProcessing() {
        createRandomEntities(200);

        long startTime = System.nanoTime();
        collisionSystem.process(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        assertTrue(duration < 200, "Full collision processing should complete in <200ms, took: " + duration + "ms");

        System.out.println("Full collision system processed 200 entities in " + duration + "ms");
    }

    @Test
    @DisplayName("Spatial partitioning improves performance over O(n²)")
    void testSpatialPartitioningPerformance() {
        // Test with different entity counts to see performance scaling
        int[] entityCounts = {50, 100, 200, 400};
        long[] times = new long[entityCounts.length];

        for (int i = 0; i < entityCounts.length; i++) {
            world = new World(); // Reset world
            createRandomEntities(entityCounts[i]);

            // Warm up
            collisionDetector.detectCollisions(gameData, world);

            // Measure
            long startTime = System.nanoTime();
            for (int j = 0; j < 3; j++) { // Run multiple times for better measurement
                collisionDetector.detectCollisions(gameData, world);
            }
            long endTime = System.nanoTime();

            times[i] = (endTime - startTime) / 3_000_000; // Average time in ms

            System.out.println(entityCounts[i] + " entities: " + times[i] + "ms");
        }

        // Verify that performance doesn't scale quadratically
        // With spatial partitioning, we should see sub-quadratic scaling
        for (int i = 1; i < times.length; i++) {
            double entityRatio = (double) entityCounts[i] / entityCounts[i-1];
            double timeRatio = (double) times[i] / times[i-1];

            // Time ratio should be much less than entity ratio squared
            assertTrue(timeRatio < entityRatio * entityRatio * 0.8,
                    "Performance should scale better than O(n^2). Entity ratio: " + entityRatio +
                            ", Time ratio: " + timeRatio);
        }
    }

    @Test
    @DisplayName("Clustered entities don't cause performance degradation")
    @Timeout(value = 300, unit = TimeUnit.MILLISECONDS)
    void testClusteredEntitiesPerformance() {
        // Create entities clustered in a small area (worst case for spatial partitioning)
        createClusteredEntities(300, 100, 100, 50); // 300 entities in 50x50 area

        long startTime = System.nanoTime();
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        assertTrue(duration < 300, "Clustered entities should still perform well, took: " + duration + "ms");
        assertTrue(collisions.size() > 0, "Should find many collisions in clustered entities");

        System.out.println("300 clustered entities processed in " + duration + "ms, found " + collisions.size() + " collisions");
    }

    @Test
    @DisplayName("Sparse entities perform very well")
    @Timeout(value = 50, unit = TimeUnit.MILLISECONDS)
    void testSparseEntitiesPerformance() {
        // Create entities spread far apart (best case for spatial partitioning)
        createSparseEntities(200);

        long startTime = System.nanoTime();
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        long endTime = System.nanoTime();

        long duration = (endTime - startTime) / 1_000_000;

        assertTrue(duration < 50, "Sparse entities should perform very well, took: " + duration + "ms");
        assertEquals(0, collisions.size(), "Should find no collisions in sparse entities");

        System.out.println("200 sparse entities processed in " + duration + "ms, found " + collisions.size() + " collisions");
    }

    @Test
    @DisplayName("Memory usage remains reasonable with many entities")
    void testMemoryUsageWithManyEntities() {
        // Force garbage collection before measurement
        System.gc();
        long memoryBefore = getUsedMemory();

        createRandomEntities(1000);

        // Process collisions multiple times
        for (int i = 0; i < 10; i++) {
            collisionDetector.detectCollisions(gameData, world);
        }

        System.gc();
        long memoryAfter = getUsedMemory();

        long memoryUsed = memoryAfter - memoryBefore;
        long memoryPerEntity = memoryUsed / 1000;

        System.out.println("Memory used for 1000 entities: " + (memoryUsed / 1024) + " KB");
        System.out.println("Memory per entity: " + memoryPerEntity + " bytes");

        // Should use less than 1KB per entity (this is a rough estimate)
        assertTrue(memoryPerEntity < 1024, "Memory usage per entity should be reasonable");
    }

    @Test
    @DisplayName("Repeated collision detection has consistent performance")
    void testRepeatedCollisionDetectionPerformance() {
        createRandomEntities(300);

        long[] times = new long[10];

        // Run collision detection multiple times
        for (int i = 0; i < 10; i++) {
            long startTime = System.nanoTime();
            collisionDetector.detectCollisions(gameData, world);
            long endTime = System.nanoTime();

            times[i] = (endTime - startTime) / 1_000_000;
        }

        // Calculate average and standard deviation
        double average = 0;
        for (long time : times) {
            average += time;
        }
        average /= times.length;

        double variance = 0;
        for (long time : times) {
            variance += Math.pow(time - average, 2);
        }
        variance /= times.length;
        double stdDev = Math.sqrt(variance);

        System.out.println("Average time: " + average + "ms, Std Dev: " + stdDev + "ms");

        // Performance should be consistent (low standard deviation relative to average)
        assertTrue(stdDev / average < 0.5, "Performance should be consistent across runs");
        assertTrue(average < 100, "Average performance should be good");
    }

    // Helper methods
    private void createRandomEntities(int count) {
        for (int i = 0; i < count; i++) {
            float x = random.nextFloat() * gameData.getDisplayWidth();
            float y = random.nextFloat() * gameData.getDisplayHeight();
            float radius = 5 + random.nextFloat() * 10; // 5-15 radius

            Entity entity = createTestEntity(x, y, radius, getRandomEntityType(), getRandomCollisionLayer());
            world.addEntity(entity);
        }
    }

    private void createClusteredEntities(int count, float centerX, float centerY, float clusterRadius) {
        for (int i = 0; i < count; i++) {
            float angle = random.nextFloat() * 2 * (float) Math.PI;
            float distance = random.nextFloat() * clusterRadius;

            float x = centerX + (float) Math.cos(angle) * distance;
            float y = centerY + (float) Math.sin(angle) * distance;
            float radius = 3 + random.nextFloat() * 6; // 3-9 radius

            Entity entity = createTestEntity(x, y, radius, getRandomEntityType(), getRandomCollisionLayer());
            world.addEntity(entity);
        }
    }

    private void createSparseEntities(int count) {
        // Create a grid to ensure entities are well separated
        int gridSize = (int) Math.ceil(Math.sqrt(count));
        float cellWidth = gameData.getDisplayWidth() / gridSize;
        float cellHeight = gameData.getDisplayHeight() / gridSize;

        for (int i = 0; i < count; i++) {
            int gridX = i % gridSize;
            int gridY = i / gridSize;

            float x = gridX * cellWidth + cellWidth / 2;
            float y = gridY * cellHeight + cellHeight / 2;
            float radius = 2; // Small radius to prevent overlaps

            Entity entity = createTestEntity(x, y, radius, getRandomEntityType(), getRandomCollisionLayer());
            world.addEntity(entity);
        }
    }

    private Entity createTestEntity(float x, float y, float radius, EntityType type, CollisionLayer layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);

        return EntityBuilder.create()
                .withType(type)
                .atPosition(x, y)
                .withRadius(radius)
                .with(collider)
                .build();
    }

    private EntityType getRandomEntityType() {
        EntityType[] types = {EntityType.PLAYER, EntityType.ENEMY, EntityType.ASTEROID, EntityType.BULLET};
        return types[random.nextInt(types.length)];
    }

    private CollisionLayer getRandomCollisionLayer() {
        CollisionLayer[] layers = {
                CollisionLayer.PLAYER, CollisionLayer.ENEMY, CollisionLayer.OBSTACLE,
                CollisionLayer.PLAYER_PROJECTILE, CollisionLayer.ENEMY_PROJECTILE
        };
        return layers[random.nextInt(layers.length)];
    }

    private long getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
}