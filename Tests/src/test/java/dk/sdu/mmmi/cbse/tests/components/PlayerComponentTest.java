package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PlayerComponent.
 */
public class PlayerComponentTest {

    private PlayerComponent player;

    @BeforeEach
    void setUp() {
        player = new PlayerComponent();
    }

    @Test
    void testInitialPlayerState() {
        assertEquals(3, player.getLives());
        assertEquals(3, player.getCurrentHealth());
        assertEquals(3, player.getMaxHealth());
        assertEquals(0, player.getScore());
        assertFalse(player.isInvulnerable());
        assertTrue(player.isAtFullHealth());
        assertEquals(1.0f, player.getHealthPercentage(), 0.01f);
    }

    @Test
    void testPlayerDamageWithoutDeath() {
        // Player takes damage but doesn't die
        boolean died = player.takeDamage(1);

        assertFalse(died);
        assertEquals(2, player.getCurrentHealth());
        assertEquals(3, player.getLives()); // Lives unchanged
        assertTrue(player.isInvulnerable()); // Should be invulnerable after damage
        assertFalse(player.isAtFullHealth());
        assertEquals(0.67f, player.getHealthPercentage(), 0.01f);
    }

    @Test
    void testPlayerDeathAndRespawn() {
        // Damage player to death
        player.takeDamage(3);

        // Should respawn with full health but lose a life
        assertEquals(3, player.getCurrentHealth()); // Respawned with full health
        assertEquals(2, player.getLives()); // Lost a life
        assertTrue(player.isInvulnerable()); // Invulnerable after respawn
        assertTrue(player.isAtFullHealth());
    }

    @Test
    void testPlayerFinalDeath() {
        // Reduce to 1 life
        player.setLives(1);

        // Take fatal damage
        boolean died = player.takeDamage(3);

        assertTrue(died); // Should indicate final death
        assertEquals(0, player.getCurrentHealth());
        assertEquals(0, player.getLives());
    }

    @Test
    void testInvulnerabilityPreventseDamage() {
        player.setInvulnerable(true);

        boolean died = player.takeDamage(5);

        assertFalse(died);
        assertEquals(3, player.getCurrentHealth()); // No damage taken
        assertEquals(3, player.getLives()); // No lives lost
    }

    @Test
    void testInvulnerabilityTimer() {
        player.setInvulnerable(true);
        assertTrue(player.isInvulnerable());
        assertEquals(180, player.getInvulnerabilityTimer()); // 3 seconds at 60 FPS

        // Simulate frame updates
        for (int i = 0; i < 180; i++) {
            player.updateInvulnerability();
        }

        assertFalse(player.isInvulnerable());
        assertEquals(0, player.getInvulnerabilityTimer());
    }

    @Test
    void testScoreSystem() {
        assertEquals(0, player.getScore());

        player.addScore(100);
        assertEquals(100, player.getScore());

        player.addScore(50);
        assertEquals(150, player.getScore());

        player.setScore(500);
        assertEquals(500, player.getScore());
    }

    @Test
    void testHealthSystem() {
        player.setMaxHealth(5);
        assertEquals(5, player.getMaxHealth());
        assertEquals(5, player.getCurrentHealth()); // Should reset to max

        player.takeDamage(2);
        assertEquals(3, player.getCurrentHealth());

        player.heal();
        assertEquals(5, player.getCurrentHealth());
        assertTrue(player.isAtFullHealth());
    }

    @Test
    void testBackwardCompatibilityDamage() {
        // Test the old damage() method (defaults to 1 damage)
        boolean died = player.damage();

        assertFalse(died);
        assertEquals(2, player.getCurrentHealth());
        assertTrue(player.isInvulnerable());
    }
}