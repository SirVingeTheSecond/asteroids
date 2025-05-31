package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IEventListener;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonasteroid.events.AsteroidSplitEvent;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.events.EventService;
import dk.sdu.mmmi.cbse.weapon.WeaponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * System tests for game scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("End-to-End Game Tests")
class GameIntegrationTest {

    @Mock
    private IBulletSPI mockBulletSPI;

    @Mock
    private ServiceLoader<IBulletSPI> mockServiceLoader;

    private IEventService eventService;
    private IWeaponSPI weaponService;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();
        eventService = new EventService();

        // Setup mocked services
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            serviceLoaderMock.when(() -> ServiceLoader.load(IBulletSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(java.util.Optional.of(mockBulletSPI));

            weaponService = new WeaponService();
        }
    }

    @Test
    @DisplayName("Complete Player Shooting Scenario")
    void completePlayerShootingScenario() {
        // 1. Create player with weapon
        Entity player = createPlayerWithWeapon();
        world.addEntity(player);

        // 2. Mock bullet creation
        Entity bullet = createBulletEntity();
        when(mockBulletSPI.createBullet(eq(player), eq(gameData), eq("tiny")))
                .thenReturn(bullet);

        // 3. Player shoots
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        assertTrue(weapon.canFire());

        List<Entity> bullets = weaponService.shoot(player, gameData, "tiny");

        // 4. Verify complete shooting flow
        assertEquals(1, bullets.size());
        assertEquals(bullet, bullets.get(0));
        assertFalse(weapon.canFire()); // Should be on cooldown

        // 5. Simulate time passing and weapon cooling down
        weapon.updateCooldown(0.5f); // More than cooldown time
        assertTrue(weapon.canFire()); // Should be ready to fire again

        verify(mockBulletSPI).createBullet(player, gameData, "tiny");
    }

    @Test
    @DisplayName("Asteroid Split Event Handling Scenario")
    void asteroidSplitEventHandlingScenario() {
        // 1. Setup event listener
        AtomicBoolean eventReceived = new AtomicBoolean(false);
        AtomicInteger splitCount = new AtomicInteger(0);

        IEventListener<AsteroidSplitEvent> splitListener = event -> {
            eventReceived.set(true);
            splitCount.incrementAndGet();
        };

        eventService.subscribe(AsteroidSplitEvent.class, splitListener);

        // 2. Create asteroid
        Entity asteroid = createAsteroidEntity();
        world.addEntity(asteroid);

        // 3. Create bullet impact
        Vector2D bulletVelocity = new Vector2D(300, 0);
        Vector2D impactPoint = new Vector2D(100, 100);

        // 4. Trigger split event
        AsteroidSplitEvent splitEvent = new AsteroidSplitEvent(asteroid, bulletVelocity, impactPoint);
        eventService.publish(splitEvent);

        // 5. Verify event was handled
        assertTrue(eventReceived.get());
        assertEquals(1, splitCount.get());
        assertTrue(splitEvent.isBulletCaused());
        assertEquals(bulletVelocity, splitEvent.getBulletVelocity());
        assertEquals(impactPoint, splitEvent.getImpactPoint());
    }

    @Test
    @DisplayName("Multi-System Integration Scenario")
    void multiSystemIntegrationScenario() {
        // This test simulates multiple systems working together

        // 1. Create game entities
        Entity player = createPlayerWithWeapon();
        Entity asteroid = createAsteroidEntity();
        world.addEntity(player);
        world.addEntity(asteroid);

        // 2. Setup event tracking
        AtomicInteger eventsHandled = new AtomicInteger(0);
        IEventListener<AsteroidSplitEvent> eventCounter = event -> eventsHandled.incrementAndGet();
        eventService.subscribe(AsteroidSplitEvent.class, eventCounter);

        // 3. Mock bullet creation
        Entity bullet = createBulletEntity();
        when(mockBulletSPI.createBullet(any(), any(), any())).thenReturn(bullet);

        // 4. Execute game sequence
        // Player shoots
        List<Entity> bullets = weaponService.shoot(player, gameData, "tiny");
        assertEquals(1, bullets.size());
        world.addEntity(bullets.get(0));

        // Simulate bullet hitting asteroid
        AsteroidSplitEvent splitEvent = new AsteroidSplitEvent(asteroid);
        eventService.publish(splitEvent);

        // 5. Verify multi-system state
        assertEquals(3, world.getEntities().size()); // Player, asteroid, bullet
        assertEquals(1, eventsHandled.get());

        // Weapon should be on cooldown
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        assertFalse(weapon.canFire());
    }

    @Test
    @DisplayName("Error Handling and Resilience Scenario")
    void errorHandlingAndResilienceScenario() {
        // 1. Create player with weapon
        Entity player = createPlayerWithWeapon();
        world.addEntity(player);

        // 2. Mock bullet service to fail
        when(mockBulletSPI.createBullet(any(), any(), any())).thenReturn(null);

        // 3. Attempt to shoot
        List<Entity> bullets = weaponService.shoot(player, gameData, "tiny");

        // 4. Verify graceful handling of failure
        assertTrue(bullets.isEmpty());

        // Weapon should still update cooldown despite failure
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        assertFalse(weapon.canFire());

        // 5. Test with missing weapon component
        Entity entityWithoutWeapon = new Entity();
        entityWithoutWeapon.addComponent(new TransformComponent());

        List<Entity> noBullets = weaponService.shoot(entityWithoutWeapon, gameData, "tiny");
        assertTrue(noBullets.isEmpty());
    }

    @Test
    @DisplayName("Event Service Concurrent Handling Scenario")
    void eventServiceConcurrentHandlingScenario() throws InterruptedException {
        // 1. Setup multiple event listeners
        AtomicInteger listener1Count = new AtomicInteger(0);
        AtomicInteger listener2Count = new AtomicInteger(0);
        AtomicInteger listener3Count = new AtomicInteger(0);

        IEventListener<AsteroidSplitEvent> listener1 = event -> listener1Count.incrementAndGet();
        IEventListener<AsteroidSplitEvent> listener2 = event -> listener2Count.incrementAndGet();
        IEventListener<AsteroidSplitEvent> listener3 = event -> listener3Count.incrementAndGet();

        eventService.subscribe(AsteroidSplitEvent.class, listener1);
        eventService.subscribe(AsteroidSplitEvent.class, listener2);
        eventService.subscribe(AsteroidSplitEvent.class, listener3);

        // 2. Create test asteroid
        Entity asteroid = createAsteroidEntity();

        // 3. Publish multiple events rapidly
        int eventCount = 10;
        for (int i = 0; i < eventCount; i++) {
            AsteroidSplitEvent event = new AsteroidSplitEvent(asteroid);
            eventService.publish(event);
        }

        // 4. Verify all listeners received all events
        assertEquals(eventCount, listener1Count.get());
        assertEquals(eventCount, listener2Count.get());
        assertEquals(eventCount, listener3Count.get());

        // 5. Test unsubscription
        eventService.unsubscribe(AsteroidSplitEvent.class, listener2);

        AsteroidSplitEvent finalEvent = new AsteroidSplitEvent(asteroid);
        eventService.publish(finalEvent);

        // Verify listener2 no longer receives events
        assertEquals(eventCount + 1, listener1Count.get());
        assertEquals(eventCount, listener2Count.get()); // Should not increment
        assertEquals(eventCount + 1, listener3Count.get());
    }

    private Entity createPlayerWithWeapon() {
        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(100, 100));
        player.addComponent(transform);

        // Weapon
        WeaponComponent weapon = new WeaponComponent();
        weapon.setFiringPattern(dk.sdu.mmmi.cbse.commonweapon.Weapon.FiringPattern.AUTOMATIC);
        weapon.setBulletType("tiny");
        weapon.setCooldownTime(0.2f);
        player.addComponent(weapon);

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        player.addComponent(tag);

        return player;
    }

    private Entity createBulletEntity() {
        Entity bullet = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(110, 100));
        transform.setRadius(2);
        bullet.addComponent(transform);

        // Bullet component
        BulletComponent bulletComp = new BulletComponent(
                UUID.randomUUID(),
                BulletComponent.BulletSource.PLAYER
        );
        bullet.addComponent(bulletComp);

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.BULLET);
        bullet.addComponent(tag);

        return bullet;
    }

    private Entity createAsteroidEntity() {
        Entity asteroid = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(200, 200));
        transform.setRadius(25);
        asteroid.addComponent(transform);

        // Asteroid component
        AsteroidComponent asteroidComp = new AsteroidComponent(AsteroidSize.LARGE);
        asteroid.addComponent(asteroidComp);

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.ASTEROID);
        asteroid.addComponent(tag);

        return asteroid;
    }
}