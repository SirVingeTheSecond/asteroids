package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for WeaponComponent.
 */
public class WeaponComponentTest {

    private WeaponComponent weapon;

    @BeforeEach
    void setUp() {
        weapon = new WeaponComponent();
    }

    @Test
    void testDefaultWeaponConfiguration() {
        assertEquals(Weapon.FiringPattern.AUTOMATIC, weapon.getFiringPattern());
        assertEquals(10.0f, weapon.getDamage());
        assertEquals(8.0f, weapon.getProjectileSpeed());
        assertEquals(0.33f, weapon.getCooldownTime());
        assertEquals("standard", weapon.getBulletType());
        assertFalse(weapon.isFiring());
        assertTrue(weapon.canFire()); // Should be able to fire initially
    }

    @Test
    void testAutomaticWeaponCooldown() {
        weapon.setFiringPattern(Weapon.FiringPattern.AUTOMATIC);

        // Should be able to fire initially
        assertTrue(weapon.canFire());

        // Fire and reset cooldown
        weapon.resetCooldown();
        assertFalse(weapon.canFire()); // Should be on cooldown
        
        weapon.updateCooldown(0.33f);
        assertTrue(weapon.canFire());
    }

    @Test
    // ToDo: Fails
    void testBurstWeapon() {
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstCount(3);
        weapon.setBurstDelay(0.1f);
        weapon.setCooldownTime(1.0f);

        // Should be able to start burst
        assertTrue(weapon.canFire());
        assertFalse(weapon.isBurstInProgress());

        // Trigger burst - this starts the burst but doesn't fire the first shot
        weapon.triggerFire();
        assertTrue(weapon.isBurstInProgress());
        assertEquals(0, weapon.getCurrentBurstCount());
        assertTrue(weapon.canFire()); // Can fire first shot immediately

        // Fire first shot
        weapon.fireBurstShot();
        assertEquals(1, weapon.getCurrentBurstCount());
        assertFalse(weapon.isBurstComplete());

        // After first shot, should need to wait for burst delay
        assertFalse(weapon.canFire()); // Can't fire immediately
        assertFalse(weapon.isBurstDelayComplete()); // Burst delay should be active

        // Update burst delay
        weapon.updateCooldown(0.1f);
        assertTrue(weapon.isBurstDelayComplete());
        assertTrue(weapon.canFire()); // Should be able to fire next shot

        // Fire second shot
        weapon.fireBurstShot();
        assertEquals(2, weapon.getCurrentBurstCount());
        assertFalse(weapon.isBurstComplete());

        // Wait for delay and fire third shot
        weapon.updateCooldown(0.1f);
        assertTrue(weapon.canFire());
        weapon.fireBurstShot();
        assertEquals(3, weapon.getCurrentBurstCount());
        assertTrue(weapon.isBurstComplete());

        // Burst should be complete and on main cooldown
        assertFalse(weapon.isBurstInProgress());
        assertFalse(weapon.canFire()); // Should be on main cooldown

        // After main cooldown, should be able to start new burst
        weapon.updateCooldown(1.0f);
        assertTrue(weapon.canFire());
    }

    @Test
    void testBurstInterruption() {
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstCount(3);

        // Start burst
        weapon.triggerFire();
        assertTrue(weapon.isBurstInProgress());

        // Fire one shot
        weapon.fireBurstShot();
        assertEquals(1, weapon.getCurrentBurstCount());

        // Player releases fire button - should complete burst
        weapon.setFiring(false);
        assertFalse(weapon.isBurstInProgress()); // Should complete burst
    }

    @Test
    void testShotgunConfiguration() {
        weapon.setFiringPattern(Weapon.FiringPattern.SHOTGUN);
        weapon.setShotCount(5);
        weapon.setSpreadAngle(30.0f);

        assertEquals(5, weapon.getShotCount());
        assertEquals(30.0f, weapon.getSpreadAngle());

        // Shotgun should use normal cooldown
        assertTrue(weapon.canFire());
        weapon.resetCooldown();
        assertFalse(weapon.canFire());
    }

    @Test
    void testHeavyWeaponPattern() {
        weapon.setFiringPattern(Weapon.FiringPattern.HEAVY);
        weapon.setDamage(25.0f);
        weapon.setCooldownTime(1.2f);

        assertTrue(weapon.canFire());
        weapon.resetCooldown();
        assertFalse(weapon.canFire());

        // Heavy weapons have longer cooldown
        weapon.updateCooldown(1.2f);
        assertTrue(weapon.canFire());
    }

    @Test
    void testWeaponConfigurationFromType() {
        // Create a weapon type for testing
        Weapon burstWeapon = new Weapon.Builder()
                .type(Weapon.FiringPattern.BURST)
                .damage(15.0f)
                .projectileSpeed(400.0f)
                .cooldownTime(0.8f)
                .burstCount(3)
                .burstDelay(0.08f)
                .defaultBulletType("standard")
                .build();

        // Configure weapon from type
        weapon.configureFromType(burstWeapon);

        assertEquals(Weapon.FiringPattern.BURST, weapon.getFiringPattern());
        assertEquals(15.0f, weapon.getDamage());
        assertEquals(400.0f, weapon.getProjectileSpeed());
        assertEquals(0.8f, weapon.getCooldownTime());
        assertEquals(3, weapon.getBurstCount());
        assertEquals(0.08f, weapon.getBurstDelay());
        assertEquals("standard", weapon.getBulletType());
    }

    @Test
    void testWeaponFromConstructor() {
        Weapon heavyWeapon = new Weapon.Builder()
                .type(Weapon.FiringPattern.HEAVY)
                .damage(25.0f)
                .projectileSpeed(280.0f)
                .cooldownTime(1.2f)
                .defaultBulletType("heavy")
                .build();

        WeaponComponent heavyWeaponComponent = new WeaponComponent(heavyWeapon);

        assertEquals(Weapon.FiringPattern.HEAVY, heavyWeaponComponent.getFiringPattern());
        assertEquals(25.0f, heavyWeaponComponent.getDamage());
        assertEquals(280.0f, heavyWeaponComponent.getProjectileSpeed());
        assertEquals(1.2f, heavyWeaponComponent.getCooldownTime());
        assertEquals("heavy", heavyWeaponComponent.getBulletType());
    }

    @Test
    void testCooldownProgression() {
        weapon.resetCooldown();
        assertFalse(weapon.canFire());

        // Partial cooldown
        weapon.updateCooldown(0.1f);
        assertFalse(weapon.canFire());

        // Complete cooldown
        weapon.updateCooldown(0.25f);
        assertTrue(weapon.canFire());
    }

    @Test
    void testBurstDelayProgression() {
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstDelay(0.2f);

        weapon.triggerFire();
        weapon.fireBurstShot(); // This sets burst delay

        assertFalse(weapon.isBurstDelayComplete());
        weapon.updateCooldown(0.1f);
        assertFalse(weapon.isBurstDelayComplete());
        weapon.updateCooldown(0.1f);
        assertTrue(weapon.isBurstDelayComplete());
    }

    @Test
    void testFiringState() {
        assertFalse(weapon.isFiring());

        weapon.setFiring(true);
        assertTrue(weapon.isFiring());

        weapon.setFiring(false);
        assertFalse(weapon.isFiring());
    }

    @Test
    void testBurstTriggerOnlyWorksOnce() {
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstCount(2);

        // First trigger should start burst
        weapon.triggerFire();
        assertTrue(weapon.isBurstInProgress());

        // Second trigger while burst in progress should not restart
        int originalBurstCount = weapon.getCurrentBurstCount();
        weapon.triggerFire();
        assertEquals(originalBurstCount, weapon.getCurrentBurstCount());
    }

    @Test
    void testBurstTriggeredFlagBehavior() {
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);

        // Triggering should set the flag regardless of firing state
        weapon.triggerFire();
        // burstTriggered is set in triggerFire but is private
        // This test verifies the behavior works correctly
        assertTrue(weapon.isBurstInProgress());
    }
}