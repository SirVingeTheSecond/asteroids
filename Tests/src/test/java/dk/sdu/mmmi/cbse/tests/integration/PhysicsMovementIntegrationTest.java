package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.physics.PhysicsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Physics and Movement
 */
@DisplayName("Physics-Movement Integration Tests")
class PhysicsMovementIntegrationTest {

    private IPhysicsSPI physicsService;
    private GameData gameData;

    @BeforeEach
    void setUp() {
        physicsService = new PhysicsService();
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);
    }

    @Test
    @DisplayName("Should apply forces and update entity velocity")
    void shouldApplyForcesAndUpdateEntityVelocity() {
        // Create entity with physics
        Entity entity = createPhysicsEntity(100, 100);

        // Apply force
        Vector2D force = new Vector2D(100, 0);
        physicsService.applyForce(entity, force);

        // Get physics component and simulate force application
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        physics.applyAccumulatedForcesAndImpulses(0.016f); // Simulate 60 FPS

        // Verify velocity changed
        Vector2D velocity = physicsService.getVelocity(entity);
        assertTrue(velocity.x() > 0);
        assertEquals(0, velocity.y(), 0.001f);
    }

    @Test
    @DisplayName("Should apply impulses and immediately affect velocity")
    void shouldApplyImpulsesAndImmediatelyAffectVelocity() {
        // Create entity with physics
        Entity entity = createPhysicsEntity(100, 100);

        // Apply impulse
        Vector2D impulse = new Vector2D(50, 0);
        physicsService.applyImpulse(entity, impulse);

        // Get physics component and simulate impulse application
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        physics.applyAccumulatedForcesAndImpulses(0.016f);

        // Verify velocity changed immediately
        Vector2D velocity = physicsService.getVelocity(entity);
        assertTrue(velocity.x() > 0);
    }

    @Test
    @DisplayName("Should handle angular physics correctly")
    void shouldHandleAngularPhysicsCorrectly() {
        // Create entity with physics
        Entity entity = createPhysicsEntity(100, 100);

        // Set angular velocity
        physicsService.setAngularVelocity(entity, 90.0f); // 90 degrees per second

        // Verify angular velocity
        float angularVelocity = physicsService.getAngularVelocity(entity);
        assertEquals(90.0f, angularVelocity, 0.001f);
    }

    @Test
    @DisplayName("Should handle drag and velocity dampening")
    void shouldHandleDragAndVelocityDampening() {
        // Create entity with physics
        Entity entity = createPhysicsEntity(100, 100);
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        physics.setDrag(0.9f); // High drag

        // Set initial velocity
        Vector2D initialVelocity = new Vector2D(100, 0);
        physicsService.setVelocity(entity, initialVelocity);

        // Apply drag multiple times
        for (int i = 0; i < 10; i++) {
            physics.applyDrag(0.016f);
        }

        // Verify velocity decreased due to drag
        Vector2D finalVelocity = physicsService.getVelocity(entity);
        assertTrue(finalVelocity.magnitude() < initialVelocity.magnitude());
    }

    @Test
    @DisplayName("Should respect maximum speed limits")
    void shouldRespectMaximumSpeedLimits() {
        // Create entity with physics and low max speed
        Entity entity = createPhysicsEntity(100, 100);
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);
        physics.setMaxSpeed(50.0f);

        // Apply very large force
        Vector2D largeForce = new Vector2D(10000, 0);
        physicsService.applyForce(entity, largeForce);
        physics.applyAccumulatedForcesAndImpulses(0.016f);

        // Verify velocity clamped to max speed
        Vector2D velocity = physicsService.getVelocity(entity);
        assertTrue(velocity.magnitude() <= 50.0f);
    }

    @Test
    @DisplayName("Should handle entities without physics components gracefully")
    void shouldHandleEntitiesWithoutPhysicsComponentsGracefully() {
        // Create entity without physics
        Entity entity = new Entity();
        entity.addComponent(new TransformComponent());

        // Should not crash when applying physics operations
        assertFalse(physicsService.hasPhysics(entity));

        physicsService.applyForce(entity, new Vector2D(100, 0));
        physicsService.setVelocity(entity, new Vector2D(50, 0));

        // Should return zero velocity for entity without physics
        assertEquals(Vector2D.zero(), physicsService.getVelocity(entity));
    }

    private Entity createPhysicsEntity(float x, float y) {
        Entity entity = new Entity();

        // Add transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(x, y));
        entity.addComponent(transform);

        // Add physics
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.0f);
        physics.setDrag(0.98f);
        physics.setMaxSpeed(200.0f);
        entity.addComponent(physics);

        return entity;
    }
}