package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
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
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;

import java.util.Random;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating bullet entities.
 * Implements IBulletSPI for service discovery.
 */
public class BulletFactory implements IBulletSPI {
    private static final Logger LOGGER = Logger.getLogger(BulletFactory.class.getName());

    private static final float DEFAULT_BULLET_RADIUS = 2.0f;
    private static final float DEFAULT_SPAWN_DISTANCE = 5.0f;
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
        // Get shooter components
        TransformComponent shooterTransform = shooter.getComponent(TransformComponent.class);
        WeaponComponent weaponComponent = shooter.getComponent(WeaponComponent.class);
        TagComponent shooterTag = shooter.getComponent(TagComponent.class);

        if (shooterTransform == null) {
            LOGGER.log(Level.WARNING, "Cannot create bullet: shooter missing TransformComponent");
            return null;
        }

        boolean isPlayerBullet = shooterTag != null && shooterTag.hasType(EntityType.PLAYER);

        // Get bullet type configuration
        BulletType bulletTypeConfig = bulletRegistry.getBulletType(bulletType);

        // Calculate rotation - add spread for shotguns
        float rotation = shooterTransform.getRotation();
        if (weaponComponent != null && weaponComponent.getFiringPattern() == Weapon.FiringPattern.SHOTGUN) {
            // Apply spread angle for shotguns
            float spreadAngle = weaponComponent.getSpreadAngle();
            int shotCount = weaponComponent.getShotCount();

            if (shotCount > 1) {
                // Calculate position in spread pattern
                float angleOffset = spreadAngle * (random.nextFloat() - 0.5f);
                rotation += angleOffset;
            }
        }

        // Calculate spawn position in front of the shooter
        float spawnDistance = shooterTransform.getRadius() + DEFAULT_SPAWN_DISTANCE;
        Vector2D forward = shooterTransform.getForward();

        // Adjust forward direction if rotation was modified for shotgun spread
        if (rotation != shooterTransform.getRotation()) {
            float radians = (float) Math.toRadians(rotation);
            forward = new Vector2D((float) Math.cos(radians), (float) Math.sin(radians));
        }

        Vector2D spawnPosition = shooterTransform.getPosition().add(forward.scale(spawnDistance));

        // Create bullet component
        BulletComponent bulletComponent = new BulletComponent(
                UUID.fromString(shooter.getID()),
                isPlayerBullet ? BulletComponent.BulletSource.PLAYER : BulletComponent.BulletSource.ENEMY
        );

        // Configure bullet from type and weapon
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

        // Create collision component
        ColliderComponent colliderComponent = new ColliderComponent();
        colliderComponent.setLayer(isPlayerBullet ? CollisionLayer.PLAYER_PROJECTILE : CollisionLayer.ENEMY_PROJECTILE);

        // Create renderer component
        RendererComponent rendererComponent = new RendererComponent();
        rendererComponent.setStrokeColor(bulletTypeConfig.getColor());
        rendererComponent.setFillColor(bulletTypeConfig.getColor());
        rendererComponent.setRenderLayer(RenderLayer.BULLET); // Bullets above most entities but below player

        // Create bullet entity
        Entity bullet = EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(spawnPosition)
                .withRotation(rotation)
                .withRadius(DEFAULT_BULLET_RADIUS)
                .withShape(-1, -1, 1, -1, 1, 1, -1, 1)
                .with(bulletComponent)
                .with(colliderComponent)
                .with(new TagComponent(EntityType.BULLET))
                .with(rendererComponent)
                .build();

        LOGGER.log(Level.FINE, "Created bullet of type {0} from shooter {1}",
                new Object[]{bulletType, shooter.getID()});

        return bullet;
    }
}