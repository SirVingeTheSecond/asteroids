package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
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
     * Create a new player entity
     *
     * @param gameData Game data
     * @return New player entity
     */
    public Entity createPlayer(GameData gameData) {
        LOGGER.log(Level.INFO, "Creating player entity");

        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(-5, -5, 10, 0, -5, 5); // Triangle shape
        transform.setPosition(new Vector2D((float) gameData.getDisplayWidth() / 2, (float) gameData.getDisplayHeight() / 2));
        transform.setRadius(8); // Collision radius
        player.addComponent(transform);

        // renderer
        RendererComponent renderer = new RendererComponent();
        renderer.setStrokeColor(Color.GREEN);
        renderer.setFillColor(Color.LIGHTGREEN);
        renderer.setStrokeWidth(2.0f);
        renderer.setRenderLayer(500); // Player on top layer
        player.addComponent(renderer);

        // player component
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setLives(3);
        player.addComponent(playerComponent);

        // movement component
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.PLAYER);
        movement.setSpeed(150.0f);
        movement.setRotationSpeed(0.0f);
        player.addComponent(movement);

        // weapon component
        WeaponComponent weapon = new WeaponComponent();
        weapon.setBulletType("standard");
        weapon.setDamage(10.0f);
        weapon.setProjectileSpeed(300.0f);
        weapon.setCooldownTime(15); // Frames between shots (4 shots per second at 60 FPS)
        player.addComponent(weapon);

        // collider component
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);
        player.addComponent(collider);

        // tag component
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        player.addComponent(tag);

        LOGGER.log(Level.INFO, "Player entity created: {0}", player.getID());
        return player;
    }
}