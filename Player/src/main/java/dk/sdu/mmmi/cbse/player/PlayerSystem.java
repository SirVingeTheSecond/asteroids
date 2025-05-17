package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IProcessingService;
import dk.sdu.mmmi.cbse.common.utils.ServiceLocator;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.input.Axis;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.utils.Time;
import dk.sdu.mmmi.cbse.core.input.InputController;
import javafx.scene.paint.Color;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for handling player input and controls.
 */
public class PlayerSystem implements IProcessingService {
    private static final Logger LOGGER = Logger.getLogger(PlayerSystem.class.getName());
    private IWeaponSPI weaponSPI;

    public PlayerSystem() {
        this.weaponSPI = ServiceLocator.getServiceOrNull(IWeaponSPI.class);
        LOGGER.log(Level.INFO, "PlayerSystem initialized with weaponSPI: {0}",
                weaponSPI != null ? weaponSPI.getClass().getName() : "not available");
    }

    @Override
    public void process(GameData gameData, World world) {
        if (weaponSPI == null) {
            weaponSPI = ServiceLocator.getServiceOrNull(IWeaponSPI.class);
        }

        Entity player = findPlayer(world);
        if (player == null) {
            return;
        }

        // required components
        MovementComponent movement = player.getComponent(MovementComponent.class);
        TransformComponent transform = player.getComponent(TransformComponent.class);
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

        if (movement == null || transform == null) {
            LOGGER.log(Level.WARNING, "Player missing required components");
            return;
        }

        float deltaTime = (float) Time.getDeltaTime();

        // player movement
        processMovement(transform, movement, deltaTime);

        // player rotation (toward mouse)
        processRotation(transform, gameData);

        // player shooting
        if (weapon != null && weaponSPI != null) {
            processShooting(player, weapon, gameData, world);
        }

        // Update player invulnerability
        if (playerComponent != null) {
            playerComponent.updateInvulnerability();

            // Visual feedback for invulnerability
            RendererComponent renderer = player.getComponent(RendererComponent.class);
            if (renderer != null && playerComponent.isInvulnerable()) {
                // Flicker player when invulnerable
                int frame = (int)(System.currentTimeMillis() / 100) % 2;
                renderer.setStrokeColor(frame == 0 ? Color.GREEN : Color.BLUE);
            } else if (renderer != null) {
                renderer.setStrokeColor(Color.GREEN);
            }
        }
    }

    /**
     * Find player entity in world
     *
     * @param world Game world
     * @return Player entity or null if not found
     */
    private Entity findPlayer(World world) {
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                return entity;
            }
        }
        return null;
    }

    /**
     * Process player movement based on input
     *
     * @param transform Player transform
     * @param movement Player movement component
     * @param deltaTime Time since last update
     */
    private void processMovement(TransformComponent transform, MovementComponent movement, float deltaTime) {
        Vector2D direction = new Vector2D(
                InputController.getAxis(Axis.HORIZONTAL),
                InputController.getAxis(Axis.VERTICAL)
        );

        // Normalize
        if (direction.magnitudeSquared() > 0.001f) {
            direction = direction.normalize();
            movement.setAccelerating(true);
        } else {
            movement.setAccelerating(false);
        }

        Vector2D velocity = direction.scale(movement.getSpeed() * deltaTime);
        transform.translate(velocity);
    }

    /**
     * Process player rotation to face mouse
     *
     * @param transform Player transform
     * @param gameData Game data
     */
    private void processRotation(TransformComponent transform, GameData gameData) {
        Vector2D mousePos = InputController.getMousePosition();

        Vector2D playerPos = transform.getPosition();
        Vector2D direction = mousePos.subtract(playerPos);

        float angle = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
        transform.setRotation(angle);
    }

    /**
     * Process player shooting based on input
     *
     * @param player Player entity
     * @param weapon Weapon component
     * @param gameData Current game data
     * @param world Game world
     */
    private void processShooting(Entity player, WeaponComponent weapon, GameData gameData, World world) {
        boolean shootPressed = InputController.isButtonPressed(Button.SPACE) ||
                InputController.isButtonPressed(Button.MOUSE1);

        if (shootPressed != weapon.isFiring()) {
            weapon.setFiring(shootPressed);
        }

        if (weapon.isFiring()) {
            weaponSPI.processFiring(player, gameData, world);
        }
    }
}