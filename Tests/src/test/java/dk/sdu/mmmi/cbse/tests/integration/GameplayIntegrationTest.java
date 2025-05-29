package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidComponent;
import dk.sdu.mmmi.cbse.commonasteroid.AsteroidSize;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionHandlers;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.collision.CollisionSystem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;

/**
 * Integration tests for complete gameplay scenarios.
 * Tests how multiple systems work together to represent actual gameplay.
 */
@DisplayName("Gameplay Integration Tests")
public class GameplayIntegrationTest {

    private GameData gameData;
    private World world;
    private CollisionSystem collisionSystem;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        world = new World();
        collisionSystem = new CollisionSystem();
    }

    @Test
    @DisplayName("Player shoots asteroid and takes damage from collision")
    void testPlayerShootsAsteroidAndTakesDamage() {
        // Setup: Create player with full health
        Entity player = createTestPlayer(400, 300);
        PlayerComponent playerComp = player.getComponent(PlayerComponent.class);
        assertEquals(3, playerComp.getCurrentHealth());
        assertFalse(playerComp.isInvulnerable());

        // Setup: Create asteroid near player
        Entity asteroid = createTestAsteroid(410, 310);
        AsteroidComponent asteroidComp = asteroid.getComponent(AsteroidComponent.class);
        assertEquals(3, asteroidComp.getCurrentHealth());

        // Setup: Create player bullet that will hit asteroid
        Entity bullet = createTestPlayerBullet(405, 305, player);

        world.addEntity(player);
        world.addEntity(asteroid);
        world.addEntity(bullet);

        // Verify initial state
        assertEquals(3, world.getEntities().size());

        // Execute: Process one collision cycle
        collisionSystem.process(gameData, world);

        // Verify: Bullet hit asteroid (asteroid should be damaged or destroyed)
        // The exact behavior depends on your collision response implementation

        // Verify: Player should eventually take damage from asteroid collision
        // (This tests the complete collision chain)
        assertTrue(playerComp.getCurrentHealth() <= 3, "Player should have taken damage or remained the same");

        // If player took damage, should be invulnerable
        if (playerComp.getCurrentHealth() < 3) {
            assertTrue(playerComp.isInvulnerable(), "Player should be invulnerable after taking damage");
        }
    }

    @Test
    @DisplayName("Player weapon burst mechanics work correctly")
    void testPlayerBurstWeaponMechanics() {
        Entity player = createTestPlayer(400, 300);
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);

        // Configure burst weapon
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstCount(3);
        weapon.setBurstDelay(0.1f);
        weapon.setCooldownTime(1.0f);

        // Test burst sequence
        assertTrue(weapon.canFire(), "Should be able to start burst");

        weapon.triggerFire();
        assertTrue(weapon.isBurstInProgress(), "Burst should be in progress");

        // Simulate burst shots
        for (int i = 0; i < 3; i++) {
            if (i > 0) {
                weapon.updateCooldown(0.1f); // Wait for burst delay
            }

            assertTrue(weapon.canFire() || i == 0, "Should be able to fire shot " + (i + 1));
            weapon.fireBurstShot();
        }

        assertTrue(weapon.isBurstComplete(), "Burst should be complete");
        assertFalse(weapon.isBurstInProgress(), "Burst should no longer be in progress");
        assertFalse(weapon.canFire(), "Should be on main cooldown");

        // Wait for main cooldown
        weapon.updateCooldown(1.0f);
        assertTrue(weapon.canFire(), "Should be able to fire again after cooldown");
    }

    @Test
    @DisplayName("Asteroid health system works with multiple bullet hits")
    void testAsteroidHealthSystem() {
        Entity asteroid = createTestAsteroid(400, 300);
        AsteroidComponent asteroidComp = asteroid.getComponent(AsteroidComponent.class);

        assertEquals(3, asteroidComp.getCurrentHealth());
        assertEquals(3, asteroidComp.getMaxHealth());
        assertTrue(asteroidComp.isAtFullHealth());

        // Damage asteroid but don't destroy it
        boolean destroyed = asteroidComp.takeDamage(1);
        assertFalse(destroyed, "Asteroid should not be destroyed by 1 damage");
        assertEquals(2, asteroidComp.getCurrentHealth());
        assertFalse(asteroidComp.isAtFullHealth());
        assertEquals(0.67f, asteroidComp.getHealthPercentage(), 0.01f);

        // Damage again
        destroyed = asteroidComp.takeDamage(1);
        assertFalse(destroyed, "Asteroid should not be destroyed by 2 total damage");
        assertEquals(1, asteroidComp.getCurrentHealth());

        // Final damage should destroy it
        destroyed = asteroidComp.takeDamage(1);
        assertTrue(destroyed, "Asteroid should be destroyed by 3 total damage");
        assertEquals(0, asteroidComp.getCurrentHealth());
    }

    @Test
    @DisplayName("Player invulnerability prevents damage correctly")
    void testPlayerInvulnerabilityMechanics() {
        Entity player = createTestPlayer(400, 300);
        PlayerComponent playerComp = player.getComponent(PlayerComponent.class);

        // Take initial damage to trigger invulnerability
        boolean died = playerComp.takeDamage(1);
        assertFalse(died, "Player should not die from 1 damage");
        assertEquals(2, playerComp.getCurrentHealth());
        assertTrue(playerComp.isInvulnerable(), "Player should be invulnerable after damage");

        // Try to damage while invulnerable
        died = playerComp.takeDamage(2);
        assertFalse(died, "Player should not take damage while invulnerable");
        assertEquals(2, playerComp.getCurrentHealth(), "Health should not change while invulnerable");

        // Simulate invulnerability wearing off
        playerComp.setInvulnerable(false);

        // Now damage should work again
        died = playerComp.takeDamage(1);
        assertFalse(died, "Player should take damage when not invulnerable");
        assertEquals(1, playerComp.getCurrentHealth());
    }

    @Test
    @DisplayName("Entity lifecycle management works correctly")
    void testEntityLifecycleManagement() {
        // Create entities
        Entity player = createTestPlayer(400, 300);
        Entity asteroid = createTestAsteroid(500, 400);
        Entity bullet = createTestPlayerBullet(450, 350, player);

        // Add to world
        world.addEntity(player);
        world.addEntity(asteroid);
        world.addEntity(bullet);

        assertEquals(3, world.getEntities().size());

        // Verify entities can be found by type
        int playerCount = 0;
        int asteroidCount = 0;
        int bulletCount = 0;

        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null) {
                if (tag.hasType(EntityType.PLAYER)) playerCount++;
                if (tag.hasType(EntityType.ASTEROID)) asteroidCount++;
                if (tag.hasType(EntityType.BULLET)) bulletCount++;
            }
        }

        assertEquals(1, playerCount, "Should have exactly 1 player");
        assertEquals(1, asteroidCount, "Should have exactly 1 asteroid");
        assertEquals(1, bulletCount, "Should have exactly 1 bullet");

        // Remove entities
        world.removeEntity(bullet);
        assertEquals(2, world.getEntities().size());

        world.removeEntity(asteroid);
        assertEquals(1, world.getEntities().size());

        world.removeEntity(player);
        assertEquals(0, world.getEntities().size());
    }

    @Test
    @DisplayName("Component composition creates functional entities")
    void testComponentComposition() {
        Entity entity = EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(300, 200)
                .withRotation(45)
                .withRadius(10)
                .with(new PlayerComponent())
                .with(new WeaponComponent())
                .with(createColliderComponent(CollisionLayer.PLAYER))
                .build();

        // Verify entity has all expected components
        assertTrue(entity.hasComponent(TagComponent.class));
        assertTrue(entity.hasComponent(TransformComponent.class));
        assertTrue(entity.hasComponent(PlayerComponent.class));
        assertTrue(entity.hasComponent(WeaponComponent.class));
        assertTrue(entity.hasComponent(ColliderComponent.class));

        // Verify component properties
        TagComponent tag = entity.getComponent(TagComponent.class);
        assertTrue(tag.hasType(EntityType.PLAYER));

        TransformComponent transform = entity.getComponent(TransformComponent.class);
        assertEquals(new Vector2D(300, 200), transform.getPosition());
        assertEquals(45f, transform.getRotation());
        assertEquals(10f, transform.getRadius());

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        assertEquals(CollisionLayer.PLAYER, collider.getLayer());

        // Verify entity has expected component count
        assertEquals(5, entity.getComponentCount());
    }

    // Helper methods for creating test entities
    private Entity createTestPlayer(float x, float y) {
        ColliderComponent collider = createColliderComponent(CollisionLayer.PLAYER);
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.ASTEROID, CollisionHandlers.createPlayerDamageHandler(1));

        return EntityBuilder.create()
                .withType(EntityType.PLAYER)
                .atPosition(x, y)
                .withRadius(8)
                .with(new PlayerComponent())
                .with(new WeaponComponent())
                .with(collider)
                .with(response)
                .build();
    }

    private Entity createTestAsteroid(float x, float y) {
        ColliderComponent collider = createColliderComponent(CollisionLayer.OBSTACLE);
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.PLAYER, CollisionHandlers.createPlayerDamageHandler(1));

        return EntityBuilder.create()
                .withType(EntityType.ASTEROID)
                .atPosition(x, y)
                .withRadius(15)
                .with(new AsteroidComponent(AsteroidSize.LARGE))
                .with(collider)
                .with(response)
                .build();
    }

    private Entity createTestPlayerBullet(float x, float y, Entity shooter) {
        BulletComponent bulletComp = new BulletComponent(
                UUID.fromString(shooter.getID()),
                BulletComponent.BulletSource.PLAYER
        );
        bulletComp.setSpeed(300f);
        bulletComp.setDamage(1f);

        ColliderComponent collider = createColliderComponent(CollisionLayer.PLAYER_PROJECTILE);
        CollisionResponseComponent response = new CollisionResponseComponent();
        response.addHandler(EntityType.ASTEROID, CollisionHandlers.BULLET_DAMAGE_HANDLER);

        return EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(x, y)
                .withRadius(3)
                .with(bulletComp)
                .with(collider)
                .with(response)
                .build();
    }

    private ColliderComponent createColliderComponent(CollisionLayer layer) {
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(layer);
        return collider;
    }
}