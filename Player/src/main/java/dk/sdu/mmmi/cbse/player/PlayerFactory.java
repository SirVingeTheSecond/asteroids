package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.FlickerComponent;
import dk.sdu.mmmi.cbse.common.components.RecoilComponent;
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
import dk.sdu.mmmi.cbse.commonweapon.Weapon;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import javafx.scene.paint.Color;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Factory for creating player entities.
 */
public class PlayerFactory {
    private static final Logger LOGGER = Logger.getLogger(PlayerFactory.class.getName());

    /**
     * Create a new player entity with all their respective components.
     */
    public Entity createPlayer(GameData gameData) {
        LOGGER.log(Level.INFO, "Creating player entity with recoil support");

        Entity player = new Entity();

        // Transform
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(-7, -7, 15, 0, -7, 7); // Triangle
        transform.setPosition(new Vector2D(
                (float) gameData.getDisplayWidth() / 2,
                (float) gameData.getDisplayHeight() / 2
        ));
        transform.setRadius(8);
        player.addComponent(transform);

        // Physics
        PhysicsComponent physics = new PhysicsComponent(PhysicsComponent.PhysicsType.DYNAMIC);
        physics.setMass(0.8f);
        physics.setDrag(0.9f);
        physics.setMaxSpeed(150f);
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

        // Player data
        PlayerComponent playerComponent = new PlayerComponent();
        playerComponent.setLives(3);
        playerComponent.setMaxHealth(3);
        player.addComponent(playerComponent);

        // Recoil
        RecoilComponent recoil = new RecoilComponent();
        player.addComponent(recoil);

        // Weapon
        WeaponComponent weapon = createAutomaticWeapon();
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

        LOGGER.log(Level.INFO, "Player entity created with recoil support and automatic weapon: {0}", player.getID());
        return player;
    }

    /**
     * Create automatic weapon component for player starting weapon
     */
    private WeaponComponent createAutomaticWeapon() {
        try {
            var weaponSPIOptional = ServiceLoader.load(dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI.class).findFirst();
            if (weaponSPIOptional.isPresent()) {
                Weapon automaticWeapon = weaponSPIOptional.get().getWeapon("automatic");
                if (automaticWeapon != null) {
                    WeaponComponent weapon = new WeaponComponent(automaticWeapon);
                    LOGGER.log(Level.INFO, "Player configured with automatic weapon from registry");
                    return weapon;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Could not load automatic weapon from registry, using fallback", e);
        }

        // Fallback: Create automatic weapon manually
        WeaponComponent weapon = new WeaponComponent();
        weapon.setFiringPattern(Weapon.FiringPattern.AUTOMATIC);
        weapon.setBulletType("tiny");
        weapon.setDamage(1.0f);
        weapon.setProjectileSpeed(400.0f);
        weapon.setCooldownTime(0.15f);

        LOGGER.log(Level.INFO, "Player configured with fallback automatic weapon");
        return weapon;
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