package dk.sdu.mmmi.cbse.tests.integration;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.weapon.WeaponService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Weapon and Bullet creation flow
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Weapon-Bullet Integration Tests")
class WeaponBulletIntegrationTest {

    @Mock
    private IBulletSPI mockBulletSPI;

    @Mock
    private ServiceLoader<IBulletSPI> mockServiceLoader;

    private WeaponService weaponService;
    private GameData gameData;
    private Entity shooter;

    @BeforeEach
    void setUp() {
        gameData = new GameData();
        gameData.setDisplayWidth(800);
        gameData.setDisplayHeight(600);

        // Create shooter entity
        shooter = createShooterEntity();

        // Mock the ServiceLoader to return our mock BulletSPI
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            serviceLoaderMock.when(() -> ServiceLoader.load(IBulletSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(java.util.Optional.of(mockBulletSPI));

            weaponService = new WeaponService();
        }
    }

    @Test
    @DisplayName("Should create bullets when shooting with automatic weapon")
    void shouldCreateBulletsWhenShootingWithAutomaticWeapon() {
        // Setup weapon
        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        weapon.setFiringPattern(Weapon.FiringPattern.AUTOMATIC);

        // Mock bullet creation
        Entity mockBullet = createMockBullet();
        when(mockBulletSPI.createBullet(eq(shooter), eq(gameData), eq("tiny")))
                .thenReturn(mockBullet);

        // Shoot
        List<Entity> bullets = weaponService.shoot(shooter, gameData, "tiny");

        // Verify
        assertEquals(1, bullets.size());
        assertEquals(mockBullet, bullets.get(0));
        verify(mockBulletSPI).createBullet(shooter, gameData, "tiny");
        assertFalse(weapon.canFire()); // Should be on cooldown
    }

    @Test
    @DisplayName("Should create multiple bullets when shooting with shotgun")
    void shouldCreateMultipleBulletsWhenShootingWithShotgun() {
        // Setup shotgun weapon
        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        weapon.setFiringPattern(Weapon.FiringPattern.SHOTGUN);
        weapon.setShotCount(3);
        weapon.setSpreadAngle(30.0f);

        // Mock bullet creation for each shot
        Entity mockBullet1 = createMockBullet();
        Entity mockBullet2 = createMockBullet();
        Entity mockBullet3 = createMockBullet();
        when(mockBulletSPI.createBullet(eq(shooter), eq(gameData), eq("standard")))
                .thenReturn(mockBullet1, mockBullet2, mockBullet3);

        // Shoot
        List<Entity> bullets = weaponService.shoot(shooter, gameData, "standard");

        // Verify
        assertEquals(3, bullets.size());
        verify(mockBulletSPI, times(3)).createBullet(eq(shooter), eq(gameData), eq("standard"));
        assertFalse(weapon.canFire()); // Should be on cooldown
    }

    @Test
    @DisplayName("Should handle burst firing correctly")
    void shouldHandleBurstFiringCorrectly() {
        // Setup burst weapon
        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        weapon.setFiringPattern(Weapon.FiringPattern.BURST);
        weapon.setBurstCount(3);
        weapon.setBurstDelay(0.1f);

        // Mock bullet creation
        Entity mockBullet = createMockBullet();
        when(mockBulletSPI.createBullet(eq(shooter), eq(gameData), eq("standard")))
                .thenReturn(mockBullet);

        // Fire first shot in burst
        List<Entity> bullets = weaponService.shoot(shooter, gameData, "standard");

        // Verify first shot
        assertEquals(1, bullets.size());
        assertEquals(1, weapon.getCurrentBurstCount());
        assertFalse(weapon.isBurstComplete());
    }

    @Test
    @DisplayName("Should not shoot when weapon cannot fire")
    void shouldNotShootWhenWeaponCannotFire() {
        // Setup weapon on cooldown
        WeaponComponent weapon = shooter.getComponent(WeaponComponent.class);
        weapon.resetCooldown(); // Put weapon on cooldown

        // Attempt to shoot
        List<Entity> bullets = weaponService.shoot(shooter, gameData, "tiny");

        // Verify no bullets created
        assertTrue(bullets.isEmpty());
        verify(mockBulletSPI, never()).createBullet(any(), any(), any());
    }

    @Test
    @DisplayName("Should handle missing BulletSPI gracefully")
    void shouldHandleMissingBulletSPIGracefully() {
        // Create weapon service without BulletSPI
        try (MockedStatic<ServiceLoader> serviceLoaderMock = mockStatic(ServiceLoader.class)) {
            serviceLoaderMock.when(() -> ServiceLoader.load(IBulletSPI.class))
                    .thenReturn(mockServiceLoader);
            when(mockServiceLoader.findFirst()).thenReturn(java.util.Optional.empty());

            WeaponService weaponServiceWithoutBullet = new WeaponService();

            // Attempt to shoot
            List<Entity> bullets = weaponServiceWithoutBullet.shoot(shooter, gameData, "tiny");

            // Should return empty list gracefully
            assertTrue(bullets.isEmpty());
        }
    }

    @Test
    @DisplayName("Should handle entity without weapon component")
    void shouldHandleEntityWithoutWeaponComponent() {
        Entity entityWithoutWeapon = new Entity();
        entityWithoutWeapon.addComponent(new TransformComponent());

        List<Entity> bullets = weaponService.shoot(entityWithoutWeapon, gameData, "tiny");

        assertTrue(bullets.isEmpty());
        verify(mockBulletSPI, never()).createBullet(any(), any(), any());
    }

    private Entity createShooterEntity() {
        Entity entity = new Entity();

        // Add transform
        TransformComponent transform = new TransformComponent();
        transform.setPosition(new Vector2D(100, 100));
        transform.setRotation(0);
        entity.addComponent(transform);

        // Add weapon
        WeaponComponent weapon = new WeaponComponent();
        weapon.setFiringPattern(Weapon.FiringPattern.AUTOMATIC);
        weapon.setBulletType("tiny");
        entity.addComponent(weapon);

        // Add tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        entity.addComponent(tag);

        return entity;
    }

    private Entity createMockBullet() {
        Entity bullet = new Entity();
        bullet.addComponent(new TransformComponent());

        BulletComponent bulletComp = new BulletComponent(
                UUID.randomUUID(),
                BulletComponent.BulletSource.PLAYER
        );
        bullet.addComponent(bulletComp);

        TagComponent tag = new TagComponent();
        tag.addType(EntityType.BULLET);
        bullet.addComponent(tag);

        return bullet;
    }
}