package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.weapon.WeaponSystem;
import dk.sdu.mmmi.cbse.core.utils.Time;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeaponSystem.
 */
// I have spend way too much time on this test.
@ExtendWith(MockitoExtension.class)
@DisplayName("WeaponSystem Unit Tests")
class WeaponSystemTest {

    @Mock
    private IWeaponSPI mockWeaponSPI;

    @Mock
    private ServiceLoader<IWeaponSPI> mockServiceLoader;

    private WeaponSystem weaponSystem;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        world = new World();
    }

    @Test
    @DisplayName("Should initialize with mocked WeaponSPI from ServiceLoader")
    void shouldInitializeWithMockedWeaponSPI() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            // Arrange: Mock ServiceLoader
            serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(Optional.of(mockWeaponSPI));

            // Act: Create WeaponSystem (triggers ServiceLoader.load in constructor)
            weaponSystem = new WeaponSystem();

            // Assert: Verify ServiceLoader was called during initialization
            serviceLoaderMock.verify(() -> ServiceLoader.load(IWeaponSPI.class));
            verify(mockServiceLoader).findFirst();
        }
    }

    @Test
    @DisplayName("Should handle missing WeaponSPI gracefully")
    void shouldHandleMissingWeaponSPIGracefully() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            // Arrange: Mock ServiceLoader
            serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(Optional.empty());

            // Act: Create WeaponSystem
            weaponSystem = new WeaponSystem();

            // Assert: Should not crash
            assertNotNull(weaponSystem);
            assertEquals(100, weaponSystem.getPriority());
        }
    }

    @Test
    @DisplayName("Should update weapon cooldowns for all entities with WeaponComponent")
    void shouldUpdateWeaponCooldownsForAllEntities() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.15f);

            // Create multiple entities with weapons
            Entity automaticEntity = createEntityWithWeapon(Weapon.FiringPattern.AUTOMATIC);
            Entity burstEntity = createEntityWithWeapon(Weapon.FiringPattern.BURST);
            Entity entityWithoutWeapon = new Entity(); // Should be skipped

            world.addEntity(automaticEntity);
            world.addEntity(burstEntity);
            world.addEntity(entityWithoutWeapon);

            // Put weapons on cooldown - for automatic weapons, main cooldown controls firing
            WeaponComponent automaticWeapon = automaticEntity.getComponent(WeaponComponent.class);
            WeaponComponent burstWeapon = burstEntity.getComponent(WeaponComponent.class);

            automaticWeapon.resetCooldown(); // Sets currentCooldown = 0.33f
            burstWeapon.resetCooldown(); // Sets currentCooldown = 0.33f

            // Verify initial state
            assertFalse(automaticWeapon.canFire());
            assertFalse(burstWeapon.canFire());

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify cooldowns were updated
            // After 0.15f delta time, currentCooldown becomes 0.18f, still > 0
            assertFalse(automaticWeapon.canFire());
            assertFalse(burstWeapon.canFire());

            // Verify Time.getDeltaTimeF was called
            timeMock.verify(Time::getDeltaTimeF);
        }
    }

    @Test
    @DisplayName("Should allow weapons to fire after cooldown expires")
    void shouldAllowWeaponsToFireAfterCooldownExpires() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.5f); // Large delta time

            Entity automaticEntity = createEntityWithWeapon(Weapon.FiringPattern.AUTOMATIC);
            world.addEntity(automaticEntity);

            WeaponComponent weapon = automaticEntity.getComponent(WeaponComponent.class);
            weapon.resetCooldown(); // Sets currentCooldown = 0.33f

            // Verify initial state
            assertFalse(weapon.canFire());

            // Act: Update system with large delta time
            weaponSystem.update(gameData, world);

            // Assert: Verify cooldown cleared (0.33f - 0.5f = 0, clamped to 0)
            assertTrue(weapon.canFire());
        }
    }

    @Test
    @DisplayName("Should trigger automatic burst continuation when conditions are met")
    void shouldTriggerAutomaticBurstContinuation() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity with burst weapon in progress
            Entity burstEntity = createEntityWithBurstInProgress();
            world.addEntity(burstEntity);

            // Mock bullet creation
            Entity mockBullet = createMockBullet();
            when(mockWeaponSPI.shoot(eq(burstEntity), eq(gameData), eq("standard")))
                    .thenReturn(List.of(mockBullet));

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify weapon service was called and bullet added to world
            verify(mockWeaponSPI).shoot(burstEntity, gameData, "standard");
            assertTrue(world.getEntities().contains(mockBullet));
        }
    }

    @Test
    @DisplayName("Should not trigger burst continuation when weapon cannot fire")
    void shouldNotTriggerBurstContinuationWhenCannotFire() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            // Use small delta time that won't clear the burst delay
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.01f);

            // Create entity with burst weapon that has just fired (burst delay active)
            Entity burstEntity = createEntityWithBurstDelayActive();
            world.addEntity(burstEntity);

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify weapon service was NOT called
            verify(mockWeaponSPI, never()).shoot(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Should handle lazy loading of WeaponSPI when initially null")
    void shouldHandleLazyLoadingOfWeaponSPI() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with null WeaponSPI initially
            serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst())
                    .thenReturn(Optional.empty()) // First call returns empty
                    .thenReturn(Optional.of(mockWeaponSPI)); // Second call returns mock

            weaponSystem = new WeaponSystem();
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity that would trigger lazy loading
            Entity burstEntity = createEntityWithBurstInProgress();
            world.addEntity(burstEntity);

            // Mock bullet creation for the second ServiceLoader call
            Entity mockBullet = createMockBullet();
            when(mockWeaponSPI.shoot(eq(burstEntity), eq(gameData), eq("standard")))
                    .thenReturn(List.of(mockBullet));

            // Act: Update system (should trigger lazy loading)
            weaponSystem.update(gameData, world);

            // Assert: Verify ServiceLoader was called twice (constructor + lazy load)
            serviceLoaderMock.verify(() -> ServiceLoader.load(IWeaponSPI.class), times(2));
            verify(mockServiceLoader, times(2)).findFirst();
            verify(mockWeaponSPI).shoot(burstEntity, gameData, "standard");
        }
    }

    @Test
    @DisplayName("Should not crash when WeaponSPI remains null during burst continuation")
    void shouldNotCrashWhenWeaponSPIRemainsNull() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with permanently null WeaponSPI
            serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(Optional.empty());

            weaponSystem = new WeaponSystem();
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity that would trigger burst continuation
            Entity burstEntity = createEntityWithBurstInProgress();
            world.addEntity(burstEntity);

            // Act & Assert: Should not throw exception
            assertDoesNotThrow(() -> weaponSystem.update(gameData, world));

            // Verify no interaction with weapon service (since it's null)
            verify(mockWeaponSPI, never()).shoot(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Should add multiple bullets to world when burst creates multiple bullets")
    void shouldAddMultipleBulletsToWorldWhenBurstCreatesMultiple() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            Entity burstEntity = createEntityWithBurstInProgress();
            world.addEntity(burstEntity);

            // Mock multiple bullet creation (like shotgun burst)
            Entity bullet1 = createMockBullet();
            Entity bullet2 = createMockBullet();
            Entity bullet3 = createMockBullet();
            when(mockWeaponSPI.shoot(eq(burstEntity), eq(gameData), eq("standard")))
                    .thenReturn(List.of(bullet1, bullet2, bullet3));

            int initialEntityCount = world.getEntities().size();

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify all bullets were added to world
            assertEquals(initialEntityCount + 3, world.getEntities().size());
            assertTrue(world.getEntities().contains(bullet1));
            assertTrue(world.getEntities().contains(bullet2));
            assertTrue(world.getEntities().contains(bullet3));

            verify(mockWeaponSPI).shoot(burstEntity, gameData, "standard");
        }
    }

    @Test
    @DisplayName("Should not trigger burst continuation when burst is complete")
    void shouldNotTriggerBurstContinuationWhenBurstComplete() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity with completed burst
            Entity burstEntity = createEntityWithCompleteBurst();
            world.addEntity(burstEntity);

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify weapon service was NOT called
            verify(mockWeaponSPI, never()).shoot(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Should not process burst continuation for non-burst weapons")
    void shouldNotProcessBurstContinuationForNonBurstWeapons() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entities with non-burst weapons
            Entity automaticEntity = createEntityWithWeapon(Weapon.FiringPattern.AUTOMATIC);
            Entity heavyEntity = createEntityWithWeapon(Weapon.FiringPattern.HEAVY);
            Entity shotgunEntity = createEntityWithWeapon(Weapon.FiringPattern.SHOTGUN);

            world.addEntity(automaticEntity);
            world.addEntity(heavyEntity);
            world.addEntity(shotgunEntity);

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: No burst continuation should happen for non-burst weapons
            verify(mockWeaponSPI, never()).shoot(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Should not trigger burst continuation when burst is not in progress")
    void shouldNotTriggerBurstContinuationWhenBurstNotInProgress() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class);
             MockedStatic<Time> timeMock = mockStatic(Time.class)) {

            // Arrange: Initialize WeaponSystem with mock
            setupMockedWeaponSystem(serviceLoaderMock);
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity with burst weapon not in progress
            Entity burstEntity = createEntityWithWeapon(Weapon.FiringPattern.BURST);
            world.addEntity(burstEntity);

            // Act: Update system
            weaponSystem.update(gameData, world);

            // Assert: Verify weapon service was NOT called
            verify(mockWeaponSPI, never()).shoot(any(), any(), any());
        }
    }

    @Test
    @DisplayName("Should have correct priority for execution order")
    void shouldHaveCorrectPriorityForExecutionOrder() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockedWeaponSystem(serviceLoaderMock);
            assertEquals(100, weaponSystem.getPriority());
        }
    }

    // === Helper Methods ===

    private void setupMockedWeaponSystem(MockedStatic<ServiceLoader> serviceLoaderMock) {
        serviceLoaderMock.when(() -> ServiceLoader.load(IWeaponSPI.class))
                .thenReturn(mockServiceLoader);
        when(mockServiceLoader.findFirst()).thenReturn(Optional.of(mockWeaponSPI));
        weaponSystem = new WeaponSystem();
    }

    private Entity createEntityWithWeapon(Weapon.FiringPattern pattern) {
        Entity entity = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(100, 100));
        entity.addComponent(transform);

        WeaponComponent weapon = new WeaponComponent();
        weapon.setFiringPattern(pattern);
        weapon.setBulletType("standard");
        entity.addComponent(weapon);

        return entity;
    }

    private Entity createEntityWithBurstInProgress() {
        Entity entity = createEntityWithWeapon(Weapon.FiringPattern.BURST);
        WeaponComponent weapon = entity.getComponent(WeaponComponent.class);

        // Set up burst in progress conditions where weapon can fire
        weapon.startBurst(); // Start burst (burstInProgress = true, currentBurstCount = 0, currentBurstDelay = 0)
        weapon.fireBurstShot(); // Fire first shot (currentBurstCount = 1, currentBurstDelay = burstDelay)

        // At this point:
        // - burstInProgress = true
        // - currentBurstCount = 1, burstCount = 3 (so !isBurstComplete())
        // - currentBurstDelay = 0.083f (burstDelay)
        // After updateCooldown(0.1f), currentBurstDelay will become 0, so canFire() = true

        return entity;
    }

    private Entity createEntityWithBurstDelayActive() {
        Entity entity = createEntityWithWeapon(Weapon.FiringPattern.BURST);
        WeaponComponent weapon = entity.getComponent(WeaponComponent.class);

        // Set up burst with active delay that won't be cleared by small delta time
        weapon.startBurst();
        weapon.fireBurstShot();

        return entity;
    }

    private Entity createEntityWithCompleteBurst() {
        Entity entity = createEntityWithWeapon(Weapon.FiringPattern.BURST);
        WeaponComponent weapon = entity.getComponent(WeaponComponent.class);

        // Set up completed burst
        weapon.startBurst();
        // Fire all shots in burst (default burstCount = 3)
        weapon.fireBurstShot(); // Shot 1
        weapon.fireBurstShot(); // Shot 2
        weapon.fireBurstShot(); // Shot 3 - burst complete

        // At this point:
        // - burstInProgress = false (completeBurst() was called)
        // - isBurstComplete() = true
        // - currentCooldown = cooldownTime (main cooldown active)

        return entity;
    }

    private Entity createMockBullet() {
        Entity bullet = new Entity();

        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(110, 100));
        bullet.addComponent(transform);

        TagComponent tag = new TagComponent();
        tag.addType(EntityType.BULLET);
        bullet.addComponent(tag);

        return bullet;
    }
}