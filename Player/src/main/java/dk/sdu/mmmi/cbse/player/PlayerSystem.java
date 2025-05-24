package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.MovementComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.utils.Time;
import dk.sdu.mmmi.cbse.core.input.InputController;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for handling player input and controls with enhanced flicker support.
 */
public class PlayerSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(PlayerSystem.class.getName());

    private IWeaponSPI weaponSPI;
    private IPhysicsSPI physicsSPI;

    private static final float PLAYER_ACCELERATION = 800.0f;
    private static final float PLAYER_MAX_SPEED = 150.0f;
    private static final float PLAYER_DRAG = 0.92f; // Drag coefficient (0.92 = 8% drag per frame)
    private static final float INVULNERABILITY_FLICKER_DURATION = 3.0f; // 3 seconds

    public PlayerSystem() {
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);

        LOGGER.log(Level.INFO, "PlayerSystem initialized with weaponSPI: {0}, physicsSPI: {1}",
                new Object[]{
                        weaponSPI != null ? weaponSPI.getClass().getName() : "not available",
                        physicsSPI != null ? physicsSPI.getClass().getName() : "not available"
                });
    }

    @Override
    public int getPriority() {
        return 80; // Run before physics system
    }

    @Override
    public void update(GameData gameData, World world) {
        if (weaponSPI == null) {
            weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        }
        if (physicsSPI == null) {
            physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        }

        Entity player = findPlayer(world);
        if (player == null) {
            return;
        }

        // Required components
        TransformComponent transform = player.getComponent(TransformComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

        if (transform == null) {
            LOGGER.log(Level.WARNING, "Player missing required components");
            return;
        }

        if (physicsSPI != null && player.hasComponent(PhysicsComponent.class)) {
            processPhysicsMovement(player, transform);
        } else {
            processDirectMovement(player, transform);
        }

        processRotation(transform, gameData);

        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        if (weapon != null && weaponSPI != null) {
            processShooting(player, weapon, gameData, world);
        }

        if (playerComponent != null) {
            updatePlayerState(player, playerComponent);
        }
    }

    /**
     * Process physics-based movement with acceleration/deceleration
     * @param player Player entity
     * @param transform Player transform component
     */
    private void processPhysicsMovement(Entity player, TransformComponent transform) {
        Vector2D inputDirection = getInputDirection();

        if (inputDirection.magnitudeSquared() > 0.001f) {
            Vector2D force = inputDirection.scale(PLAYER_ACCELERATION);
            physicsSPI.applyForce(player, force);

            PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
            if (physics != null) {
                physics.setMaxSpeed(PLAYER_MAX_SPEED);
                physics.setDrag(PLAYER_DRAG);
            }
        }
        // Deceleration is handled by drag in PhysicsComponent
    }

    /**
     * Fallback direct movement (for backward compatibility)
     * @param player Player entity
     * @param transform Player transform component
     */
    private void processDirectMovement(Entity player, TransformComponent transform) {
        Vector2D direction = getInputDirection();

        MovementComponent movement = player.getComponent(MovementComponent.class);
        if (movement == null) {
            return;
        }

        float deltaTime = Time.getDeltaTimeF();

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
     * Get normalized input direction from player input
     * @return Normalized direction vector
     */
    private Vector2D getInputDirection() {
        float horizontal = 0;
        float vertical = 0;

        if (InputController.isButtonPressed(Button.LEFT)) horizontal -= 1;
        if (InputController.isButtonPressed(Button.RIGHT)) horizontal += 1;
        if (InputController.isButtonPressed(Button.UP)) vertical -= 1;
        if (InputController.isButtonPressed(Button.DOWN)) vertical += 1;

        Vector2D direction = new Vector2D(horizontal, vertical);

        // Normalize diagonal movement to prevent faster diagonal speed
        if (direction.magnitudeSquared() > 0.001f) {
            return direction.normalize();
        }

        return direction;
    }

    /**
     * Process player rotation to face mouse cursor
     * @param transform Player transform component
     * @param gameData Game data for screen information
     */
    private void processRotation(TransformComponent transform, GameData gameData) {
        Vector2D mousePos = InputController.getMousePosition();
        Vector2D playerPos = transform.getPosition();
        Vector2D direction = mousePos.subtract(playerPos);

        if (direction.magnitudeSquared() > 0.001f) {
            float angle = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
            transform.setRotation(angle);
        }
    }

    /**
     * Process player shooting based on input
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

        if (weapon.isFiring() && weapon.canFire() && weaponSPI != null) {
            Entity bullet = weaponSPI.shoot(player, gameData, weapon.getBulletType());
            if (bullet != null) {
                world.addEntity(bullet);
                LOGGER.log(Level.FINE, "Added bullet to world: {0}", bullet.getID());
            }
        }
    }

    /**
     * Update player state and visual feedback using FlickerUtility
     * @param player Player entity
     * @param playerComponent Player component
     */
    private void updatePlayerState(Entity player, PlayerComponent playerComponent) {
        float deltaTime = Time.getDeltaTimeF();

        playerComponent.updateInvulnerability();

        if (playerComponent.isInvulnerable()) {
            if (!FlickerUtility.isFlickering(player)) {
                FlickerUtility.startInvulnerabilityFlicker(player, INVULNERABILITY_FLICKER_DURATION);
            }
        } else {
            if (FlickerUtility.isFlickering(player)) {
                FlickerUtility.stopFlicker(player);
            }
        }

        FlickerUtility.updateFlicker(player, deltaTime);
    }

    /**
     * Find player entity in world
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
}