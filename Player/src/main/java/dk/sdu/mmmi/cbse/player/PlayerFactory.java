package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionHandlers;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionResponseComponent;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating player entities.
 */
public class PlayerFactory {
    private static final Logger LOGGER = Logger.getLogger(PlayerFactory.class.getName());

    /**
     * Create a new player entity.
     */
    public Entity createPlayer(GameData gameData) {
        LOGGER.log(Level.INFO, "Creating player entity");

        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(-7, -7, 15, 0, -7, 7); // Triangle pointing right
        transform.setPosition(new Vector2D(
                (float) gameData.getDisplayWidth() / 2,
                (float) gameData.getDisplayHeight() / 2
        ));
        transform.setRadius(8);
        player.addComponent(transform);

        // Physics - clean, responsive movement configuration
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.0f);
        physics.setDrag(MovementConfig.getDragCoefficient());
        physics.setMaxSpeed(MovementConfig.getMaxSpeed());
        physics.setAngularDrag(0.98f);
        player.addComponent(physics);

        // Renderer - visual appearance
        RendererComponent renderer = new RendererComponent();
        renderer.setStrokeColor(Color.LIGHTGREEN);
        renderer.setFillColor(Color.DARKGREEN);
        renderer.setStrokeWidth(2f);
        renderer.setRenderLayer(RenderLayer.PLAYER);
        renderer.setFilled(true);
        player.addComponent(renderer);

        // Player data
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setLives(3);
        playerComponent.setMaxHealth(3);
        player.addComponent(playerComponent);

        // Weapon
        WeaponComponent weapon = new WeaponComponent();
        weapon.setBulletType("standard");
        weapon.setDamage(1.0f);
        weapon.setProjectileSpeed(300.0f);
        weapon.setCooldownTime(0.25f); // 4 shots per second
        player.addComponent(weapon);

        // Collision
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);
        player.addComponent(collider);

        // Collision response
        player.addComponent(createPlayerCollisionResponse());

        // Flicker
        FlickerComponent flicker = new FlickerComponent();
        flicker.setFlickerRate(10.0f); // 10 Hz
        player.addComponent(flicker);

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        player.addComponent(tag);

        LOGGER.log(Level.INFO, "Player entity created: {0}", player.getID());
        return player;
    }

    /**
     * Create collision response component for player interactions
     */
    private CollisionResponseComponent createPlayerCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        // Player takes damage from asteroids and enemies
        response.addHandler(EntityType.ASTEROID, CollisionHandlers.DIRECT_DAMAGE_HANDLER);
        response.addHandler(EntityType.ENEMY, CollisionHandlers.DIRECT_DAMAGE_HANDLER);

        // Player takes damage from enemy bullets
        response.addHandler(EntityType.BULLET, CollisionHandlers.ENEMY_BULLET_HANDLER);

        // Player ignores obstacles (boundary system handles collision)
        response.addHandler(EntityType.OBSTACLE, CollisionHandlers.IGNORE_COLLISION_HANDLER);

        return response;
    }
}