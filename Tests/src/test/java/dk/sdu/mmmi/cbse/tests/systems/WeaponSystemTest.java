package dk.sdu.mmmi.cbse.tests.systems;

import dk.sdu.mmmi.cbse.common.data.Entity;
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

import java.util.ServiceLoader;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeaponSystem
 */
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

        weaponSystem = new WeaponSystem();
    }

    @Test
    @DisplayName("Should have correct priority for execution order")
    void shouldHaveCorrectPriority() {
        assertEquals(100, weaponSystem.getPriority());
    }

    @Test
    @DisplayName("Should update weapon cooldowns for all entities")
    void shouldUpdateWeaponCooldowns() {
        try (MockedStatic<Time> timeMock = mockStatic(Time.class)) {
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity with weapon
            Entity entity = new Entity();
            WeaponComponent weapon = new WeaponComponent();
            weapon.resetCooldown(); // Set cooldown
            entity.addComponent(weapon);
            world.addEntity(entity);

            // Verify cooldown before update
            assertFalse(weapon.canFire());

            // Update system
            weaponSystem.update(gameData, world);

            // Verify cooldown was updated (should still be on cooldown)
            assertFalse(weapon.canFire());

            // Update again with enough time to clear cooldown
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.5f);
            weaponSystem.update(gameData, world);

            // Now should be able to fire
            assertTrue(weapon.canFire());
        }
    }

    @Test
    @DisplayName("Should handle burst weapon automatic continuation")
    // ToDo: expected: <true> but was: <false>
    void shouldHandleBurstWeaponAutomaticContinuation() {
        try (MockedStatic<Time> timeMock = mockStatic(Time.class)) {
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create entity with burst weapon
            Entity entity = new Entity();
            WeaponComponent weapon = new WeaponComponent();
            weapon.setFiringPattern(Weapon.FiringPattern.BURST);
            weapon.setBurstCount(3);
            weapon.setBurstDelay(0.1f);
            weapon.startBurst();
            weapon.fireBurstShot(); // Fire first shot
            entity.addComponent(weapon);
            world.addEntity(entity);

            // Verify burst is in progress but delay not complete
            assertTrue(weapon.isBurstInProgress());
            assertFalse(weapon.isBurstDelayComplete());

            // Update system to clear burst delay
            weaponSystem.update(gameData, world);

            // Verify burst delay is now complete
            assertTrue(weapon.isBurstDelayComplete());
        }
    }

    @Test
    @DisplayName("Should handle entities without weapon components gracefully")
    void shouldHandleEntitiesWithoutWeaponComponents() {
        // Create entity without weapon
        Entity entity = new Entity();
        world.addEntity(entity);

        // Should not throw exception
        assertDoesNotThrow(() -> weaponSystem.update(gameData, world));
    }

    @Test
    @DisplayName("Should skip entities without required components")
    void shouldSkipEntitiesWithoutRequiredComponents() {
        try (MockedStatic<Time> timeMock = mockStatic(Time.class)) {
            timeMock.when(Time::getDeltaTimeF).thenReturn(0.1f);

            // Create multiple entities with different component combinations
            Entity entityWithWeapon = new Entity();
            entityWithWeapon.addComponent(new WeaponComponent());
            world.addEntity(entityWithWeapon);

            Entity entityWithoutWeapon = new Entity();
            world.addEntity(entityWithoutWeapon);

            // Should process without errors
            assertDoesNotThrow(() -> weaponSystem.update(gameData, world));
        }
    }
}