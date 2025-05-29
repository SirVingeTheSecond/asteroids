package dk.sdu.mmmi.cbse.tests.contracts;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ServiceLoader;

/**
 * Contract tests for IPhysicsSPI implementations.
 */
@DisplayName("Physics SPI Contract Tests")
public class PhysicsSPIContractTest {

    private IPhysicsSPI physicsSPI;
    private Entity testEntity;

    @BeforeEach
    void setUp() {
        physicsSPI = ServiceLoader.load(IPhysicsSPI.class)
                .findFirst()
                .orElseThrow(() -> new AssertionError("IPhysicsSPI implementation not found"));

        testEntity = EntityBuilder.create()
                .atPosition(100, 100)
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();
    }

    @Test
    @DisplayName("Contract: applyForce must affect velocity")
    void testApplyForceContract() {
        Vector2D initialVelocity = physicsSPI.getVelocity(testEntity);
        Vector2D force = new Vector2D(100, 0);

        physicsSPI.applyForce(testEntity, force);

        Vector2D newVelocity = physicsSPI.getVelocity(testEntity);
        assertNotEquals(initialVelocity, newVelocity,
                "applyForce must change entity velocity");
    }

    @Test
    @DisplayName("Contract: implementation must handle null entities gracefully")
    void testNullEntityHandling() {
        assertDoesNotThrow(() -> {
            physicsSPI.applyForce(null, Vector2D.zero());
            Vector2D velocity = physicsSPI.getVelocity(null);
            assertEquals(Vector2D.zero(), velocity, "Should return zero velocity for null entity");
        }, "SPI implementation must handle null entities gracefully");
    }

    @Test
    @DisplayName("Contract: setVelocity must be immediately retrievable")
    void testVelocityConsistency() {
        Vector2D testVelocity = new Vector2D(50, 75);

        physicsSPI.setVelocity(testEntity, testVelocity);
        Vector2D retrievedVelocity = physicsSPI.getVelocity(testEntity);

        assertEquals(testVelocity, retrievedVelocity,
                "Set velocity must be immediately retrievable");
    }

    @Test
    @DisplayName("Contract: hasPhysics must correctly identify physics entities")
    void testHasPhysicsContract() {
        assertTrue(physicsSPI.hasPhysics(testEntity),
                "Entity with PhysicsComponent should return true for hasPhysics");

        Entity nonPhysicsEntity = EntityBuilder.create().build();
        assertFalse(physicsSPI.hasPhysics(nonPhysicsEntity),
                "Entity without PhysicsComponent should return false for hasPhysics");
    }
}