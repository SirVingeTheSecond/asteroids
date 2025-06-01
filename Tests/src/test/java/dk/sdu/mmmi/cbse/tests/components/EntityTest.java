package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Entity
 */
@DisplayName("Entity Unit Tests")
class EntityTest {

    private Entity entity;
    private TransformComponent transformComponent;
    private TagComponent tagComponent;

    @BeforeEach
    void setUp() {
        entity = new Entity();
        transformComponent = new TransformComponent();
        tagComponent = new TagComponent();
    }

    @Test
    @DisplayName("Should generate unique IDs for different entities")
    void shouldGenerateUniqueIDs() {
        Entity entity1 = new Entity();
        Entity entity2 = new Entity();

        assertNotNull(entity1.getID());
        assertNotNull(entity2.getID());
        assertNotEquals(entity1.getID(), entity2.getID());
    }

    @Test
    @DisplayName("Should add and retrieve components correctly")
    void shouldAddAndRetrieveComponents() {
        // Initially should have no components
        assertEquals(0, entity.getComponentCount());
        assertNull(entity.getComponent(TransformComponent.class));

        // Add component
        entity.addComponent(transformComponent);
        assertEquals(1, entity.getComponentCount());
        assertTrue(entity.hasComponent(TransformComponent.class));

        // Retrieve component
        TransformComponent retrieved = entity.getComponent(TransformComponent.class);
        assertNotNull(retrieved);
        assertSame(transformComponent, retrieved);
    }

    @Test
    @DisplayName("Should remove components correctly")
    void shouldRemoveComponents() {
        // Add component
        entity.addComponent(transformComponent);
        assertTrue(entity.hasComponent(TransformComponent.class));

        // Remove component
        boolean removed = entity.removeComponent(TransformComponent.class);
        assertTrue(removed);
        assertFalse(entity.hasComponent(TransformComponent.class));
        assertEquals(0, entity.getComponentCount());

        // Try to remove again - should return false
        boolean removedAgain = entity.removeComponent(TransformComponent.class);
        assertFalse(removedAgain);
    }

    @Test
    @DisplayName("Should handle multiple components of different types")
    void shouldHandleMultipleComponents() {
        entity.addComponent(transformComponent);
        entity.addComponent(tagComponent);

        assertEquals(2, entity.getComponentCount());
        assertTrue(entity.hasComponent(TransformComponent.class));
        assertTrue(entity.hasComponent(TagComponent.class));

        assertSame(transformComponent, entity.getComponent(TransformComponent.class));
        assertSame(tagComponent, entity.getComponent(TagComponent.class));
    }

    @Test
    @DisplayName("Should throw exception for null component")
    void shouldThrowExceptionForNullComponent() {
        assertThrows(NullPointerException.class, () -> {
            entity.addComponent(null);
        });
    }

    @Test
    @DisplayName("Should throw exception for null component type")
    void shouldThrowExceptionForNullComponentType() {
        assertThrows(NullPointerException.class, () -> {
            entity.getComponent(null);
        });

        assertThrows(NullPointerException.class, () -> {
            entity.hasComponent(null);
        });

        assertThrows(NullPointerException.class, () -> {
            entity.removeComponent(null);
        });
    }

    @Test
    @DisplayName("Should replace component of same type")
    void shouldReplaceComponentOfSameType() {
        TransformComponent firstTransform = new TransformComponent();
        TransformComponent secondTransform = new TransformComponent();

        entity.addComponent(firstTransform);
        assertSame(firstTransform, entity.getComponent(TransformComponent.class));

        // Adding another component of same type should replace the first
        entity.addComponent(secondTransform);
        assertEquals(1, entity.getComponentCount());
        assertSame(secondTransform, entity.getComponent(TransformComponent.class));
        assertNotSame(firstTransform, entity.getComponent(TransformComponent.class));
    }
}

