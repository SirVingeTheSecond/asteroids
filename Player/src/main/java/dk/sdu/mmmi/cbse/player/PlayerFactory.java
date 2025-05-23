package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.RenderLayer;
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
        LOGGER.log(Level.INFO, "Creating player entity with physics");

        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(-7,-7,15,0,-7,7); // Triangle shape
        transform.setPosition(new Vector2D((float) gameData.getDisplayWidth() / 2, (float) gameData.getDisplayHeight() / 2));
        transform.setRadius(8);
        player.addComponent(transform);

        // Physics component for acceleration/deceleration
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(1.0f);           // Standard mass
        physics.setDrag(0.92f);          // 8% drag per frame
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

        // Player component
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setLives(3);
        player.addComponent(playerComponent);

        // Movement component (for compatibility with existing systems)
        MovementComponent movement = new MovementComponent();
        movement.setPattern(MovementComponent.MovementPattern.PLAYER);
        movement.setSpeed(150.0f); // Base speed (used as max speed reference)
        movement.setRotationSpeed(0.0f);
        player.addComponent(movement);

        // Weapon component
        WeaponComponent weapon = new WeaponComponent();
        weapon.setBulletType("standard");
        weapon.setDamage(10.0f);
        weapon.setProjectileSpeed(300.0f);
        weapon.setCooldownTime(15); // Frames between shots (4 shots per second at 60 FPS)
        player.addComponent(weapon);

        // Collider component
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.PLAYER);
        player.addComponent(collider);

        // Tag component
        TagComponent tag = new TagComponent();
        tag.addType(EntityType.PLAYER);
        player.addComponent(tag);

        LOGGER.log(Level.INFO, "Player entity created with physics: {0}", player.getID());
        return player;
    }
}