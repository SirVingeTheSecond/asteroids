package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.collision.CollisionDetector;
import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for CollisionDetector - testing collision detection algorithms
 */
@DisplayName("CollisionDetector Unit Tests")
class CollisionDetectorTest {

    private CollisionDetector collisionDetector;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        collisionDetector = new CollisionDetector();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
    }

    @Test
    @DisplayName("Should detect circle-circle collision correctly")
    void shouldDetectCircleCircleCollision() {
        // Create two entities that should collide
        Entity entity1 = createCollisionEntity(10f, 10f, 10f, CollisionLayer.PLAYER);
        Entity entity2 = createCollisionEntity(15f, 10f, 8f, CollisionLayer.ENEMY);

        // Distance between centers = 5, combined radius = 18, so they should collide
        assertTrue(collisionDetector.isColliding(entity1, entity2));

        // Move them apart
        TransformComponent transform2 = entity2.getComponent(TransformComponent.class);
        transform2.setPosition(new Vector2D(30f, 10f));

        // Distance between centers = 20, combined radius = 18, so they should not collide
        assertFalse(collisionDetector.isColliding(entity1, entity2));
    }

    @Test
    @DisplayName("Should detect bullet-polygon collision correctly")
    void shouldDetectBulletPolygonCollision() {
        // Create a polygon entity
        Entity polygon = createCollisionEntity(10f, 10f, 10f, CollisionLayer.ENEMY);
        TransformComponent polygonTransform = polygon.getComponent(TransformComponent.class);
        polygonTransform.setPolygonCoordinates(-5, -5, 5, -5, 5, 5, -5, 5); // Square

        // Create a bullet entity inside the polygon
        Entity bullet = createBulletEntity(10f, 10f, 1f);

        assertTrue(collisionDetector.isColliding(bullet, polygon));

        // Move bullet outside polygon
        TransformComponent bulletTransform = bullet.getComponent(TransformComponent.class);
        bulletTransform.setPosition(new Vector2D(20f, 10f));

        assertFalse(collisionDetector.isColliding(bullet, polygon));
    }

    @Test
    @DisplayName("Should handle entities without required components")
    void shouldHandleEntitiesWithoutRequiredComponents() {
        Entity entityWithoutTransform = new Entity();
        entityWithoutTransform.addComponent(new ColliderComponent());

        Entity entityWithoutCollider = new Entity();
        entityWithoutCollider.addComponent(new TransformComponent());

        Entity normalEntity = createCollisionEntity(10f, 10f, 5f, CollisionLayer.PLAYER);

        // Should not crash and should return false
        assertFalse(collisionDetector.isColliding(entityWithoutTransform, normalEntity));
        assertFalse(collisionDetector.isColliding(entityWithoutCollider, normalEntity));
    }

    @Test
    @DisplayName("Should detect collisions in world correctly")
    void shouldDetectCollisionsInWorld() {
        // Create colliding entities
        Entity entity1 = createCollisionEntity(10f, 10f, 10f, CollisionLayer.PLAYER);
        Entity entity2 = createCollisionEntity(15f, 10f, 8f, CollisionLayer.ENEMY);
        world.addEntity(entity1);
        world.addEntity(entity2);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);

        assertEquals(1, collisions.size());
        Pair<Entity, Entity> collision = collisions.get(0);
        assertTrue((collision.getFirst() == entity1 && collision.getSecond() == entity2) ||
                (collision.getFirst() == entity2 && collision.getSecond() == entity1));
    }

    @Test
    @DisplayName("Should not detect collisions between incompatible layers")
    void shouldNotDetectCollisionsBetweenIncompatibleLayers() {
        // Create entities with layers that cannot collide
        Entity entity1 = createCollisionEntity(10f, 10f, 10f, CollisionLayer.PLAYER);
        Entity entity2 = createCollisionEntity(15f, 10f, 8f, CollisionLayer.PLAYER_PROJECTILE);
        world.addEntity(entity1);
        world.addEntity(entity2);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);

        assertEquals(0, collisions.size());
    }

    @Test
    @DisplayName("Should handle empty world correctly")
    void shouldHandleEmptyWorld() {
        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertTrue(collisions.isEmpty());
    }

    @Test
    @DisplayName("Should handle single entity correctly")
    void shouldHandleSingleEntity() {
        Entity entity = createCollisionEntity(10f, 10f, 5f, CollisionLayer.PLAYER);
        world.addEntity(entity);

        List<Pair<Entity, Entity>> collisions = collisionDetector.detectCollisions(gameData, world);
        assertTrue(collisions.isEmpty());
    }

    private Entity createCollisionEntity(float x, float y, float radius, CollisionLayer layer) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(x, y));
        transform.setRadius(radius);
        entity.addComponent(transform);

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);
        entity.addComponent(collider);

        return entity;
    }

    private Entity createBulletEntity(float x, float y, float radius) {
        Entity bullet = createCollisionEntity(x, y, radius, CollisionLayer.PLAYER_PROJECTILE);

        TagComponent tag = new TagComponent();
        tag.addType(EntityType.BULLET);
        bullet.addComponent(tag);

        return bullet;
    }
}