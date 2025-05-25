package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.commonphysics.IPhysicsSPI;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import dk.sdu.mmmi.cbse.commonplayer.PlayerComponent;
import dk.sdu.mmmi.cbse.commonweapon.IWeaponSPI;
import dk.sdu.mmmi.cbse.commonweapon.WeaponComponent;
import dk.sdu.mmmi.cbse.core.input.Button;
import dk.sdu.mmmi.cbse.core.input.InputController;
import dk.sdu.mmmi.cbse.core.utils.Time;

import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System handling the Player.
 */
public class PlayerSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(PlayerSystem.class.getName());

    // Movement config
    private static final float ACCELERATION_FORCE = 800.0f;
    private static final float MAX_SPEED = 160.0f;
    private static final float DRAG_COEFFICIENT = 0.88f;
    private static final float STOP_THRESHOLD = 5.0f;

    private IWeaponSPI weaponSPI;
    private IPhysicsSPI physicsSPI;

    public PlayerSystem() {
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        LOGGER.log(Level.INFO, "PlayerSystem initialized");
    }

    /**
     * Stopping logic that prevents any oscillation
     */
    private void applySmartStopping(Entity player, Vector2D currentVelocity, float currentSpeed) {
        if (currentSpeed < 0.1f) {
            physicsSPI.setVelocity(player, Vector2D.zero());
            return;
        }

        if (currentSpeed > STOP_THRESHOLD) {
            // Apply proportional stopping force - stronger at higher speeds
            float stopForceMultiplier = Math.min(0.8f, currentSpeed / MAX_SPEED);
            Vector2D stopForce = currentVelocity.normalize().scale(-ACCELERATION_FORCE * stopForceMultiplier);
            physicsSPI.applyForce(player, stopForce);

            LOGGER.log(Level.FINEST, "Applied stopping force: {0} (multiplier: {1})",
                    new Object[]{stopForce.magnitude(), stopForceMultiplier});
        } else {
            Vector2D dampedVelocity = currentVelocity.scale(0.85f);
            if (dampedVelocity.magnitude() < 0.5f) {
                dampedVelocity = Vector2D.zero(); // Complete stop
            }
            physicsSPI.setVelocity(player, dampedVelocity);

            LOGGER.log(Level.FINEST, "Applied velocity damping: {0} -> {1}",
                    new Object[]{currentVelocity.magnitude(), dampedVelocity.magnitude()});
        }
    }

    @Override
    public int getPriority () {
        return 80;
    }

    @Override
    public void update (GameData gameData, World world){
        Entity player = findPlayer(world);
        if (player == null) return;

        TransformComponent transform = player.getComponent(TransformComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);

        if (transform == null) return;

        processMovement(player, transform);

        processRotation(transform);

        processShooting(player, gameData, world);

        updatePlayerState(player, playerComponent);
    }

    /**
     * Movement using force
     */
    private void processMovement (Entity player, TransformComponent transform){
        if (physicsSPI == null) {
            processDirectMovement(transform);
            return;
        }

        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        if (physics == null) {
            processDirectMovement(transform);
            return;
        }

        // Configure physics for responsive movement
        physics.setMaxSpeed(MAX_SPEED);
        physics.setDrag(DRAG_COEFFICIENT);

        Vector2D inputDirection = getCleanInputDirection();
        Vector2D currentVelocity = physics.getVelocity();
        float currentSpeed = currentVelocity.magnitude();

        if (inputDirection.magnitudeSquared() > 0.001f) {
            Vector2D movementForce = inputDirection.scale(ACCELERATION_FORCE);
            physicsSPI.applyForce(player, movementForce);

            LOGGER.log(Level.FINEST, "Applied movement force: {0}, current speed: {1}",
                    new Object[]{movementForce.magnitude(), currentSpeed});
        } else {
            // Stopping logic to prevent jitter
            applySmartStopping(player, currentVelocity, currentSpeed);
        }
    }

    /**
     * Fallback movement for entities without physics
     */
    private void processDirectMovement (TransformComponent transform){
        Vector2D direction = getCleanInputDirection();

        if (direction.magnitudeSquared() > 0.001f) {
            float deltaTime = Time.getDeltaTimeF();
            Vector2D velocity = direction.scale(MAX_SPEED * deltaTime);
            transform.translate(velocity);
        }
    }

    /**
     * Get clean, normalized input direction
     */
    private Vector2D getCleanInputDirection () {
        float horizontal = 0;
        float vertical = 0;

        if (InputController.isButtonPressed(Button.LEFT)) horizontal -= 1;
        if (InputController.isButtonPressed(Button.RIGHT)) horizontal += 1;
        if (InputController.isButtonPressed(Button.UP)) vertical -= 1;
        if (InputController.isButtonPressed(Button.DOWN)) vertical += 1;

        Vector2D direction = new Vector2D(horizontal, vertical);

        // Normalize to prevent faster diagonal movement
        if (direction.magnitudeSquared() > 0.001f) {
            return direction.normalize();
        }
        return direction;
    }

    /**
     * Handle player rotation to face mouse cursor
     */
    private void processRotation (TransformComponent transform){
        Vector2D mousePos = InputController.getMousePosition();
        Vector2D playerPos = transform.getPosition();
        Vector2D direction = mousePos.subtract(playerPos);

        if (direction.magnitudeSquared() > 0.001f) {
            float angle = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
            transform.setRotation(angle);
        }
    }

    /**
     * Handle player shooting
     */
    private void processShooting (Entity player, GameData gameData, World world){
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        if (weapon == null || weaponSPI == null) return;

        boolean shootPressed = InputController.isButtonPressed(Button.SPACE) ||
                InputController.isButtonPressed(Button.MOUSE1);

        weapon.setFiring(shootPressed);

        if (weapon.isFiring() && weapon.canFire()) {
            Entity bullet = weaponSPI.shoot(player, gameData, weapon.getBulletType());
            if (bullet != null) {
                world.addEntity(bullet);
            }
        }
    }

    /**
     * Update player state and effects
     */
    private void updatePlayerState (Entity player, PlayerComponent playerComponent){
        if (playerComponent != null) {
            playerComponent.updateInvulnerability();

            // Handle invulnerability visual effects
            float deltaTime = Time.getDeltaTimeF();
            dk.sdu.mmmi.cbse.common.utils.FlickerUtility.updateFlicker(player, deltaTime);
        }
    }

    /**
     * Find player entity in world
     */
    private Entity findPlayer (World world){
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.PLAYER)) {
                return entity;
            }
        }
        return null;
    }
}