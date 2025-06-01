package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeaponComponent - testing weapon mechanics and state management
 */
@DisplayName("WeaponComponent Unit Tests")
class WeaponComponentTest {

    private WeaponComponent weaponComponent;

    @BeforeEach
    void setUp() {
        weaponComponent = new WeaponComponent();
    }

    @Test
    @DisplayName("Should initialize with default automatic weapon configuration")
    void shouldInitializeWithDefaultConfiguration() {
        assertEquals(Weapon.FiringPattern.AUTOMATIC, weaponComponent.getFiringPattern());
        assertEquals(10.0f, weaponComponent.getDamage(), 0.001f);
        assertEquals(8.0f, weaponComponent.getProjectileSpeed(), 0.001f);
        assertEquals(0.33f, weaponComponent.getCooldownTime(), 0.001f);
        assertTrue(weaponComponent.canFire());
        assertFalse(weaponComponent.isFiring());
    }

    @Test
    @DisplayName("Should configure weapon from weapon type correctly")
    void shouldConfigureFromWeaponType() {
        // Create a burst weapon configuration
        Weapon burstWeapon = new Weapon.Builder()
                .type(Weapon.FiringPattern.BURST)
                .damage(1.5f)
                .projectileSpeed(350.0f)
                .cooldownTime(0.8f)
                .burstCount(3)
                .burstDelay(0.1f)
                .defaultBulletType("standard")
                .build();

        weaponComponent.configureFromType(burstWeapon);

        assertEquals(Weapon.FiringPattern.BURST, weaponComponent.getFiringPattern());
        assertEquals(1.5f, weaponComponent.getDamage(), 0.001f);
        assertEquals(350.0f, weaponComponent.getProjectileSpeed(), 0.001f);
        assertEquals(0.8f, weaponComponent.getCooldownTime(), 0.001f);
        assertEquals(3, weaponComponent.getBurstCount());
        assertEquals(0.1f, weaponComponent.getBurstDelay(), 0.001f);
        assertEquals("standard", weaponComponent.getBulletType());
    }

    @Test
    @DisplayName("Should handle cooldown correctly")
    void shouldHandleCooldown() {
        // Initially can fire
        assertTrue(weaponComponent.canFire());

        // After firing, should reset cooldown
        weaponComponent.resetCooldown();
        assertFalse(weaponComponent.canFire());

        // Update cooldown partially
        weaponComponent.updateCooldown(0.15f);
        assertFalse(weaponComponent.canFire());

        // Update cooldown completely
        weaponComponent.updateCooldown(0.25f);
        assertTrue(weaponComponent.canFire());
    }

    @Test
    @DisplayName("Should handle shotgun configuration correctly")
    void shouldHandleShotgunConfiguration() {
        weaponComponent.setFiringPattern(Weapon.FiringPattern.SHOTGUN);
        weaponComponent.setShotCount(5);
        weaponComponent.setSpreadAngle(35.0f);

        assertEquals(Weapon.FiringPattern.SHOTGUN, weaponComponent.getFiringPattern());
        assertEquals(5, weaponComponent.getShotCount());
        assertEquals(35.0f, weaponComponent.getSpreadAngle(), 0.001f);
    }

    @Test
    @DisplayName("Should handle firing state management correctly")
    void shouldHandleFiringState() {
        assertFalse(weaponComponent.isFiring());

        weaponComponent.setFiring(true);
        assertTrue(weaponComponent.isFiring());

        // Test burst interruption
        weaponComponent.setFiringPattern(Weapon.FiringPattern.BURST);
        weaponComponent.startBurst();
        assertTrue(weaponComponent.isBurstInProgress());

        // Releasing fire during burst should complete it
        weaponComponent.setFiring(false);
        assertFalse(weaponComponent.isBurstInProgress());
    }
}