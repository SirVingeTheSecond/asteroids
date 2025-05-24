package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
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
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.input.InputController;
import javafx.scene.paint.Color;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating bullet entities.
 */
public class BulletFactory implements IBulletSPI {
    private static final Logger LOGGER = Logger.getLogger(BulletFactory.class.getName());

    private static final float DEFAULT_BULLET_RADIUS = 4f;
    private static final float DEFAULT_SPAWN_DISTANCE = 15f;
    private final BulletRegistry bulletRegistry;
    private final Random random = new Random();

    /**
     * Create a new bullet factory
     */
    public BulletFactory() {
        this.bulletRegistry = BulletRegistry.getInstance();
        LOGGER.log(Level.INFO, "BulletFactory initialized");
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

        float rotation;
        if (isPlayerBullet) {
            Vector2D playerPos = shooterTransform.getPosition();
            Vector2D mousePos = InputController.getMousePosition();
            Vector2D direction = mousePos.subtract(playerPos);
            rotation = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
        } else {
            rotation = shooterTransform.getRotation();
        }

        // Apply spread for shotguns AFTER calculating base direction
        if (weaponComponent != null && weaponComponent.getFiringPattern() == Weapon.FiringPattern.SHOTGUN) {
            float spreadAngle = weaponComponent.getSpreadAngle();
            int shotCount = weaponComponent.getShotCount();

            if (shotCount > 1) {
                float angleOffset = spreadAngle * (random.nextFloat() - 0.5f);
                rotation += angleOffset;
            }
        }

        float spawnDistance = shooterTransform.getRadius() + DEFAULT_SPAWN_DISTANCE;
        float radians = (float) Math.toRadians(rotation);
        Vector2D forward = new Vector2D((float) Math.cos(radians), (float) Math.sin(radians));
        Vector2D spawnPosition = shooterTransform.getPosition().add(forward.scale(spawnDistance));

        BulletComponent bulletComponent = new BulletComponent(
                UUID.fromString(shooter.getID()),
                isPlayerBullet ? BulletComponent.BulletSource.PLAYER : BulletComponent.BulletSource.ENEMY
        );

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

        // Movement
        MovementComponent movementComponent = new MovementComponent();
        movementComponent.setPattern(MovementComponent.MovementPattern.LINEAR);
        movementComponent.setSpeed(speed);
        movementComponent.setRotationSpeed(0.0f);

        // collision
        ColliderComponent colliderComponent = new ColliderComponent();
        colliderComponent.setLayer(isPlayerBullet ? CollisionLayer.PLAYER_PROJECTILE : CollisionLayer.ENEMY_PROJECTILE);

        RendererComponent rendererComponent = new RendererComponent();
        rendererComponent.setShapeType(RendererComponent.ShapeType.CIRCLE);

        Color baseColor = bulletTypeConfig.getColor();
        Color strokeColor = darkenColor(baseColor, 0.3f); // 30% darker stroke
        Color fillColor = lightenColor(baseColor, 0.2f);  // 20% lighter fill

        rendererComponent.setStrokeColor(strokeColor);
        rendererComponent.setFillColor(fillColor);
        rendererComponent.setStrokeWidth(2.0f);
        rendererComponent.setRenderLayer(RenderLayer.BULLET);
        rendererComponent.setFilled(true);

        Entity bullet = EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(spawnPosition)
                .withRotation(rotation)
                .withRadius(DEFAULT_BULLET_RADIUS)
                .with(bulletComponent)
                .with(movementComponent)
                .with(colliderComponent)
                .with(rendererComponent)
                .with(createBulletCollisionResponse(isPlayerBullet))
                .build();

        LOGGER.log(Level.FINE, "Created circular bullet of type {0} from shooter {1} with direction {2}",
                new Object[]{bulletType, shooter.getID(), rotation});

        return bullet;
    }

    private CollisionResponseComponent createBulletCollisionResponse(boolean isPlayerBullet) {
        CollisionResponseComponent response = new CollisionResponseComponent();

        if (isPlayerBullet) {
            response.addHandler(EntityType.ENEMY, CollisionHandlers.BULLET_DAMAGE_HANDLER);
            response.addHandler(EntityType.ASTEROID, CollisionHandlers.BULLET_DAMAGE_HANDLER);
        } else {
            response.addHandler(EntityType.PLAYER, CollisionHandlers.BULLET_DAMAGE_HANDLER);
        }

        response.addHandler(EntityType.OBSTACLE, CollisionHandlers.REMOVE_ON_COLLISION_HANDLER);

        return response;
    }

    private Color darkenColor(Color color, float factor) {
        double red = Math.max(0, color.getRed() - factor);
        double green = Math.max(0, color.getGreen() - factor);
        double blue = Math.max(0, color.getBlue() - factor);
        return new Color(red, green, blue, color.getOpacity());
    }

    private Color lightenColor(Color color, float factor) {
        double red = Math.min(1.0, color.getRed() + factor);
        double green = Math.min(1.0, color.getGreen() + factor);
        double blue = Math.min(1.0, color.getBlue() + factor);
        return new Color(red, green, blue, color.getOpacity());
    }
}