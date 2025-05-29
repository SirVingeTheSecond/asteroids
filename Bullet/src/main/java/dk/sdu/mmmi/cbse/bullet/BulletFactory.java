package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.*;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonbullet.BulletType;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionHandlers;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.input.InputController;
import javafx.scene.paint.Color;

import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating bullet entities.
 */
public class BulletFactory implements IBulletSPI {
    private static final Logger LOGGER = Logger.getLogger(BulletFactory.class.getName());

    // Bullet size
    private static final float TINY_BULLET_RADIUS = 2.5f;
    private static final float STANDARD_BULLET_RADIUS = 4f;
    private static final float HEAVY_BULLET_RADIUS = 6.5f;

    private static final float DEFAULT_SPAWN_DISTANCE = 15f;

    // Recoil config
    private static final float HEAVY_RECOIL_FORCE = 150f;
    private static final float SHOTGUN_RECOIL_FORCE = 80f;
    private static final float BURST_RECOIL_FORCE = 40f;
    private static final float AUTO_RECOIL_FORCE = 0f;

    private final BulletRegistry bulletRegistry;
    private final IPhysicsSPI physicsSPI;

    /**
     * Create a new bullet factory
     */
    public BulletFactory() {
        this.bulletRegistry = BulletRegistry.getInstance();
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);

        if (physicsSPI == null) {
            LOGGER.log(Level.WARNING, "PhysicsSPI not available - recoil effects disabled");
        }

        LOGGER.log(Level.INFO, "BulletFactory initialized with variable bullet sizes");
    }

    @Override
    public Entity createBullet(Entity shooter, GameData gameData, String bulletType) {
        TransformComponent shooterTransform = shooter.getComponent(TransformComponent.class);
        WeaponComponent weaponComponent = shooter.getComponent(WeaponComponent.class);
        TagComponent shooterTag = shooter.getComponent(TagComponent.class);

        if (shooterTransform == null) {
            LOGGER.log(Level.WARNING, "Cannot create bullet: shooter missing TransformComponent");
            return null;
        }

        boolean isPlayerBullet = shooterTag != null && shooterTag.hasType(EntityType.PLAYER);
        BulletType bulletTypeConfig = bulletRegistry.getBulletType(bulletType);

        float rotation = calculateBulletDirection(shooterTransform, weaponComponent, isPlayerBullet);

        // Apply recoil to player for heavy weapons
        if (isPlayerBullet && weaponComponent != null) {
            applyRecoil(shooter, weaponComponent, rotation);
        }

        float bulletRadius = getBulletRadius(bulletType);
        float spawnDistance = shooterTransform.getRadius() + DEFAULT_SPAWN_DISTANCE;
        float radians = (float) Math.toRadians(rotation);
        Vector2D forward = new Vector2D((float) Math.cos(radians), (float) Math.sin(radians));
        Vector2D spawnPosition = shooterTransform.getPosition().add(forward.scale(spawnDistance));

        BulletComponent bulletComponent = new BulletComponent(
                UUID.fromString(shooter.getID()),
                isPlayerBullet ? BulletComponent.BulletSource.PLAYER : BulletComponent.BulletSource.ENEMY
        );

        // Configure bullet from weapon and bullet type
        configureBulletComponent(bulletComponent, weaponComponent, bulletTypeConfig);

        // Movement
        MovementComponent movementComponent = new MovementComponent();
        movementComponent.setPattern(MovementComponent.MovementPattern.LINEAR);
        movementComponent.setSpeed(bulletComponent.getSpeed());
        movementComponent.setRotationSpeed(0.0f);

        // Collision
        ColliderComponent colliderComponent = new ColliderComponent();
        colliderComponent.setLayer(isPlayerBullet ? CollisionLayer.PLAYER_PROJECTILE : CollisionLayer.ENEMY_PROJECTILE);

        // Rendering with colors matching shooter
        RendererComponent rendererComponent = createBulletRenderer(shooter, bulletTypeConfig, bulletRadius);

        Entity bullet = EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(spawnPosition)
                .withRotation(rotation)
                .withRadius(bulletRadius)
                .with(bulletComponent)
                .with(movementComponent)
                .with(colliderComponent)
                .with(rendererComponent)
                .with(createBulletCollisionResponse(isPlayerBullet))
                .build();

        LOGGER.log(Level.FINE, "Created {0} bullet (radius: {1}) from shooter {2}",
                new Object[]{bulletType, bulletRadius, shooter.getID()});

        return bullet;
    }

    // Static counter for shotgun pellet distribution
    private static int shotgunPelletIndex = 0;

    /**
     * Calculate bullet direction with proper spread handling
     */
    private float calculateBulletDirection(TransformComponent shooterTransform,
                                           WeaponComponent weaponComponent,
                                           boolean isPlayerBullet) {
        float rotation;

        if (isPlayerBullet) {
            // Player bullets aim toward mouse
            Vector2D playerPos = shooterTransform.getPosition();
            Vector2D mousePos = InputController.getMousePosition();
            Vector2D direction = mousePos.subtract(playerPos);
            rotation = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
        } else {
            // Enemy bullets use transform rotation
            rotation = shooterTransform.getRotation();
        }

        // Apply spread for shotguns with proper pellet distribution
        if (weaponComponent != null && weaponComponent.getFiringPattern() == Weapon.FiringPattern.SHOTGUN) {
            float spreadAngle = weaponComponent.getSpreadAngle();
            int shotCount = weaponComponent.getShotCount();

            if (shotCount > 1) {
                // Create even spread pattern across all pellets
                float halfSpread = spreadAngle / 2.0f;
                float angleStep = spreadAngle / (shotCount - 1);

                // Use sequential pellet index for even distribution
                float angleOffset = -halfSpread + (angleStep * (shotgunPelletIndex % shotCount));
                rotation += angleOffset;

                shotgunPelletIndex++;
            }
        }

        return rotation;
    }

    /**
     * Apply recoil to the player with satisfying kick-and-recovery mechanics
     * Uses RecoilComponent to manage recoil state and physics override
     */
    private void applyRecoil(Entity shooter, WeaponComponent weaponComponent, float bulletDirection) {
        if (physicsSPI == null || !physicsSPI.hasPhysics(shooter)) {
            return;
        }

        RecoilComponent recoil = shooter.getComponent(RecoilComponent.class);
        if (recoil == null) {
            LOGGER.log(Level.WARNING, "Player missing RecoilComponent for recoil effects");
            return;
        }

        // Recoil configuration per weapon type
        float recoilForce = 0.0f;
        float recoilDuration = 0.0f;
        float angularKick = 0.0f;

        switch (weaponComponent.getFiringPattern()) {
            case HEAVY:
                recoilForce = 400.0f;      // Strong kick
                recoilDuration = 0.5f;     // 500ms recovery
                angularKick = 25.0f;       // Rotational kick
                break;

            case SHOTGUN:
                recoilForce = 280.0f;      // Moderate kick
                recoilDuration = 0.35f;    // 350ms recovery
                angularKick = 15.0f;       // Light rotation
                break;

            case BURST:
                recoilForce = 120.0f;      // Light kick per shot
                recoilDuration = 0.25f;    // 250ms recovery
                angularKick = 8.0f;        // Minimal rotation
                break;

            case AUTOMATIC:
            default:
                recoilForce = 0.0f;        // No recoil for rapid fire
                break;
        }

        if (recoilForce > 0) {
            // Calculate recoil direction (opposite to bullet direction)
            float recoilAngle = bulletDirection + 180.0f;
            float radians = (float) Math.toRadians(recoilAngle);
            Vector2D recoilDirection = new Vector2D(
                    (float) Math.cos(radians),
                    (float) Math.sin(radians)
            );

            // Apply recoil impulse
            Vector2D recoilVelocity = recoilDirection.scale(recoilForce);
            physicsSPI.applyImpulse(shooter, recoilVelocity);

            // Start recoil state management
            recoil.startRecoil(recoilVelocity, recoilDuration);

            // Apply angular kick for heavy weapons
            if (angularKick > 0) {
                float randomDirection = (float)(Math.random() - 0.5) * 2.0f; // -1 to 1
                float angularImpulse = randomDirection * angularKick;
                physicsSPI.setAngularVelocity(shooter, angularImpulse);
            }

            LOGGER.log(Level.FINE, "Applied {0} recoil: force={1}, duration={2}s, angular={3}°",
                    new Object[]{weaponComponent.getFiringPattern(), recoilForce, recoilDuration, angularKick});
        }
    }

    /**
     * Get bullet radius based on bullet type
     */
    private float getBulletRadius(String bulletType) {
        switch (bulletType.toLowerCase()) {
            case "tiny":
                return TINY_BULLET_RADIUS;
            case "heavy":
                return HEAVY_BULLET_RADIUS;
            case "standard":
            default:
                return STANDARD_BULLET_RADIUS;
        }
    }

    /**
     * Configure bullet component with weapon and bullet type
     */
    private void configureBulletComponent(BulletComponent bulletComponent,
                                          WeaponComponent weaponComponent,
                                          BulletType bulletTypeConfig) {
        float speed = bulletTypeConfig.getSpeed();
        float damage = bulletTypeConfig.getDamage();

        if (weaponComponent != null) {
            speed = weaponComponent.getProjectileSpeed();
            damage = weaponComponent.getDamage();
        }

        bulletComponent.setSpeed(speed);
        bulletComponent.setDamage(damage);
        bulletComponent.setPiercing(bulletTypeConfig.isPiercing());
        bulletComponent.setPierceCount(bulletTypeConfig.getPierceCount());
        bulletComponent.setBouncing(bulletTypeConfig.isBouncing());
        bulletComponent.setBounceCount(bulletTypeConfig.getBounceCount());
    }

    /**
     * Create renderer component with colors matching shooter
     */
    private RendererComponent createBulletRenderer(Entity shooter, BulletType bulletTypeConfig, float bulletRadius) {
        RendererComponent rendererComponent = new RendererComponent();
        rendererComponent.setShapeType(RendererComponent.ShapeType.CIRCLE);

        // Get shooter's colors
        RendererComponent shooterRenderer = shooter.getComponent(RendererComponent.class);
        Color strokeColor;
        Color fillColor;

        if (shooterRenderer != null) {
            // Use shooter's colors
            strokeColor = shooterRenderer.getStrokeColor();
            fillColor = shooterRenderer.getFillColor();
        } else {
            // Fallback to bullet type colors
            Color baseColor = bulletTypeConfig.getColor();
            strokeColor = darkenColor(baseColor, 0.3f);
            fillColor = lightenColor(baseColor, 0.2f);
        }

        rendererComponent.setStrokeColor(strokeColor);
        rendererComponent.setFillColor(fillColor);

        float strokeWidth = Math.max(1.0f, bulletRadius * 0.4f);
        rendererComponent.setStrokeWidth(strokeWidth);

        rendererComponent.setRenderLayer(RenderLayer.BULLET);
        rendererComponent.setFilled(true);

        return rendererComponent;
    }

    /**
     * Create collision response for bullets
     */
    private CollisionResponseComponent createBulletCollisionResponse(boolean isPlayerBullet) {
        CollisionResponseComponent response = new CollisionResponseComponent();

        if (isPlayerBullet) {
            response.addHandler(EntityType.ENEMY, CollisionHandlers.BULLET_DAMAGE_HANDLER);
            response.addHandler(EntityType.ASTEROID, CollisionHandlers.BULLET_DAMAGE_HANDLER);
            response.addHandler(EntityType.BULLET, CollisionHandlers.BULLET_BULLET_COLLISION_HANDLER);
        } else {
            response.addHandler(EntityType.PLAYER, CollisionHandlers.BULLET_DAMAGE_HANDLER);
            response.addHandler(EntityType.BULLET, CollisionHandlers.BULLET_BULLET_COLLISION_HANDLER);
        }

        response.addHandler(EntityType.OBSTACLE, CollisionHandlers.REMOVE_ON_COLLISION_HANDLER);

        return response;
    }

    /**
     * Darken a color by a factor
     */
    private Color darkenColor(Color color, float factor) {
        double red = Math.max(0, color.getRed() - factor);
        double green = Math.max(0, color.getGreen() - factor);
        double blue = Math.max(0, color.getBlue() - factor);
        return new Color(red, green, blue, color.getOpacity());
    }

    /**
     * Lighten a color by a factor
     */
    private Color lightenColor(Color color, float factor) {
        double red = Math.min(1.0, color.getRed() + factor);
        double green = Math.min(1.0, color.getGreen() + factor);
        double blue = Math.min(1.0, color.getBlue() + factor);
        return new Color(red, green, blue, color.getOpacity());
    }
}