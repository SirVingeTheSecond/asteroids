package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TagComponent
 */
@DisplayName("TagComponent Unit Tests")
class TagComponentTest {

    private TagComponent tagComponent;

    @BeforeEach
    void setUp() {
        tagComponent = new TagComponent();
    }

    @Test
    @DisplayName("Should initialize with no types")
    void shouldInitializeWithNoTypes() {
        assertTrue(tagComponent.getTypes().isEmpty());
        assertFalse(tagComponent.hasType(EntityType.PLAYER));
        assertFalse(tagComponent.hasType(EntityType.ENEMY));
    }

    @Test
    @DisplayName("Should add and check entity types correctly")
    void shouldAddAndCheckEntityTypes() {
        tagComponent.addType(EntityType.PLAYER);

        assertTrue(tagComponent.hasType(EntityType.PLAYER));
        assertFalse(tagComponent.hasType(EntityType.ENEMY));
        assertEquals(1, tagComponent.getTypes().size());
        assertTrue(tagComponent.getTypes().contains(EntityType.PLAYER));
    }

    @Test
    @DisplayName("Should handle multiple entity types")
    void shouldHandleMultipleEntityTypes() {
        tagComponent.addType(EntityType.PLAYER);
        tagComponent.addType(EntityType.BULLET);

        assertTrue(tagComponent.hasType(EntityType.PLAYER));
        assertTrue(tagComponent.hasType(EntityType.BULLET));
        assertFalse(tagComponent.hasType(EntityType.ENEMY));
        assertEquals(2, tagComponent.getTypes().size());
    }

    @Test
    @DisplayName("Should remove entity types correctly")
    void shouldRemoveEntityTypes() {
        tagComponent.addType(EntityType.PLAYER);
        tagComponent.addType(EntityType.BULLET);

        tagComponent.removeType(EntityType.PLAYER);

        assertFalse(tagComponent.hasType(EntityType.PLAYER));
        assertTrue(tagComponent.hasType(EntityType.BULLET));
        assertEquals(1, tagComponent.getTypes().size());
    }

    @Test
    @DisplayName("Should check for all types correctly")
    void shouldCheckForAllTypes() {
        tagComponent.addType(EntityType.PLAYER);
        tagComponent.addType(EntityType.BULLET);

        assertTrue(tagComponent.hasAllTypes(EntityType.PLAYER));
        assertTrue(tagComponent.hasAllTypes(EntityType.PLAYER, EntityType.BULLET));
        assertFalse(tagComponent.hasAllTypes(EntityType.PLAYER, EntityType.ENEMY));
        assertTrue(tagComponent.hasAllTypes()); // Empty array should return true
    }

    @Test
    @DisplayName("Should check for any type correctly")
    void shouldCheckForAnyType() {
        tagComponent.addType(EntityType.PLAYER);

        assertTrue(tagComponent.hasAnyType(EntityType.PLAYER));
        assertTrue(tagComponent.hasAnyType(EntityType.PLAYER, EntityType.ENEMY));
        assertFalse(tagComponent.hasAnyType(EntityType.ENEMY, EntityType.ASTEROID));
        assertFalse(tagComponent.hasAnyType()); // Empty array should return false
    }

    @Test
    @DisplayName("Should clear all types correctly")
    void shouldClearAllTypes() {
        tagComponent.addType(EntityType.PLAYER);
        tagComponent.addType(EntityType.BULLET);
        assertEquals(2, tagComponent.getTypes().size());

        tagComponent.clearTypes();

        assertTrue(tagComponent.getTypes().isEmpty());
        assertFalse(tagComponent.hasType(EntityType.PLAYER));
        assertFalse(tagComponent.hasType(EntityType.BULLET));
    }

    @Test
    @DisplayName("Should handle null types gracefully")
    void shouldHandleNullTypes() {
        // Adding null should not affect the component
        tagComponent.addType(null);
        assertTrue(tagComponent.getTypes().isEmpty());

        // Checking null should return false
        assertFalse(tagComponent.hasType(null));

        // Removing null should not affect the component
        tagComponent.addType(EntityType.PLAYER);
        tagComponent.removeType(null);
        assertTrue(tagComponent.hasType(EntityType.PLAYER));
    }

    @Test
    @DisplayName("Should initialize with initial types")
    void shouldInitializeWithInitialTypes() {
        TagComponent tagWithInitialTypes = new TagComponent(EntityType.PLAYER, EntityType.BULLET);

        assertTrue(tagWithInitialTypes.hasType(EntityType.PLAYER));
        assertTrue(tagWithInitialTypes.hasType(EntityType.BULLET));
        assertEquals(2, tagWithInitialTypes.getTypes().size());
    }
}
