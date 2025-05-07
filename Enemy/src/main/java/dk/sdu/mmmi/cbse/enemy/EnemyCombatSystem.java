package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.*;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.events.ShootEvent;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;
import dk.sdu.mmmi.cbse.common.services.IGameEventService;

import java.util.Random;
import java.util.ServiceLoader;

public class EnemyCombatSystem implements IEntityProcessingService {
    private final Random random = new Random();
    private final IGameEventService eventService;

    public EnemyCombatSystem() {
        this.eventService = ServiceLoader.load(IGameEventService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IGameEventService implementation found"));
    }

    @Override
    public void process(GameData gameData, World world) {
        for (Entity enemy : world.getEntities()) {
            // Skip entities that aren't enemies or missing required components
            if (!isEnemy(enemy) || !hasRequiredComponents(enemy)) {
                continue;
            }

            AIComponent ai = enemy.getComponent(AIComponent.class);
            CombatComponent combat = enemy.getComponent(CombatComponent.class);
            ShootingComponent shooting = enemy.getComponent(ShootingComponent.class);
            TransformComponent transform = enemy.getComponent(TransformComponent.class);

            // Update shooting cooldown
            if (shooting != null) {
                shooting.updateCooldown();
            }

            // Get target if available
            Entity target = ai.getTarget();
            if (target == null) continue;

            TransformComponent targetTransform = target.getComponent(TransformComponent.class);
            if (targetTransform == null) continue;

            // Check if target is in range and we can shoot
            float distanceToTarget = calculateDistance(transform, targetTransform);
            if (distanceToTarget <= combat.getAttackRange() &&
                    (shooting == null || shooting.isCanShoot()) &&
                    random.nextFloat() < 0.05) { // 5% chance to shoot per frame

                // Reset cooldown if we have a shooting component
                if (shooting != null) {
                    shooting.resetCooldown();
                }

                // Trigger shoot event
                eventService.publish(new ShootEvent(enemy));
            }
        }
    }

    private boolean isEnemy(Entity entity) {
        TagComponent tagComponent = entity.getComponent(TagComponent.class);
        return tagComponent != null && tagComponent.hasTag(TagComponent.TAG_ENEMY);
    }

    private boolean hasRequiredComponents(Entity entity) {
        return entity.hasComponent(AIComponent.class) &&
                entity.hasComponent(CombatComponent.class) &&
                entity.hasComponent(TransformComponent.class);
    }

    private float calculateDistance(TransformComponent t1, TransformComponent t2) {
        double dx = t2.getX() - t1.getX();
        double dy = t2.getY() - t1.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}