package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerComponent.
 */
@DisplayName("Player Component Tests")
public class PlayerComponentTest {

    private PlayerComponent player;

    @BeforeEach
    void setUp() {
        player = new PlayerComponent();
    }

    @Test
    @DisplayName("Player initializes with correct default values")
    void testInitialPlayerState() {
        assertEquals(3, player.getLives());
        assertEquals(3, player.getCurrentHealth());
        assertEquals(3, player.getMaxHealth());
        assertFalse(player.isInvulnerable());
        assertFalse(player.needsRespawn());
        assertTrue(player.isAtFullHealth());
        assertEquals(1.0f, player.getHealthPercentage(), 0.01f);
    }

    @Test
    @DisplayName("Player takes damage but doesn't die")
    void testPlayerDamageWithoutDeath() {
        // Player takes damage but doesn't die
        boolean died = player.takeDamage(1);

        assertFalse(died, "Player should not die from non-fatal damage");
        assertEquals(2, player.getCurrentHealth());
        assertEquals(3, player.getLives(), "Lives should remain unchanged for non-fatal damage");
        assertTrue(player.isInvulnerable(), "Player should be invulnerable after taking non-fatal damage");
        assertFalse(player.isAtFullHealth());
        assertFalse(player.needsRespawn(), "Player should not need respawn for non-fatal damage");
        assertEquals(0.67f, player.getHealthPercentage(), 0.01f);
    }

    @Test
    @DisplayName("Player death triggers respawn mechanics correctly")
    void testPlayerDeathAndRespawnMechanics() {
        // Damage player to death (3 damage when health is 3)
        boolean died = player.takeDamage(3);

        assertFalse(died, "Player should not die immediately - should trigger respawn");
        assertEquals(0, player.getCurrentHealth(), "Health should be 0 after fatal damage");
        assertEquals(2, player.getLives(), "Lives should decrease by 1");
        assertTrue(player.needsRespawn(), "Player should need respawn after death");

        assertFalse(player.isInvulnerable(), "Player is NOT invulnerable immediately after death");

        // Complete the respawn process
        player.completeRespawn();

        assertFalse(player.needsRespawn(), "Respawn flag should be cleared");
        assertEquals(3, player.getCurrentHealth(), "Health should be restored to max");
        assertTrue(player.isInvulnerable(), "Player should be invulnerable after respawn completion");
        assertTrue(player.isAtFullHealth());
    }

    @Test
    @DisplayName("Player final death when no lives remain")
    void testPlayerFinalDeath() {
        // Reduce to 1 life
        player.setLives(1);

        // Take fatal damage
        boolean died = player.takeDamage(3);

        assertTrue(died, "Player should die when no lives remain");
        assertEquals(0, player.getCurrentHealth());
        assertEquals(0, player.getLives());
        assertFalse(player.needsRespawn(), "Dead player should not need respawn");
    }

    @Test
    @DisplayName("Invulnerability prevents damage")
    void testInvulnerabilityPreventssDamage() {
        player.setInvulnerable(true);

        boolean died = player.takeDamage(5);

        assertFalse(died, "Invulnerable player should not die");
        assertEquals(3, player.getCurrentHealth(), "Invulnerable player should take no damage");
        assertEquals(3, player.getLives(), "Invulnerable player should lose no lives");
    }

    @Test
    @DisplayName("Invulnerability timer counts down correctly")
    void testInvulnerabilityTimer() {
        // The current implementation uses frame-based timing (180 frames = 3 seconds at 60 FPS)
        player.setInvulnerable(true);
        assertTrue(player.isInvulnerable());

        // Simulate frame updates - invulnerability should last 180 frames
        for (int frame = 0; frame < 179; frame++) {
            player.updateInvulnerability();
            assertTrue(player.isInvulnerable(), "Should still be invulnerable at frame " + frame);
        }

        // After 180 frames, invulnerability should end
        player.updateInvulnerability();
        assertFalse(player.isInvulnerable(), "Invulnerability should end after 180 frames");
    }

    @Test
    @DisplayName("Health system works correctly")
    void testHealthSystem() {
        // Test setting max health
        player.setMaxHealth(5);
        assertEquals(5, player.getMaxHealth());
        assertEquals(5, player.getCurrentHealth(), "Current health should reset to max when max changes");

        // Test taking damage
        player.takeDamage(2);
        assertEquals(3, player.getCurrentHealth());
        assertFalse(player.isAtFullHealth());
        assertEquals(0.6f, player.getHealthPercentage(), 0.01f);

        // Test healing
        player.heal();
        assertEquals(5, player.getCurrentHealth(), "Heal should restore to max health");
        assertTrue(player.isAtFullHealth());
        assertEquals(1.0f, player.getHealthPercentage(), 0.01f);
    }

    @Test
    @DisplayName("Lives management works correctly")
    void testLivesSystem() {
        assertEquals(3, player.getLives());

        player.setLives(5);
        assertEquals(5, player.getLives());

        player.setLives(1);
        assertEquals(1, player.getLives());
    }

    @Test
    @DisplayName("Respawn mechanics work correctly")
    void testRespawnMechanics() {
        // Initially should not need respawn
        assertFalse(player.needsRespawn());

        // Take fatal damage
        player.takeDamage(3);
        assertTrue(player.needsRespawn(), "Should need respawn after death");
        assertEquals(0, player.getCurrentHealth());
        assertEquals(2, player.getLives());

        // Complete respawn
        player.completeRespawn();
        assertFalse(player.needsRespawn(), "Should not need respawn after completion");
        assertEquals(3, player.getCurrentHealth(), "Health should be restored");
        assertTrue(player.isInvulnerable(), "Should be invulnerable after respawn");
    }

    @Test
    @DisplayName("Health percentage calculation is accurate")
    void testHealthPercentageCalculation() {
        player.setMaxHealth(10);
        assertEquals(1.0f, player.getHealthPercentage(), 0.01f);

        // Take damage without triggering invulnerability by clearing it first
        player.takeDamage(3);
        assertEquals(0.7f, player.getHealthPercentage(), 0.01f); // 7/10 = 0.7

        // Clear invulnerability to allow more damage
        player.setInvulnerable(false);
        player.takeDamage(2);
        assertEquals(0.5f, player.getHealthPercentage(), 0.01f); // 5/10 = 0.5

        // Clear invulnerability again
        player.setInvulnerable(false);
        player.takeDamage(5);
        assertEquals(0.0f, player.getHealthPercentage(), 0.01f); // 0/10 = 0.0
    }

    @Test
    @DisplayName("Invulnerability can be manually controlled")
    void testManualInvulnerabilityControl() {
        assertFalse(player.isInvulnerable());

        // Manually set invulnerable
        player.setInvulnerable(true);
        assertTrue(player.isInvulnerable());

        // Should prevent damage
        player.takeDamage(1);
        assertEquals(3, player.getCurrentHealth());

        // Manually remove invulnerability
        player.setInvulnerable(false);
        assertFalse(player.isInvulnerable());

        // Should now take damage
        player.takeDamage(1);
        assertEquals(2, player.getCurrentHealth());
    }

    @Test
    @DisplayName("Multiple death and respawn cycles work correctly")
    void testMultipleDeathRespawnCycles() {
        assertEquals(3, player.getLives());

        // First death
        player.takeDamage(3);
        assertEquals(2, player.getLives());
        assertTrue(player.needsRespawn());
        player.completeRespawn();
        assertEquals(3, player.getCurrentHealth());

        // Clear invulnerability to allow second death
        player.setInvulnerable(false);

        // Second death
        player.takeDamage(3);
        assertEquals(1, player.getLives());
        assertTrue(player.needsRespawn());
        player.completeRespawn();
        assertEquals(3, player.getCurrentHealth());

        // Clear invulnerability to allow final death
        player.setInvulnerable(false);

        // Final death
        boolean died = player.takeDamage(3);
        assertEquals(0, player.getLives());
        assertTrue(died, "Player should die when no lives remain");
        assertFalse(player.needsRespawn(), "Dead player should not need respawn");
    }

    @Test
    @DisplayName("Respawn provides invulnerability frames")
    void testRespawnInvulnerabilityFrames() {
        // Kill player
        player.takeDamage(3);
        player.completeRespawn();

        // Should be invulnerable after respawn
        assertTrue(player.isInvulnerable());

        // Invulnerability should prevent damage
        player.takeDamage(1);
        assertEquals(3, player.getCurrentHealth(), "Should not take damage during respawn invulnerability");

        // Simulate invulnerability wearing off
        for (int i = 0; i < 180; i++) {
            player.updateInvulnerability();
        }
        assertFalse(player.isInvulnerable());

        // Should now take damage normally
        player.takeDamage(1);
        assertEquals(2, player.getCurrentHealth());
    }

    @Test
    @DisplayName("Edge case: Zero max health handling")
    void testZeroMaxHealthEdgeCase() {
        // This tests the edge case in getHealthPercentage when maxHealth is 0
        player.setMaxHealth(0);
        assertEquals(0, player.getMaxHealth());
        assertEquals(0, player.getCurrentHealth());
        assertEquals(0.0f, player.getHealthPercentage(), "Health percentage should be 0 when max health is 0");
    }

    @Test
    @DisplayName("Damage during invulnerability maintains invulnerable state")
    void testInvulnerabilityStateMaintenance() {
        player.setInvulnerable(true);

        // Take damage while invulnerable
        player.takeDamage(1);

        // Should still be invulnerable (damage shouldn't clear the flag)
        assertTrue(player.isInvulnerable(), "Invulnerability should be maintained when taking no damage");
        assertEquals(3, player.getCurrentHealth(), "Should take no damage while invulnerable");
    }

    @Test
    @DisplayName("Invulnerability is only set on non-fatal damage")
    void testInvulnerabilityOnlyOnNonFatalDamage() {
        // Non-fatal damage should set invulnerability
        player.takeDamage(1);
        assertTrue(player.isInvulnerable(), "Non-fatal damage should trigger invulnerability");

        // Reset for next test
        player = new PlayerComponent();

        // Fatal damage should NOT set invulnerability immediately
        player.takeDamage(3);
        assertFalse(player.isInvulnerable(), "Fatal damage does not immediately set invulnerability");
        assertTrue(player.needsRespawn(), "Fatal damage should trigger respawn need");

        // Only completeRespawn() sets invulnerability after death
        player.completeRespawn();
        assertTrue(player.isInvulnerable(), "Respawn completion should set invulnerability");
    }
}