package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.collision.CollisionGroup;
import dk.sdu.mmmi.cbse.common.collision.CollisionLayer;
import dk.sdu.mmmi.cbse.common.components.*;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.enemy.EnemyBehavior;
import dk.sdu.mmmi.cbse.common.enemy.EnemyProperties;
import dk.sdu.mmmi.cbse.common.enemy.IEnemyFactory;
import javafx.scene.paint.Color;

import java.util.Random;

public class EnemyFactory implements IEnemyFactory {
    private final Random rnd = new Random();

    @Override
    public Entity createEnemy(GameData gameData, EnemyBehavior behavior, EnemyProperties properties) {
        Entity enemy = new Entity();

        // Set shape, position and transform data
        TransformComponent transform = new TransformComponent();
        transform.setPolygonCoordinates(5,-5, -10,0, 5,5);
        transform.setRadius(8);
        transform.setX(rnd.nextDouble() * gameData.getDisplayWidth());
        transform.setY(rnd.nextDouble() * gameData.getDisplayHeight());
        enemy.addComponent(transform);

        // Add renderer component with color based on behavior
        RendererComponent renderer = new RendererComponent();
        renderer.setRenderLayer(300); // Enemies above asteroids, below player

        // Set color based on enemy type
        switch (behavior) {
            case PATROL:
                renderer.setStrokeColor(Color.ORANGE);
                break;
            case AGGRESSIVE:
                renderer.setStrokeColor(Color.RED);
                break;
            case DEFENSIVE:
                renderer.setStrokeColor(Color.PURPLE);
                break;
            case SNIPER:
                renderer.setStrokeColor(Color.DARKRED);
                break;
        }

        enemy.addComponent(renderer);

        // Add behavior component
        enemy.addComponent(new BehaviorComponent(behavior));

        // Add health component
        HealthComponent health = new HealthComponent(properties.getHealth());
        enemy.addComponent(health);

        // Add combat component
        CombatComponent combat = new CombatComponent();
        combat.setDamage(properties.getDamage());
        combat.setAttackRange(properties.getShootingRange());
        combat.setScoreValue(properties.getScoreValue());
        enemy.addComponent(combat);

        // Add AI component
        AIComponent ai = new AIComponent();
        ai.setDetectionRange(properties.getDetectionRange());
        ai.setFleeThreshold(0.3f); // Flee at 30% health
        enemy.addComponent(ai);

        // Add movement component
        MovementComponent movement = setupMovementForBehavior(behavior, properties);
        enemy.addComponent(movement);

        // Add collision component
        CollisionComponent collision = new CollisionComponent();
        collision.setLayer(CollisionLayer.ENEMY);
        collision.addGroup(CollisionGroup.HOSTILE);
        collision.addGroup(CollisionGroup.DESTRUCTIBLE);
        enemy.addComponent(collision);

        // Add tag component to identify as enemy
        enemy.addComponent(new TagComponent(TagComponent.TAG_ENEMY));

        return enemy;
    }

    private MovementComponent setupMovementForBehavior(EnemyBehavior behavior, EnemyProperties properties) {
        MovementComponent movement = new MovementComponent();

        switch (behavior) {
            case PATROL:
                movement.setPattern(MovementComponent.MovementPattern.LINEAR);
                movement.setSpeed(properties.getSpeed());
                movement.setRotationSpeed(0.5f);
                break;
            case AGGRESSIVE:
                movement.setPattern(MovementComponent.MovementPattern.HOMING);
                movement.setSpeed(properties.getSpeed());
                break;
            case DEFENSIVE:
                movement.setPattern(MovementComponent.MovementPattern.RANDOM);
                movement.setSpeed(properties.getSpeed());
                break;
            case SNIPER:
                movement.setPattern(MovementComponent.MovementPattern.LINEAR);
                movement.setSpeed(properties.getSpeed());
                break;
        }

        return movement;
    }
}