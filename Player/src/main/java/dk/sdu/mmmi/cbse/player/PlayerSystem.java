package dk.sdu.mmmi.cbse.player;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RecoilComponent;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.GameOverEvent;
import dk.sdu.mmmi.cbse.common.services.IEventService;
import dk.sdu.mmmi.cbse.common.services.IScoreSPI;
import dk.sdu.mmmi.cbse.common.services.IUpdate;
import dk.sdu.mmmi.cbse.common.utils.FlickerUtility;
import dk.sdu.mmmi.cbse.commonmovement.IMovementSPI;
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
 * System handling the Player.
 */
public class PlayerSystem implements IUpdate {
    private static final Logger LOGGER = Logger.getLogger(PlayerSystem.class.getName());

    // Movement config
    private static final float ACCELERATION_FORCE = 800.0f;
    private static final float MAX_SPEED = 160.0f;
    private static final float DRAG_COEFFICIENT = 0.88f;
    private static final float STOP_THRESHOLD = 5.0f;

    // Respawn config
    private static final float RESPAWN_INVINCIBILITY_TIME = 3.0f;

    private IWeaponSPI weaponSPI;
    private IPhysicsSPI physicsSPI;
    private IMovementSPI movementSPI;
    private IScoreSPI scoreSPI;
    private IEventService eventService;

    // Game over state tracking
    private boolean gameOverEventSent = false;

    public PlayerSystem() {
        this.weaponSPI = ServiceLoader.load(IWeaponSPI.class).findFirst().orElse(null);
        this.physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
        this.movementSPI = ServiceLoader.load(IMovementSPI.class).findFirst().orElse(null);  // Load movement service
        this.scoreSPI = ServiceLoader.load(IScoreSPI.class).findFirst().orElse(null);
        this.eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);

        LOGGER.log(Level.INFO, "PlayerSystem initialized");
    }

    @Override
    public int getPriority() {
        return 80;
    }

    @Override
    public void update(GameData gameData, World world) {
        // Refresh services if not available
        refreshServices();

        Entity player = findPlayer(world);
        if (player == null) return;

        TransformComponent transform = player.getComponent(TransformComponent.class);
        PlayerComponent playerComponent = player.getComponent(PlayerComponent.class);
        RecoilComponent recoil = player.getComponent(RecoilComponent.class);

        if (transform == null) return;

        // Check for game over condition
        if (playerComponent != null && isPlayerDeadForGood(playerComponent)) {
            triggerGameOverEvent(player);
            return; // Stop processing player when game is over
        }

        if (playerComponent != null && playerComponent.needsRespawn()) {
            handleRespawn(player, playerComponent, gameData);
        }

        processMovement(player, transform, recoil);
        processRotation(transform);
        processShooting(player, gameData, world);
        updatePlayerState(player, playerComponent, recoil);
    }

    private void refreshServices() {
        if (scoreSPI == null) {
            scoreSPI = ServiceLoader.load(IScoreSPI.class).findFirst().orElse(null);
            if (scoreSPI != null) {
                LOGGER.log(Level.INFO, "Score service became available: {0}", scoreSPI.getServiceInfo());
            }
        }

        if (eventService == null) {
            eventService = ServiceLoader.load(IEventService.class).findFirst().orElse(null);
            if (eventService != null) {
                LOGGER.log(Level.INFO, "Event service became available");
            }
        }

        // Refresh movement services if not available
        if (physicsSPI == null) {
            physicsSPI = ServiceLoader.load(IPhysicsSPI.class).findFirst().orElse(null);
            if (physicsSPI != null) {
                LOGGER.log(Level.INFO, "Physics service became available - switching to physics-based movement");
            }
        }

        if (movementSPI == null) {
            movementSPI = ServiceLoader.load(IMovementSPI.class).findFirst().orElse(null);
            if (movementSPI != null) {
                LOGGER.log(Level.INFO, "Movement service became available - enabling fallback movement");
            }
        }
    }

    private boolean isPlayerDeadForGood(PlayerComponent playerComponent) {
        return playerComponent.getLives() <= 0 && playerComponent.getCurrentHealth() <= 0;
    }

    private void triggerGameOverEvent(Entity player) {
        if (gameOverEventSent) return;

        gameOverEventSent = true;

        int finalScore = getFinalScore();
        String scoreSource = getScoreSourceInfo();

        LOGGER.log(Level.INFO, "GAME OVER! Player eliminated! Final score: {0} ({1})",
                new Object[]{finalScore, scoreSource});

        // lololol
        System.out.println("\n" + "=".repeat(60));
        System.out.println("                    GAME OVER!");
        System.out.println("         You are simply a waste of time!");
        System.out.println("              Final Score: " + finalScore);
        System.out.println("           This is clearly not for you");
        System.out.println("              Get lost, bozo...");
        System.out.println("=".repeat(60) + "\n");

        // Publish game over event for proper CBSE separation of concerns
        if (eventService != null) {
            GameOverEvent gameOverEvent = GameOverEvent.playerDeath(player, finalScore, scoreSource);
            eventService.publish(gameOverEvent);
            LOGGER.log(Level.INFO, "Published GameOverEvent - application lifecycle will be handled by Game class");
        } else {
            LOGGER.log(Level.SEVERE, "Cannot publish GameOverEvent - EventService not available!");
        }
    }

    /**
     * Get the final score from the authoritative source (microservice)
     */
    private int getFinalScore() {
        if (scoreSPI != null) {
            try {
                return scoreSPI.getCurrentScore();
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Failed to get final score from microservice, using fallback", e);
                return 0; // Fallback score
            }
        }
        LOGGER.log(Level.WARNING, "Score service not available for final score");
        return 0; // Fallback when no service
    }

    /**
     * Get information about the score source for logging
     */
    private String getScoreSourceInfo() {
        if (scoreSPI != null) {
            return scoreSPI.getServiceInfo();
        }
        return "No score service available";
    }

    /**
     * Handle player movement using service hierarchy:
     * 1. Physics-based movement (if available)
     * 2. Direct movement service (if available)
     * 3. No movement (if neither available)
     */
    private void processMovement(Entity player, TransformComponent transform, RecoilComponent recoil) {
        Vector2D inputDirection = getCleanInputDirection();

        // No input = no movement
        if (inputDirection.magnitudeSquared() <= 0.001f) {
            return;
        }

        // First priority: Physics-based movement
        if (physicsSPI != null && player.hasComponent(PhysicsComponent.class)) {
            processPhysicsMovement(player, inputDirection, recoil);
            return;
        }

        // Second priority: Movement service
        if (movementSPI != null && movementSPI.canMove(player)) {
            processServiceMovement(player, inputDirection, recoil);
            return;
        }

        // No movement services available
        LOGGER.log(Level.FINEST, "No movement services available for player");
    }

    /**
     * Process physics-based movement
     */
    private void processPhysicsMovement(Entity player, Vector2D inputDirection, RecoilComponent recoil) {
        PhysicsComponent physics = player.getComponent(PhysicsComponent.class);
        if (physics == null) {
            return;
        }

        // Configure physics based on recoil state
        physics.setMaxSpeed(MAX_SPEED);

        if (recoil != null && recoil.isInRecoil()) {
            processRecoilMovement(player, physics, recoil, inputDirection);
        } else {
            processNormalMovement(player, physics, inputDirection);
        }

        LOGGER.log(Level.FINEST, "Using physics-based movement");
    }

    /**
     * Process movement service-based movement
     */
    private void processServiceMovement(Entity player, Vector2D inputDirection, RecoilComponent recoil) {
        float speedMultiplier = 1.0f;

        // Apply recoil effects to movement speed
        if (recoil != null && recoil.isInRecoil()) {
            speedMultiplier = recoil.getInputStrength();
        }

        float deltaTime = Time.getDeltaTimeF();
        movementSPI.moveEntity(player, inputDirection, MAX_SPEED, speedMultiplier, deltaTime);

        LOGGER.log(Level.FINEST, "Using movement service (speed multiplier: {0})", speedMultiplier);
    }

    /**
     * Process movement during recoil using physics
     */
    private void processRecoilMovement(Entity player, PhysicsComponent physics, RecoilComponent recoil, Vector2D inputDirection) {
        // Apply recoil-modified drag
        float recoilDrag = recoil.getRecoilDrag(DRAG_COEFFICIENT);
        physics.setDrag(recoilDrag);

        // Apply input with recoil-modified strength
        float inputStrength = recoil.getInputStrength();
        Vector2D recoilModifiedForce = inputDirection.scale(ACCELERATION_FORCE * inputStrength);
        physicsSPI.applyForce(player, recoilModifiedForce);

        LOGGER.log(Level.FINEST, "Recoil movement - Phase: {0}, InputStrength: {1}, Drag: {2}",
                new Object[]{recoil.getRecoilPhase(), inputStrength, recoilDrag});
    }

    /**
     * Process normal movement when not in recoil using physics
     */
    private void processNormalMovement(Entity player, PhysicsComponent physics, Vector2D inputDirection) {
        physics.setDrag(DRAG_COEFFICIENT);

        Vector2D movementForce = inputDirection.scale(ACCELERATION_FORCE);
        physicsSPI.applyForce(player, movementForce);
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
     * Update player state
     */
    private void updatePlayerState(Entity player, PlayerComponent playerComponent, RecoilComponent recoil) {
        if (playerComponent != null) {
            playerComponent.updateInvulnerability();
            float deltaTime = Time.getDeltaTimeF();
            FlickerUtility.updateFlicker(player, deltaTime);
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

    /**
     * Handle player respawn
     */
    private void handleRespawn(Entity player, PlayerComponent playerComponent, GameData gameData) {
        TransformComponent transform = player.getComponent(TransformComponent.class);
        if (transform == null) {
            return;
        }

        // Reset position to screen center
        float centerX = gameData.getDisplayWidth() / 2.0f;
        float centerY = gameData.getDisplayHeight() / 2.0f;
        transform.setPosition(new Vector2D(centerX, centerY));

        // Reset velocity if physics enabled
        if (physicsSPI != null && physicsSPI.hasPhysics(player)) {
            physicsSPI.setVelocity(player, Vector2D.zero());
        }

        playerComponent.completeRespawn();

        FlickerUtility.startInvulnerabilityFlicker(player, RESPAWN_INVINCIBILITY_TIME);

        LOGGER.log(Level.INFO, "Player respawned with {0} lives remaining",
                playerComponent.getLives());
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

    /**
     * Check if game over event has been sent (for testing/debugging)
     */
    public boolean isGameOverEventSent() {
        return gameOverEventSent;
    }

    /**
     * Reset the game over state (for testing/debugging)
     */
    public void resetGameOverState() {
        gameOverEventSent = false;
    }
}