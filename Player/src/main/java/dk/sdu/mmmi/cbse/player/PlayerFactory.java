package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
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
     *
     * @param gameData Game data
     * @return New player entity
     */
    public Entity createPlayer(GameData gameData) {
        LOGGER.log(Level.INFO, "Creating player entity with physics, flicker support, and collision response");

        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(-7,-7,15,0,-7,7); // Triangle
        transform.setPosition(new Vector2D((float) gameData.getDisplayWidth() / 2, (float) gameData.getDisplayHeight() / 2));
        transform.setRadius(8);
        player.addComponent(transform);

        // Physics
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.0f);
        physics.setDrag(0.92f); // 8% drag per frame
        physics.setMaxSpeed(150.0f);
        physics.setAngularDrag(0.95f);
        player.addComponent(physics);

        // Renderer
        RendererComponent renderer = new RendererComponent();
        renderer.setStrokeColor(Color.LIGHTGREEN);
        renderer.setFillColor(Color.DARKGREEN);
        renderer.setStrokeWidth(2f);
        renderer.setRenderLayer(RenderLayer.PLAYER);
        renderer.setFilled(true);
        player.addComponent(renderer);

        // Player
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setLives(3);
        playerComponent.setMaxHealth(3); // 3 health points
        player.addComponent(playerComponent);

        // Movement
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.PLAYER);
        movement.setSpeed(150.0f);
        movement.setRotationSpeed(0.0f);
        player.addComponent(movement);

        // Weapon
        WeaponComponent weapon = new WeaponComponent();
        weapon.setBulletType("standard");
        weapon.setDamage(1.0f);
        weapon.setProjectileSpeed(300.0f);
        weapon.setCooldownTime(0.25f); // 4 shots per second
        player.addComponent(weapon);

        // Collider
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);
        player.addComponent(collider);

        // Collision Response
        player.addComponent(createPlayerCollisionResponse());

        // Flicker
        FlickerComponent flicker = new FlickerComponent();
        flicker.setFlickerRate(10.0f); // 10 Hz flicker for invulnerability
        player.addComponent(flicker);
        LOGGER.log(Level.FINE, "Added FlickerComponent to player with rate: {0}", flicker.getFlickerRate());

        // Tag
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        player.addComponent(tag);

        LOGGER.log(Level.INFO, "Player entity created with 3 health points and collision response: {0}", player.getID());
        return player;
    }

    /**
     * Create collision response component for player
     */
    private CollisionResponseComponent createPlayerCollisionResponse() {
        CollisionResponseComponent response = new CollisionResponseComponent();

        response.addHandler(EntityType.ASTEROID, CollisionHandlers.DIRECT_DAMAGE_HANDLER);
        response.addHandler(EntityType.ENEMY, CollisionHandlers.DIRECT_DAMAGE_HANDLER);

        response.addHandler(EntityType.BULLET, CollisionHandlers.ENEMY_BULLET_HANDLER);

        return response;
    }
}