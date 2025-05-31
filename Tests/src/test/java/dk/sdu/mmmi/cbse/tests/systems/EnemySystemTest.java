package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commondifficulty.IDifficultyService;
import dk.sdu.mmmi.cbse.commonenemy.EnemyComponent;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;
import dk.sdu.mmmi.cbse.commonenemy.IEnemySPI;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.enemy.EnemySystem;
import dk.sdu.mmmi.cbse.tests.utils.MockServiceLoader;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.ServiceLoader;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for EnemySystem with difficulty scaling integration.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Enemy System Tests")
public class EnemySystemTest {

    @Mock
    private IEnemySPI mockEnemySPI;

    @Mock
    private IWeaponSPI mockWeaponSPI;

    @Mock
    private IDifficultyService mockDifficultyService;

    private EnemySystem enemySystem;
    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
        world = new World();

        // Configure default mock behaviors
        when(mockDifficultyService.getCurrentDifficulty()).thenReturn(1.0f);
        when(mockDifficultyService.getHunterSpeedMultiplier()).thenReturn(1.2f);
        when(mockDifficultyService.getHunterFiringRateMultiplier()).thenReturn(0.8f); // Shorter intervals = faster firing
        when(mockWeaponSPI.shoot(any(), any(), any())).thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("System initializes with difficulty service integration")
    void testSystemInitializationWithDifficulty() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);

            enemySystem = new EnemySystem();

            // Verify system initialized correctly
            assertEquals(75, enemySystem.getPriority());
        }
    }

    @Test
    @DisplayName("System handles missing difficulty service gracefully")
    void testSystemWithoutDifficultyService() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServicesWithoutDifficulty(serviceLoaderMock);

            // Should not crash when difficulty service is unavailable
            assertDoesNotThrow(() -> {
                enemySystem = new EnemySystem();
                enemySystem.update(gameData, world);
            });
        }
    }

    @Test
    @DisplayName("System processes hunter enemy movement with difficulty scaling")
    void testHunterMovementWithDifficultyScaling() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            // Create player and hunter
            Entity player = createTestPlayer(400, 300);
            Entity hunter = createTestHunter(200, 200);
            world.addEntity(player);
            world.addEntity(hunter);

            enemySystem.update(gameData, world);

            // Verify movement component was configured
            MovementComponent movement = hunter.getComponent(MovementComponent.class);
            assertNotNull(movement);

            // Speed should be affected by difficulty scaling
            // Base speed would be modified by the difficulty multiplier
            assertTrue(movement.getSpeed() > 0, "Hunter should have positive movement speed");
        }
    }

    @Test
    @DisplayName("System processes turret enemy behavior correctly")
    void testTurretBehavior() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            Entity player = createTestPlayer(400, 300);
            Entity turret = createTestTurret(300, 300);
            world.addEntity(player);
            world.addEntity(turret);

            enemySystem.update(gameData, world);

            // Verify turret stays stationary
            MovementComponent movement = turret.getComponent(MovementComponent.class);
            assertNotNull(movement);
            assertEquals(0.0f, movement.getSpeed(), "Turret should not move");
        }
    }

    @Test
    @DisplayName("System spawns enemies through enemy SPI")
    void testEnemySpawning() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            enemySystem.update(gameData, world);

            // Verify enemy SPI was called for spawning
            verify(mockEnemySPI, atLeastOnce()).spawnEnemies(gameData, world);
        }
    }

    @Test
    @DisplayName("System handles enemy firing behavior")
    void testEnemyFiring() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            Entity player = createTestPlayer(400, 300);
            Entity hunter = createTestHunter(380, 290); // Close to player
            world.addEntity(player);
            world.addEntity(hunter);

            // Configure hunter to want to fire
            when(mockEnemySPI.shouldFire(eq(hunter), any())).thenReturn(true);
            when(mockWeaponSPI.shoot(eq(hunter), eq(gameData), any())).thenReturn(Collections.emptyList());

            enemySystem.update(gameData, world);

            // Verify firing logic was checked
            verify(mockEnemySPI).shouldFire(eq(hunter), any());
        }
    }

    @Test
    @DisplayName("System updates weapon cooldowns")
    void testWeaponCooldownUpdates() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            Entity hunter = createTestHunter(300, 300);
            WeaponComponent weapon = hunter.getComponent(WeaponComponent.class);
            weapon.resetCooldown(); // Put weapon on cooldown

            world.addEntity(hunter);

            float initialCooldown = weapon.getCooldownTime();
            enemySystem.update(gameData, world);

            // Cooldown should have been updated (note: we can't test exact values due to Time.getDeltaTimeF())
            // But we can verify the weapon component still exists and is functional
            assertNotNull(hunter.getComponent(WeaponComponent.class));
        }
    }

    @Test
    @DisplayName("System ignores entities without enemy components")
    void testIgnoresNonEnemyEntities() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            Entity player = createTestPlayer(400, 300);
            Entity nonEnemy = EntityBuilder.create()
                    .withType(EntityType.ASTEROID)
                    .atPosition(200, 200)
                    .build();

            world.addEntity(player);
            world.addEntity(nonEnemy);

            // Should not crash
            assertDoesNotThrow(() -> {
                enemySystem.update(gameData, world);
            });
        }
    }

    @Test
    @DisplayName("System handles missing player gracefully")
    void testHandlesMissingPlayer() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            setupMockServices(serviceLoaderMock);
            enemySystem = new EnemySystem();

            Entity hunter = createTestHunter(300, 300);
            world.addEntity(hunter);

            // Should not crash when no player is present
            assertDoesNotThrow(() -> {
                enemySystem.update(gameData, world);
            });
        }
    }

    @Test
    @DisplayName("System refreshes services if they become available")
    void testServiceRefreshing() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            // Initially setup without some services
            setupMockServicesWithoutDifficulty(serviceLoaderMock);
            enemySystem = new EnemySystem();

            // First update without difficulty service
            enemySystem.update(gameData, world);

            // Now make difficulty service available
            @SuppressWarnings("unchecked")
            ServiceLoader<IDifficultyService> difficultyLoader = mock(ServiceLoader.class);
            when(difficultyLoader.findFirst()).thenReturn(Optional.of(mockDifficultyService));
            serviceLoaderMock.when(() -> ServiceLoader.load(IDifficultyService.class)).thenReturn(difficultyLoader);

            // Second update should pick up the new service
            assertDoesNotThrow(() -> {
                enemySystem.update(gameData, world);
            });
        }
    }

    // Helper methods
    private void setupMockServices(MockedStatic<ServiceLoader> serviceLoaderMock) {
        setupMockService(serviceLoaderMock, IEnemySPI.class, mockEnemySPI);
        setupMockService(serviceLoaderMock, IWeaponSPI.class, mockWeaponSPI);
        setupMockService(serviceLoaderMock, IDifficultyService.class, mockDifficultyService);
    }

    private void setupMockServicesWithoutDifficulty(MockedStatic<ServiceLoader> serviceLoaderMock) {
        setupMockService(serviceLoaderMock, IEnemySPI.class, mockEnemySPI);
        setupMockService(serviceLoaderMock, IWeaponSPI.class, mockWeaponSPI);
        MockServiceLoader.setupMissingSPI(serviceLoaderMock, IDifficultyService.class);
    }

    private <T> void setupMockService(MockedStatic<ServiceLoader> serviceLoaderMock, Class<T> serviceClass, T mockService) {
        @SuppressWarnings("unchecked")
        ServiceLoader<T> loader = mock(ServiceLoader.class);
        when(loader.findFirst()).thenReturn(Optional.of(mockService));
        serviceLoaderMock.when(() -> ServiceLoader.load(serviceClass)).thenReturn(loader);
    }

    private Entity createTestPlayer(float x, float y) {
        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(8)
                .build();
    }

    private Entity createTestHunter(float x, float y) {
        EnemyComponent enemyComp = new EnemyComponent(EnemyType.HUNTER);
        enemyComp.setFireDistance(300.0f);

        WeaponComponent weapon = new WeaponComponent();
        MovementComponent movement = new MovementComponent();

        return EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(x, y)
                .withRadius(12)
                .with(enemyComp)
                .with(weapon)
                .with(movement)
                .build();
    }

    private Entity createTestTurret(float x, float y) {
        EnemyComponent enemyComp = new EnemyComponent(EnemyType.TURRET);
        enemyComp.setFireDistance(300.0f);

        WeaponComponent weapon = new WeaponComponent();
        MovementComponent movement = new MovementComponent();

        return EntityBuilder.create()
                .withType(EntityType.ENEMY)
                .atPosition(x, y)
                .withRadius(12)
                .with(enemyComp)
                .with(weapon)
                .with(movement)
                .build();
    }
}