package dk.sdu.mmmi.cbse.tests.utils;

import dk.sdu.mmmi.cbse.common.components.RecoilComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.UUID;

/**
 * Enhanced factory for creating test entities with proper CBSE component composition.
 */
public class TestEntityFactory {

    /**
     * Create a fully configured player entity for testing
     */
    public static Entity createFullPlayer(float x, float y) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);

        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(8.0f)
                .with(new PlayerComponent())
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .with(new WeaponComponent())
                .with(new RecoilComponent()) // Important for PlayerSystem tests
                .with(collider)
                .with(new CollisionResponseComponent())
                .build();
    }

    /**
     * Create minimal player for component testing
     */
    public static Entity createMinimalPlayer(float x, float y) {
        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(8.0f)
                .with(new PlayerComponent())
                .build();
    }

    /**
     * Create test asteroid with specified size
     */
    public static Entity createTestAsteroid(float x, float y, AsteroidSize size) {
        float radius = switch (size) {
            case LARGE -> 25.0f;
            case MEDIUM -> 15.0f;
            case SMALL -> 8.0f;
        };

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);

        return EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(x, y)
                .withRadius(radius)
                .with(new AsteroidComponent(size))
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .with(collider)
                .with(new CollisionResponseComponent())
                .build();
    }

    /**
     * Create test bullet
     */
    public static Entity createTestBullet(float x, float y, Entity shooter) {
        BulletComponent bulletComp = new BulletComponent(
                UUID.fromString(shooter.getID()),
                BulletComponent.BulletSource.PLAYER);

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER_PROJECTILE);

        return EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(x, y)
                .withRadius(3.0f)
                .with(bulletComp)
                .with(collider)
                .with(new CollisionResponseComponent())
                .build();
    }

    /**
     * Create entity with only essential components for testing
     */
    public static Entity createBasicEntity(float x, float y, float radius) {
        return EntityBuilder.create()
                .atPosition(x, y)
                .withRadius(radius)
                .build();
    }

    /**
     * Create entity with physics but no other game-specific components
     */
    public static Entity createPhysicsEntity(float x, float y) {
        return EntityBuilder.create()
                .atPosition(x, y)
                .withRadius(10.0f)
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();
    }
}