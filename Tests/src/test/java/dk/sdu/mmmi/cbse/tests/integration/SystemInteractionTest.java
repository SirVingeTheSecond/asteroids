package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.player.PlayerSystem;
import dk.sdu.mmmi.cbse.physics.PhysicsSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests showing how systems work together following CBSE principles.
 */
@DisplayName("System Interaction Integration Tests")
public class SystemInteractionTest {

    private GameData gameData;
    private World world;
    private PlayerSystem playerSystem;
    private PhysicsSystem physicsSystem;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        world = new World();
        playerSystem = new PlayerSystem();
        physicsSystem = new PhysicsSystem();
    }

    @Test
    @DisplayName("Player and Physics systems coordinate correctly")
    void testPlayerPhysicsSystemCoordination() {
        Entity player = EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(400, 300)
                .with(new PlayerComponent())
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .with(new WeaponComponent())
                .build();

        world.addEntity(player);

        // Process systems in order (physics before player for consistency)
        assertDoesNotThrow(() -> {
            physicsSystem.update(gameData, world);
            playerSystem.update(gameData, world);
        }, "Systems should coordinate without errors");

        // Verify entity is still in valid state
        assertTrue(world.getEntities().contains(player));
        assertNotNull(player.getComponent(PlayerComponent.class));
        assertNotNull(player.getComponent(PhysicsComponent.class));
    }

    @Test
    @DisplayName("Systems handle missing components gracefully")
    void testSystemRobustness() {
        // Create entity missing expected components
        Entity incompleteEntity = EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(100, 100)
                // Missing PlayerComponent, PhysicsComponent, etc.
                .build();

        world.addEntity(incompleteEntity);

        // Systems should handle incomplete entities without crashing
        assertDoesNotThrow(() -> {
            playerSystem.update(gameData, world);
            physicsSystem.update(gameData, world);
        }, "Systems must handle incomplete entities gracefully");
    }

    @Test
    @DisplayName("System processing maintains entity state consistency")
    void testStateConsistency() {
        Entity player = EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(200, 200)
                .with(new PlayerComponent())
                .with(new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC))
                .build();

        world.addEntity(player);

        Vector2D initialPosition = player.getComponent(
                dk.sdu.mmmi.cbse.common.components.TransformComponent.class).getPosition();

        // Process multiple update cycles
        for (int i = 0; i < 10; i++) {
            physicsSystem.update(gameData, world);
            playerSystem.update(gameData, world);
        }

        // Verify entity state remains consistent
        assertNotNull(player.getComponent(PlayerComponent.class),
                "PlayerComponent should not be removed");
        assertNotNull(player.getComponent(PhysicsComponent.class),
                "PhysicsComponent should not be removed");

        Vector2D finalPosition = player.getComponent(
                dk.sdu.mmmi.cbse.common.components.TransformComponent.class).getPosition();
        assertNotNull(finalPosition, "Position should remain valid");
    }
}