package dk.sdu.mmmi.cbse.bullet;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.ColliderComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.components.WeaponComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonbullet.BulletComponent;
import dk.sdu.mmmi.cbse.commonbullet.IBulletSPI;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import javafx.scene.paint.Color;

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating bullet entities.
 */
public class BulletFactory implements IBulletSPI {
    private static final Logger LOGGER = Logger.getLogger(BulletFactory.class.getName());
    private static final float DEFAULT_BULLET_RADIUS = 2.0f;
    private static final float DEFAULT_SPAWN_DISTANCE = 5.0f;

    @Override
    public Entity createBullet(Entity shooter, GameData gameData) {
        // Get shooter components
        TransformComponent shooterTransform = shooter.getComponent(TransformComponent.class);
        WeaponComponent weaponComponent = shooter.getComponent(WeaponComponent.class);
        TagComponent shooterTag = shooter.getComponent(TagComponent.class);

        if (shooterTransform == null) {
            LOGGER.log(Level.WARNING, "Cannot create bullet: shooter missing TransformComponent");
            return null;
        }

        boolean isPlayerBullet = shooterTag != null && shooterTag.hasType(EntityType.PLAYER);

        // Calculate spawn position in front of the shooter
        float spawnDistance = shooterTransform.getRadius() + DEFAULT_SPAWN_DISTANCE;
        Vector2D spawnPosition = shooterTransform.getPosition().add(
                shooterTransform.getForward().scale(spawnDistance));

        // Create bullet component
        BulletComponent bulletComponent = new BulletComponent(
                UUID.fromString(shooter.getID()),
                isPlayerBullet ? BulletComponent.BulletType.PLAYER : BulletComponent.BulletType.ENEMY
        );

        if (weaponComponent != null) {
            bulletComponent.setSpeed(weaponComponent.getProjectileSpeed());
            bulletComponent.setDamage(weaponComponent.getDamage());
        }

        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(isPlayerBullet ? CollisionLayer.PLAYER_PROJECTILE : CollisionLayer.ENEMY_PROJECTILE);

        RendererComponent renderer = new RendererComponent();
        renderer.setStrokeColor(isPlayerBullet ? Color.YELLOW : Color.ORANGE);
        renderer.setFillColor(isPlayerBullet ? Color.YELLOW : Color.ORANGE);
        renderer.setRenderLayer(400); // Bullets above most entities but below player

        // Use EntityBuilder to create the bullet entity
        Entity bullet = EntityBuilder.create()
                .withType(EntityType.BULLET)
                .atPosition(spawnPosition)
                .withRotation(shooterTransform.getRotation())
                .withRadius(DEFAULT_BULLET_RADIUS)
                .withShape(-1, -1, 1, -1, 1, 1, -1, 1)
                .with(bulletComponent)
                .with(collider)
                .with(renderer)
                .build();

        LOGGER.log(Level.FINE, "Created bullet from shooter {0}", shooter.getID());

        return bullet;
    }
}