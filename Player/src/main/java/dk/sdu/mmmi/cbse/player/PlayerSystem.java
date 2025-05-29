package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RecoilComponent;
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

import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System handling the Player functionality.
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

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void update(GameData gameData, World world) {
        Entity player = findPlayer(world);
        if (player == null) return;

        TransformComponent transform = player.getComponent(TransformComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        RecoilComponent recoil = player.getComponent(RecoilComponent.class);

        if (transform == null) return;

        processMovement(player, transform, recoil);
        processRotation(transform);
        processShooting(player, gameData, world);
        updatePlayerState(player, playerComponent, recoil);
    }

    /**
     * Handle player movement
     */
    private void processMovement(Entity player, TransformComponent transform, RecoilComponent recoil) {
        if (physicsSPI == null) {
            processDirectMovement(transform, recoil);
            return;
        }

        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        if (physics == null) {
            processDirectMovement(transform, recoil);
            return;
        }

        // Configure physics based on recoil state
        physics.setMaxSpeed(MAX_SPEED);

        if (recoil != null && recoil.isInRecoil()) {
            processRecoilMovement(player, physics, recoil);
        } else {
            processNormalMovement(player, physics);
        }
    }

    /**
     * Process movement during recoil
     */
    private void processRecoilMovement(Entity player, PhysicsComponent physics, RecoilComponent recoil) {
        // Apply recoil-modified drag
        float recoilDrag = recoil.getRecoilDrag(DRAG_COEFFICIENT);
        physics.setDrag(recoilDrag);

        // Get input with reduced strength
        Vector2D inputDirection = getCleanInputDirection();
        Vector2D currentVelocity = physics.getVelocity();
        float currentSpeed = currentVelocity.magnitude();

        if (inputDirection.magnitudeSquared() > 0.001f) {
            // Apply input with recoil-modified strength
            float inputStrength = recoil.getInputStrength();
            Vector2D recoilModifiedForce = inputDirection.scale(ACCELERATION_FORCE * inputStrength);
            physicsSPI.applyForce(player, recoilModifiedForce);

            LOGGER.log(Level.FINEST, "Recoil movement - Phase: {0}, InputStrength: {1}, Drag: {2}",
                    new Object[]{recoil.getRecoilPhase(), inputStrength, recoilDrag});
        } else {
            // Apply smart stopping during recoil
            applyRecoilStopping(player, currentVelocity, currentSpeed, recoil);
        }
    }

    /**
     * Process normal movement when not in recoil
     */
    private void processNormalMovement(Entity player, PhysicsComponent physics) {
        physics.setDrag(DRAG_COEFFICIENT);

        Vector2D inputDirection = getCleanInputDirection();
        Vector2D currentVelocity = physics.getVelocity();
        float currentSpeed = currentVelocity.magnitude();

        if (inputDirection.magnitudeSquared() > 0.001f) {
            Vector2D movementForce = inputDirection.scale(ACCELERATION_FORCE);
            physicsSPI.applyForce(player, movementForce);
        } else {
            applySmartStopping(player, currentVelocity, currentSpeed);
        }
    }

    /**
     * Apply stopping force during recoil
     */
    private void applyRecoilStopping(Entity player, Vector2D currentVelocity, float currentSpeed, RecoilComponent recoil) {
        if (currentSpeed < 0.1f) {
            physicsSPI.setVelocity(player, Vector2D.zero());
            return;
        }

        // During recoil, apply soft stopping to let recoil play out
        float inputStrength = recoil.getInputStrength();
        float stoppingStrength = inputStrength * 0.5f;

        if (currentSpeed > STOP_THRESHOLD) {
            float stopForceMultiplier = Math.min(0.4f, currentSpeed / MAX_SPEED) * stoppingStrength;
            Vector2D stopForce = currentVelocity.normalize().scale(-ACCELERATION_FORCE * stopForceMultiplier);
            physicsSPI.applyForce(player, stopForce);
        } else {
            Vector2D dampedVelocity = currentVelocity.scale(0.92f + (inputStrength * 0.05f)); // Gentler damping
            if (dampedVelocity.magnitude() < 0.5f) {
                dampedVelocity = Vector2D.zero();
            }
            physicsSPI.setVelocity(player, dampedVelocity);
        }
    }

    /**
     * Apply smart stopping for normal movement
     */
    private void applySmartStopping(Entity player, Vector2D currentVelocity, float currentSpeed) {
        if (currentSpeed < 0.1f) {
            physicsSPI.setVelocity(player, Vector2D.zero());
            return;
        }

        if (currentSpeed > STOP_THRESHOLD) {
            float stopForceMultiplier = Math.min(0.8f, currentSpeed / MAX_SPEED);
            Vector2D stopForce = currentVelocity.normalize().scale(-ACCELERATION_FORCE * stopForceMultiplier);
            physicsSPI.applyForce(player, stopForce);
        } else {
            Vector2D dampedVelocity = currentVelocity.scale(0.85f);
            if (dampedVelocity.magnitude() < 0.5f) {
                dampedVelocity = Vector2D.zero();
            }
            physicsSPI.setVelocity(player, dampedVelocity);
        }
    }

    /**
     * Process direct movement (when no physics available)
     */
    private void processDirectMovement(TransformComponent transform, RecoilComponent recoil) {
        Vector2D direction = getCleanInputDirection();
        if (direction.magnitudeSquared() > 0.001f) {
            float deltaTime = Time.getDeltaTimeF();
            float speedMultiplier = 1.0f;

            // Reduce movement speed during recoil
            if (recoil != null && recoil.isInRecoil()) {
                speedMultiplier = recoil.getInputStrength();
            }

            Vector2D velocity = direction.scale(MAX_SPEED * deltaTime * speedMultiplier);
            transform.translate(velocity);
        }
    }

    /**
     * Handle player shooting
     */
    private void processShooting(Entity player, GameData gameData, World world) {
        WeaponComponent weapon = player.getComponent(WeaponComponent.class);
        if (weapon == null || weaponSPI == null) return;

        boolean shootPressed = InputController.isButtonPressed(Button.SPACE) ||
                InputController.isButtonPressed(Button.MOUSE1);
        boolean shootJustPressed = InputController.isButtonDown(Button.SPACE) ||
                InputController.isButtonDown(Button.MOUSE1);

        weapon.setFiring(shootPressed);

        boolean shouldFire = false;

        switch (weapon.getFiringPattern()) {
            case BURST:
                // Burst weapons fire on button press (not hold)
                if (shootJustPressed && weapon.canFire()) {
                    weapon.triggerFire();
                    shouldFire = true;
                }
                break;

            case AUTOMATIC:
            case HEAVY:
            case SHOTGUN:
            default:
                // Other weapons fire while held
                shouldFire = weapon.isFiring() && weapon.canFire();
                break;
        }

        if (shouldFire) {
            List<Entity> bullets = weaponSPI.shoot(player, gameData, weapon.getBulletType());

            for (Entity bullet : bullets) {
                world.addEntity(bullet);
            }

            if (!bullets.isEmpty()) {
                LOGGER.log(Level.FINE, "Player fired {0} bullets with {1} weapon",
                        new Object[]{bullets.size(), weapon.getFiringPattern()});
            }
        }
    }

    private void processRotation(TransformComponent transform) {
        Vector2D mousePos = InputController.getMousePosition();
        Vector2D playerPos = transform.getPosition();
        Vector2D direction = mousePos.subtract(playerPos);

        if (direction.magnitudeSquared() > 0.001f) {
            float angle = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
            transform.setRotation(angle);
        }
    }

    /**
     * Update player state including recoil management
     */
    private void updatePlayerState(Entity player, PlayerComponent playerComponent, RecoilComponent recoil) {
        if (playerComponent != null) {
            playerComponent.updateInvulnerability();
            float deltaTime = Time.getDeltaTimeF();
            dk.sdu.mmmi.cbse.common.utils.FlickerUtility.updateFlicker(player, deltaTime);
        }

        // Update recoil state
        if (recoil != null) {
            recoil.updateRecoil(Time.getDeltaTimeF());
        }
    }

    private Vector2D getCleanInputDirection() {
        float horizontal = 0;
        float vertical = 0;

        if (InputController.isButtonPressed(Button.LEFT)) horizontal -= 1;
        if (InputController.isButtonPressed(Button.RIGHT)) horizontal += 1;
        if (InputController.isButtonPressed(Button.UP)) vertical -= 1;
        if (InputController.isButtonPressed(Button.DOWN)) vertical += 1;

        Vector2D direction = new Vector2D(horizontal, vertical);
        if (direction.magnitudeSquared() > 0.001f) {
            return direction.normalize();
        }
        return direction;
    }

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