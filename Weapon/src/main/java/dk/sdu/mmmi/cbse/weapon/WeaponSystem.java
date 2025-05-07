package dk.sdu.mmmi.cbse.weapon;

import dk.sdu.mmmi.cbse.common.Time;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.bullet.BulletSPI;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.IGameEventListener;
import dk.sdu.mmmi.cbse.common.events.ShootEvent;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;
import dk.sdu.mmmi.cbse.common.util.ServiceLocator;

import java.util.ArrayList;
import java.util.List;

/**
 * System that processes bullets.
 * Handles bullet movement, lifetime, and creation from shoot events.
 */
public class WeaponSystem implements IEntityProcessingService, IGameEventListener<ShootEvent> {
    private final BulletSPI bulletSPI;
    private final IGameEventService eventService;
    private World world;
    private GameData gameData;

    /**
     * Create a new bullet system
     */
    public WeaponSystem() {
        // Get required services
        this.bulletSPI = ServiceLocator.getService(BulletSPI.class);
        this.eventService = ServiceLocator.getService(IGameEventService.class);

        // Register for ShootEvents
        eventService.addListener(ShootEvent.class, this);
    }

    @Override
    public void process(GameData gameData, World world) {
        // Store references for use in event handling
        this.world = world;
        this.gameData = gameData;

        float deltaTime = (float) Time.getDeltaTime();
        List<Entity> bulletsToRemove = new ArrayList<>();

        // Process all bullet entities
        for (Entity entity : world.getEntities()) {
            // Skip if not a bullet
            TagComponent tagComponent = entity.getComponent(TagComponent.class);
            if (tagComponent == null || !tagComponent.hasType(EntityType.BULLET)) {
                continue;
            }

            // Get required components
            BulletComponent bulletComponent = entity.getComponent(BulletComponent.class);
            TransformComponent transform = entity.getComponent(TransformComponent.class);

            if (bulletComponent == null || transform == null) {
                continue;
            }

            // Update position based on bullet speed and direction
            Vector2D forward = transform.getForward();
            Vector2D velocity = forward.scale(bulletComponent.getSpeed() * deltaTime);
            transform.translate(velocity);

            // Handle bullet lifetime
            bulletComponent.setRemainingLifetime(bulletComponent.getRemainingLifetime() - 1);

            // Remove bullets that are either expired or out of bounds
            if (bulletComponent.getRemainingLifetime() <= 0 || isOutOfBounds(transform, gameData)) {
                bulletsToRemove.add(entity);
            }
        }

        // Remove expired bullets
        bulletsToRemove.forEach(world::removeEntity);
    }

    /**
     * Check if bullet is out of the game area
     */
    private boolean isOutOfBounds(TransformComponent transform, GameData gameData) {
        float x = transform.getX();
        float y = transform.getY();

        return x < -50 || x > gameData.getDisplayWidth() + 50 ||
                y < -50 || y > gameData.getDisplayHeight() + 50;
    }

    @Override
    public void onEvent(ShootEvent event) {
        if (world == null || gameData == null) {
            return; // Not initialized yet
        }

        Entity shooter = event.getSource();
        if (shooter == null) {
            return; // Invalid event
        }

        // Create a bullet based on the shooter
        Entity bullet = bulletSPI.createBullet(shooter, gameData);
        if (bullet != null) {
            // If the event has a direction override, apply it
            if (event.hasDirectionOverride()) {
                TransformComponent bulletTransform = bullet.getComponent(TransformComponent.class);
                if (bulletTransform != null) {
                    bulletTransform.setRotation(event.getDirection());
                }
            }

            world.addEntity(bullet);
        }
    }
}