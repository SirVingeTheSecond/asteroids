package dk.sdu.mmmi.cbse.tests.contracts;

import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Contract tests for verifying plugin implementations
 */
@DisplayName("Plugin Contract Tests")
class PluginContractTest {

    private GameData gameData;
    private World world;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        world = new World();
    }

    @Test
    @DisplayName("Plugin contract should handle lifecycle correctly")
    void pluginContractShouldHandleLifecycleCorrectly() {
        // Test implementation of IPluginService contract
        TestPlugin plugin = new TestPlugin();

        // Verify initial state
        assertFalse(plugin.isStarted());
        assertEquals(0, plugin.getStartCount());
        assertEquals(0, plugin.getStopCount());

        // Test start
        assertDoesNotThrow(() -> plugin.start(gameData, world));
        assertTrue(plugin.isStarted());
        assertEquals(1, plugin.getStartCount());

        // Test multiple starts (should be idempotent)
        plugin.start(gameData, world);
        assertEquals(1, plugin.getStartCount()); // Should not increment

        // Test stop
        assertDoesNotThrow(() -> plugin.stop(gameData, world));
        assertFalse(plugin.isStarted());
        assertEquals(1, plugin.getStopCount());

        // Test multiple stops (should be idempotent)
        plugin.stop(gameData, world);
        assertEquals(1, plugin.getStopCount()); // Should not increment
    }

    @Test
    @DisplayName("Plugin contract should handle null parameters gracefully")
    void pluginContractShouldHandleNullParametersGracefully() {
        TestPlugin plugin = new TestPlugin();

        // Should not throw exceptions with null parameters
        assertDoesNotThrow(() -> plugin.start(null, world));
        assertDoesNotThrow(() -> plugin.start(gameData, null));
        assertDoesNotThrow(() -> plugin.start(null, null));

        assertDoesNotThrow(() -> plugin.stop(null, world));
        assertDoesNotThrow(() -> plugin.stop(gameData, null));
        assertDoesNotThrow(() -> plugin.stop(null, null));
    }

    @Test
    @DisplayName("Plugin contract should maintain consistent state")
    void pluginContractShouldMaintainConsistentState() {
        TestPlugin plugin = new TestPlugin();

        // Start -> Stop -> Start sequence
        plugin.start(gameData, world);
        assertTrue(plugin.isStarted());

        plugin.stop(gameData, world);
        assertFalse(plugin.isStarted());

        plugin.start(gameData, world);
        assertTrue(plugin.isStarted());

        // Verify counts
        assertEquals(2, plugin.getStartCount());
        assertEquals(1, plugin.getStopCount());
    }

    /**
     * Test implementation of IPluginService for contract verification
     */
    private static class TestPlugin implements IPluginService {
        private boolean started = false;
        private int startCount = 0;
        private int stopCount = 0;

        @Override
        public void start(GameData gameData, World world) {
            if (!started) {
                started = true;
                startCount++;
            }
        }

        @Override
        public void stop(GameData gameData, World world) {
            if (started) {
                started = false;
                stopCount++;
            }
        }

        public boolean isStarted() {
            return started;
        }

        public int getStartCount() {
            return startCount;
        }

        public int getStopCount() {
            return stopCount;
        }
    }
}