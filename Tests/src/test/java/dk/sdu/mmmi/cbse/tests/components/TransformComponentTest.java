package dk.sdu.mmmi.cbse.tests.components;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for TransformComponent
 */
@DisplayName("TransformComponent Unit Tests")
class TransformComponentTest {

    private TransformComponent transform;

    @BeforeEach
    void setUp() {
        transform = new TransformComponent();
    }

    @Test
    @DisplayName("Should initialize with default values")
    void shouldInitializeWithDefaults() {
        assertEquals(0f, transform.getX(), 0.001f);
        assertEquals(0f, transform.getY(), 0.001f);
        assertEquals(Vector2D.zero(), transform.getPosition());
        assertEquals(new Vector2D(1, 1), transform.getScale());
        assertEquals(0f, transform.getRotation(), 0.001f);
        assertEquals(1f, transform.getRadius(), 0.001f);
    }

    @Test
    @DisplayName("Should handle position changes correctly")
    void shouldHandlePositionChanges() {
        Vector2D newPosition = new Vector2D(10f, 20f);
        transform.setPosition(newPosition);

        assertEquals(newPosition, transform.getPosition());
        assertEquals(10f, transform.getX(), 0.001f);
        assertEquals(20f, transform.getY(), 0.001f);
    }

    @Test
    @DisplayName("Should handle translation correctly")
    void shouldHandleTranslation() {
        transform.setPosition(new Vector2D(5f, 5f));
        Vector2D translation = new Vector2D(3f, 7f);

        transform.translate(translation);

        assertEquals(8f, transform.getX(), 0.001f);
        assertEquals(12f, transform.getY(), 0.001f);
    }

    @Test
    @DisplayName("Should handle rotation and direction vectors correctly")
    void shouldHandleRotationAndDirections() {
        // Test initial forward vector (0 degrees = right)
        Vector2D initialForward = transform.getForward();
        assertEquals(1f, initialForward.x(), 0.001f);
        assertEquals(0f, initialForward.y(), 0.001f);

        // Test 90-degree rotation (should point up)
        transform.setRotation(90);
        Vector2D upwardForward = transform.getForward();
        assertEquals(0f, upwardForward.x(), 0.001f);
        assertEquals(1f, upwardForward.y(), 0.001f);

        // Test right vector (perpendicular to forward)
        Vector2D right = transform.getRight();
        assertEquals(-1f, right.x(), 0.001f);
        assertEquals(0f, right.y(), 0.001f);
    }

    @Test
    @DisplayName("Should handle rotation accumulation correctly")
    void shouldHandleRotationAccumulation() {
        transform.setRotation(45);
        transform.rotate(45);

        assertEquals(90f, transform.getRotation(), 0.001f);

        // Test wrapping around 360 degrees
        transform.rotate(280);
        assertEquals(10f, transform.getRotation(), 0.001f); // 370 % 360 = 10
    }

    @Test
    @DisplayName("Should handle movement in local directions")
    // ToDo: expected: <-3.0> but was: <3.0>
    void shouldHandleMoveForwardAndRight() {
        transform.setPosition(new Vector2D(0, 0));
        transform.setRotation(0); // Facing right

        // Move forward (should move right)
        transform.moveForward(5f);
        assertEquals(5f, transform.getX(), 0.001f);
        assertEquals(0f, transform.getY(), 0.001f);

        // Move right (should move down when facing right)
        transform.moveRight(3f);
        assertEquals(5f, transform.getX(), 0.001f);
        assertEquals(-3f, transform.getY(), 0.001f);
    }

    @Test
    @DisplayName("Should handle polygon coordinates")
    void shouldHandlePolygonCoordinates() {
        double[] coords = {-5, -5, 5, -5, 5, 5, -5, 5}; // Square
        transform.setPolygonCoordinates(coords);

        double[] retrieved = transform.getPolygonCoordinates();
        assertArrayEquals(coords, retrieved);
    }

    @Test
    @DisplayName("Should handle radius changes")
    void shouldHandleRadiusChanges() {
        transform.setRadius(15f);
        assertEquals(15f, transform.getRadius(), 0.001f);
    }
}
