package dk.sdu.mmmi.cbse.tests.utils;

import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import javafx.scene.paint.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for FlickerUtility static methods and behavior.
 */
@DisplayName("Flicker Utility Tests")
public class FlickerUtilityTest {

    private Entity testEntity;
    private FlickerComponent flickerComponent;
    private RendererComponent rendererComponent;

    @BeforeEach
    void setUp() {
        flickerComponent = new FlickerComponent();
        rendererComponent = new RendererComponent();
        rendererComponent.setStrokeColor(Color.WHITE);
        rendererComponent.setFillColor(Color.BLUE);

        testEntity = EntityBuilder.create()
                .atPosition(100, 100)
                .with(flickerComponent)
                .with(rendererComponent)
                .build();
    }

    @Test
    @DisplayName("Start flicker stores original colors and begins flickering")
    void testStartFlicker() {
        Color originalStroke = rendererComponent.getStrokeColor();
        Color originalFill = rendererComponent.getFillColor();

        FlickerUtility.startFlicker(testEntity, 2.0f, Color.RED, Color.DARKRED);

        // Should be flickering
        assertTrue(flickerComponent.isFlickering());
        assertEquals(2.0f, flickerComponent.getFlickerDuration());

        // Should store original colors
        assertEquals(originalStroke, flickerComponent.getOriginalStrokeColor());
        assertEquals(originalFill, flickerComponent.getOriginalFillColor());

        // Should store flicker colors
        assertEquals(Color.RED, flickerComponent.getFlickerStrokeColor());
        assertEquals(Color.DARKRED, flickerComponent.getFlickerFillColor());
    }

    @Test
    @DisplayName("Start flicker handles missing components gracefully")
    void testStartFlickerMissingComponents() {
        Entity incompleteEntity = EntityBuilder.create()
                .atPosition(50, 50)
                .build(); // No FlickerComponent or RendererComponent

        // Should not crash
        assertDoesNotThrow(() -> {
            FlickerUtility.startFlicker(incompleteEntity, 1.0f, Color.RED, Color.DARKRED);
        });
    }

    @Test
    @DisplayName("Update flicker progresses timer and handles color transitions")
    void testUpdateFlicker() {
        FlickerUtility.startFlicker(testEntity, 1.0f, Color.RED, Color.DARKRED);

        // Update flicker for half duration
        FlickerUtility.updateFlicker(testEntity, 0.5f);

        // Should still be flickering
        assertTrue(flickerComponent.isFlickering());
        assertTrue(flickerComponent.getFlickerTimer() > 0);
        assertTrue(flickerComponent.getFlickerTimer() < flickerComponent.getFlickerDuration());

        // Update past duration
        FlickerUtility.updateFlicker(testEntity, 0.6f);

        // Should stop flickering
        assertFalse(flickerComponent.isFlickering());
        assertEquals(0.0f, flickerComponent.getFlickerTimer());
    }

    @Test
    @DisplayName("Update flicker handles color cycling based on flicker rate")
    void testFlickerColorCycling() {
        FlickerUtility.startFlicker(testEntity, 1.0f, Color.RED, Color.DARKRED);

        // Store original colors for comparison
        Color originalStroke = flickerComponent.getOriginalStrokeColor();
        Color originalFill = flickerComponent.getOriginalFillColor();

        // Test multiple update cycles to see color changes
        for (int i = 0; i < 20; i++) {
            FlickerUtility.updateFlicker(testEntity, 0.04f); // Small time steps

            if (flickerComponent.isFlickering()) {
                Color currentStroke = rendererComponent.getStrokeColor();
                Color currentFill = rendererComponent.getFillColor();

                // Should be either original or flicker colors
                assertTrue(
                        (currentStroke.equals(originalStroke) && currentFill.equals(originalFill)) ||
                                (currentStroke.equals(Color.RED) && currentFill.equals(Color.DARKRED)),
                        "Colors should alternate between original and flicker colors"
                );
            }
        }
    }

    @Test
    @DisplayName("Stop flicker restores original colors and stops flickering")
    void testStopFlicker() {
        Color originalStroke = rendererComponent.getStrokeColor();
        Color originalFill = rendererComponent.getFillColor();

        FlickerUtility.startFlicker(testEntity, 2.0f, Color.RED, Color.DARKRED);
        assertTrue(flickerComponent.isFlickering());

        FlickerUtility.stopFlicker(testEntity);

        // Should stop flickering
        assertFalse(flickerComponent.isFlickering());
        assertEquals(0.0f, flickerComponent.getFlickerTimer());

        // Should restore original colors
        assertEquals(originalStroke, rendererComponent.getStrokeColor());
        assertEquals(originalFill, rendererComponent.getFillColor());
    }

    @Test
    @DisplayName("Is flickering correctly identifies flicker state")
    void testIsFlickering() {
        assertFalse(FlickerUtility.isFlickering(testEntity));

        FlickerUtility.startFlicker(testEntity, 1.0f, Color.RED, Color.DARKRED);
        assertTrue(FlickerUtility.isFlickering(testEntity));

        FlickerUtility.stopFlicker(testEntity);
        assertFalse(FlickerUtility.isFlickering(testEntity));
    }

    @Test
    @DisplayName("Start damage flicker uses predefined red colors")
    void testStartDamageFlicker() {
        FlickerUtility.startDamageFlicker(testEntity, 1.5f);

        assertTrue(flickerComponent.isFlickering());
        assertEquals(1.5f, flickerComponent.getFlickerDuration());
        assertEquals(Color.RED, flickerComponent.getFlickerStrokeColor());
        assertEquals(Color.DARKRED, flickerComponent.getFlickerFillColor());
    }

    @Test
    @DisplayName("Start invulnerability flicker uses predefined cyan colors")
    void testStartInvulnerabilityFlicker() {
        FlickerUtility.startInvulnerabilityFlicker(testEntity, 3.0f);

        assertTrue(flickerComponent.isFlickering());
        assertEquals(3.0f, flickerComponent.getFlickerDuration());
        assertEquals(Color.LIGHTCYAN, flickerComponent.getFlickerStrokeColor());
        assertEquals(Color.CYAN, flickerComponent.getFlickerFillColor());
    }

    @Test
    @DisplayName("Damage flicker handles missing FlickerComponent")
    void testDamageFlickerMissingComponent() {
        Entity entityWithoutFlicker = EntityBuilder.create()
                .atPosition(75, 75)
                .with(rendererComponent)
                .build(); // No FlickerComponent

        // Should not crash
        assertDoesNotThrow(() -> {
            FlickerUtility.startDamageFlicker(entityWithoutFlicker, 1.0f);
        });
    }

    @Test
    @DisplayName("Flicker utility preserves original colors across multiple flicker sessions")
    void testOriginalColorPreservation() {
        Color originalStroke = Color.WHITE;
        Color originalFill = Color.BLUE;

        // First flicker session
        FlickerUtility.startFlicker(testEntity, 0.5f, Color.RED, Color.DARKRED);
        FlickerUtility.updateFlicker(testEntity, 0.6f); // End first session

        // Verify original colors restored
        assertEquals(originalStroke, rendererComponent.getStrokeColor());
        assertEquals(originalFill, rendererComponent.getFillColor());

        // Second flicker session with different colors
        FlickerUtility.startFlicker(testEntity, 0.5f, Color.GREEN, Color.DARKGREEN);

        // Should still have the same original colors stored
        assertEquals(originalStroke, flickerComponent.getOriginalStrokeColor());
        assertEquals(originalFill, flickerComponent.getOriginalFillColor());
    }

    @Test
    @DisplayName("Update flicker handles missing components gracefully")
    void testUpdateFlickerMissingComponents() {
        Entity incompleteEntity = EntityBuilder.create()
                .atPosition(25, 25)
                .build();

        // Should not crash
        assertDoesNotThrow(() -> {
            FlickerUtility.updateFlicker(incompleteEntity, 0.1f);
        });
    }

    @Test
    @DisplayName("Flicker component correctly restores colors after completion")
    void testFlickerCompletionRestoration() {
        Color originalStroke = rendererComponent.getStrokeColor();
        Color originalFill = rendererComponent.getFillColor();

        FlickerUtility.startFlicker(testEntity, 0.2f, Color.YELLOW, Color.ORANGE);

        // Let flicker complete naturally
        FlickerUtility.updateFlicker(testEntity, 0.25f);

        // Should restore original colors automatically
        assertEquals(originalStroke, rendererComponent.getStrokeColor());
        assertEquals(originalFill, rendererComponent.getFillColor());
        assertFalse(flickerComponent.isFlickering());
    }
}