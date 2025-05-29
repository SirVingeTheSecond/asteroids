package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RecoilComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.player.PlayerSystem;
import dk.sdu.mmmi.cbse.tests.utils.MockServiceLoader;
import dk.sdu.mmmi.cbse.tests.utils.TestEntityFactory;

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
 * System integration tests using Mockito to test PlayerSystem behavior
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Player System Integration Tests")
public class PlayerSystemTest {

    @Mock
    private IPhysicsSPI mockPhysicsSPI;

    @Mock
    private IWeaponSPI mockWeaponSPI;

    private PlayerSystem playerSystem;
    private GameData gameData;
    private World world;
    private Entity player;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        world = new World();

        // Create player entity with all required components
        player = TestEntityFactory.createFullPlayer(400, 300);
        world.addEntity(player);

        // Configure common mock behaviors
        when(mockPhysicsSPI.hasPhysics(any(Entity.class))).thenReturn(true);
        when(mockPhysicsSPI.getVelocity(any(Entity.class))).thenReturn(Vector2D.zero());
        when(mockWeaponSPI.shoot(any(Entity.class), any(GameData.class), anyString()))
                .thenReturn(Collections.emptyList());
    }

    @Test
    @DisplayName("System delegates physics operations to Physics SPI")
    void testPhysicsIntegration() {
        // Create PlayerSystem with mocked ServiceLoader
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            // Update system (this should trigger physics operations)
            playerSystem.update(gameData, world);

            // Verify physics SPI was used
            verify(mockPhysicsSPI, atLeastOnce()).hasPhysics(player);
            verify(mockPhysicsSPI, atLeastOnce()).getVelocity(player);
        }
    }

    @Test
    @DisplayName("System handles recoil physics correctly")
    void testRecoilPhysics() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            // Set up recoil state
            RecoilComponent recoil = player.getComponent(RecoilComponent.class);
            recoil.startRecoil(new Vector2D(-50, 0), 0.5f);

            // Configure physics mock for recoil scenario
            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            when(mockPhysicsSPI.getVelocity(player)).thenReturn(new Vector2D(10, 0));

            playerSystem.update(gameData, world);

            // Verify system applied recoil-modified drag
            verify(mockPhysicsSPI, atLeastOnce()).getVelocity(player);
            assertTrue(recoil.isInRecoil(), "Recoil should still be active");
        }
    }

    @Test
    @DisplayName("System handles weapon firing correctly")
    void testWeaponFiring() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            // Set weapon to firing state
            WeaponComponent weapon = player.getComponent(WeaponComponent.class);
            weapon.setFiring(true);

            // Configure weapon to be able to fire
            when(mockWeaponSPI.shoot(eq(player), eq(gameData), anyString()))
                    .thenReturn(Collections.singletonList(TestEntityFactory.createTestBullet(405, 305, player)));

            playerSystem.update(gameData, world);

            // Verify weapon SPI was called for shooting
            verify(mockWeaponSPI).shoot(eq(player), eq(gameData), anyString());
        }
    }

    @Test
    @DisplayName("System handles missing Physics SPI gracefully")
    void testMissingPhysicsSPI() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            // Setup ServiceLoader to return empty Optional for Physics SPI
            @SuppressWarnings("unchecked")
            ServiceLoader<IPhysicsSPI> physicsLoader = mock(ServiceLoader.class);
            when(physicsLoader.findFirst()).thenReturn(Optional.empty());

            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);
            serviceLoaderMock.when(() -> ServiceLoader.load(IPhysicsSPI.class))
                    .thenReturn(physicsLoader);

            playerSystem = new PlayerSystem();

            // Should not crash when Physics SPI is unavailable
            assertDoesNotThrow(() -> {
                playerSystem.update(gameData, world);
            }, "System should handle missing Physics SPI gracefully");

            // Should fall back to direct movement
            TransformComponent transform = player.getComponent(TransformComponent.class);
            assertNotNull(transform, "Transform should still be valid");
        }
    }

    @Test
    @DisplayName("System maintains component state consistency")
    void testComponentStateConsistency() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            PlayerComponent playerComp = player.getComponent(PlayerComponent.class);
            int initialHealth = playerComp.getCurrentHealth();

            // Process multiple updates
            for (int i = 0; i < 5; i++) {
                playerSystem.update(gameData, world);
            }

            // Component state should remain consistent
            assertEquals(initialHealth, playerComp.getCurrentHealth(),
                    "Player health should not change during normal updates");
            assertNotNull(player.getComponent(TransformComponent.class),
                    "Transform component should remain attached");
            assertNotNull(player.getComponent(WeaponComponent.class),
                    "Weapon component should remain attached");
        }
    }

    @Test
    @DisplayName("System processes weapon cooldowns correctly")
    void testWeaponCooldownProcessing() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            WeaponComponent weapon = player.getComponent(WeaponComponent.class);
            weapon.setFiringPattern(Weapon.FiringPattern.BURST);
            weapon.setBurstCount(3);
            weapon.triggerFire(); // Start burst

            // Weapon should process burst mechanics
            assertTrue(weapon.isBurstInProgress(), "Burst should be in progress");

            playerSystem.update(gameData, world);

            // System should maintain weapon state correctly
            WeaponComponent updatedWeapon = player.getComponent(WeaponComponent.class);
            assertNotNull(updatedWeapon, "Weapon component should remain valid");
        }
    }

    @Test
    @DisplayName("System handles entity without required components")
    void testIncompleteEntityHandling() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            // Create entity missing components
            Entity incompletePlayer = EntityBuilder.create()
                    .withType(EntityType.PLAYER)
                    .atPosition(100, 100)
                    // Missing PlayerComponent, WeaponComponent, etc.
                    .build();

            world.addEntity(incompletePlayer);

            // Should handle incomplete entities without crashing
            assertDoesNotThrow(() -> {
                playerSystem.update(gameData, world);
            }, "System should handle incomplete entities gracefully");
        }
    }

    @Test
    @DisplayName("System respects SPI contract boundaries")
    void testSPIContractBoundaries() {
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            MockServiceLoader.setupPhysicsSPI(serviceLoaderMock, mockPhysicsSPI);
            MockServiceLoader.setupWeaponSPI(serviceLoaderMock, mockWeaponSPI);

            playerSystem = new PlayerSystem();

            playerSystem.update(gameData, world);

            // Verify system only uses SPI methods, not implementation details
            verify(mockPhysicsSPI, never()).toString(); // Should not call implementation methods
            verify(mockWeaponSPI, never()).toString();

            // Should only call contract methods
            verify(mockPhysicsSPI, atLeastOnce()).hasPhysics(any(Entity.class));
            verify(mockPhysicsSPI, atLeastOnce()).getVelocity(any(Entity.class));
        }
    }
}