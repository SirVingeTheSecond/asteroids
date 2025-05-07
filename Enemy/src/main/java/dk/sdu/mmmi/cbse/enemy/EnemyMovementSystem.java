package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.*;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.enemy.EnemyBehavior;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;

import java.util.Random;

public class EnemyMovementSystem implements IEntityProcessingService {
    private static final float EDGE_MARGIN = 50.0f;
    private final Random random = new Random();

    @Override
    public void process(GameData gameData, World world) {
        for (Entity enemy : world.getEntities()) {
            // Skip entities that aren't enemies or missing required components
            if (!isEnemy(enemy) || !hasRequiredComponents(enemy)) {
                continue;
            }

            BehaviorComponent behaviorComponent = enemy.getComponent(BehaviorComponent.class);
            MovementComponent movement = enemy.getComponent(MovementComponent.class);
            TransformComponent transform = enemy.getComponent(TransformComponent.class);
            AIComponent ai = enemy.getComponent(AIComponent.class);

            // Get enemy behavior
            EnemyBehavior behavior = behaviorComponent.getBehaviorAs(EnemyBehavior.class);
            if (behavior == null) continue;

            // Get target if available
            Entity target = ai.getTarget();
            TransformComponent targetTransform =
                    target != null ? target.getComponent(TransformComponent.class) : null;

            // Apply movement based on behavior
            switch (behavior) {
                case PATROL:
                    processPacrolMovement(transform, movement, gameData);
                    break;
                case AGGRESSIVE:
                    if (targetTransform != null) {
                        processAggressiveMovement(transform, movement, targetTransform);
                    }
                    break;
                case DEFENSIVE:
                    if (targetTransform != null) {
                        processDefensiveMovement(transform, movement, targetTransform);
                    }
                    break;
                case SNIPER:
                    if (targetTransform != null) {
                        processSniperMovement(transform, movement, targetTransform);
                    }
                    break;
            }
        }
    }

    private boolean isEnemy(Entity entity) {
        TagComponent tagComponent = entity.getComponent(TagComponent.class);
        return tagComponent != null && tagComponent.hasTag(TagComponent.TAG_ENEMY);
    }

    private boolean hasRequiredComponents(Entity entity) {
        return entity.hasComponent(BehaviorComponent.class) &&
                entity.hasComponent(MovementComponent.class) &&
                entity.hasComponent(TransformComponent.class) &&
                entity.hasComponent(AIComponent.class);
    }

    private void processPacrolMovement(TransformComponent transform, MovementComponent movement, GameData gameData) {
        if (isNearEdge(transform, gameData)) {
            double currentRotation = transform.getRotation();
            currentRotation += 90 + random.nextInt(90);
            transform.setRotation(currentRotation % 360);
        }
        movement.setPattern(MovementComponent.MovementPattern.LINEAR);
    }

    private void processAggressiveMovement(TransformComponent transform, MovementComponent movement, TransformComponent target) {
        // Calculate angle to target
        double angle = calculateAngleToTarget(transform, target);
        transform.setRotation(angle);
        movement.setPattern(MovementComponent.MovementPattern.LINEAR);
    }

    private void processDefensiveMovement(TransformComponent transform, MovementComponent movement, TransformComponent target) {
        float distance = calculateDistance(transform, target);

        if (distance < 150) { // Too close, move away
            double angle = calculateAngleToTarget(transform, target);
            transform.setRotation((angle + 180) % 360); // Opposite direction
            movement.setPattern(MovementComponent.MovementPattern.LINEAR);
        } else {
            movement.setPattern(MovementComponent.MovementPattern.RANDOM);
        }
    }

    private void processSniperMovement(TransformComponent transform, MovementComponent movement, TransformComponent target) {
        // For snipers, just stop and aim at target
        movement.setSpeed(0);
        double angle = calculateAngleToTarget(transform, target);
        transform.setRotation(angle);
    }

    private boolean isNearEdge(TransformComponent transform, GameData gameData) {
        return transform.getX() < EDGE_MARGIN
                || transform.getX() > gameData.getDisplayWidth() - EDGE_MARGIN
                || transform.getY() < EDGE_MARGIN
                || transform.getY() > gameData.getDisplayHeight() - EDGE_MARGIN;
    }

    private double calculateAngleToTarget(TransformComponent source, TransformComponent target) {
        double dx = target.getX() - source.getX();
        double dy = target.getY() - source.getY();
        return Math.toDegrees(Math.atan2(dy, dx));
    }

    private float calculateDistance(TransformComponent t1, TransformComponent t2) {
        double dx = t2.getX() - t1.getX();
        double dy = t2.getY() - t1.getY();
        return (float) Math.sqrt(dx * dx + dy * dy);
    }
}