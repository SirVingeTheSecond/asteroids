package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.commonenemy.EnemyType;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Plugin for Enemy.
 */
public class EnemyPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(EnemyPlugin.class.getName());
    private final List<Entity> initialEnemies = new ArrayList<>();
    private final EnemyFactory enemyFactory;

    public EnemyPlugin() {
        this.enemyFactory = new EnemyFactory();
        LOGGER.log(Level.INFO, "EnemyPlugin initialized for HUNTER and TURRET enemies");
    }

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "EnemyPlugin starting - creating initial enemies");

        // Create one of each type as initial enemies
        Entity hunter = enemyFactory.createEnemy(EnemyType.HUNTER, gameData, world);
        Entity turret = enemyFactory.createEnemy(EnemyType.TURRET, gameData, world);

        world.addEntity(hunter);
        world.addEntity(turret);

        initialEnemies.add(hunter);
        initialEnemies.add(turret);

        LOGGER.log(Level.INFO, "Created {0} initial enemies (1 HUNTER, 1 TURRET)", initialEnemies.size());
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "EnemyPlugin stopping - removing all enemies");

        // Remove tracked initial enemies
        for (Entity enemy : initialEnemies) {
            world.removeEntity(enemy);
        }
        initialEnemies.clear();

        // Remove any other enemies that may have spawned
        List<Entity> enemiesToRemove = new ArrayList<>();
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.ENEMY)) {
                enemiesToRemove.add(entity);
            }
        }

        for (Entity enemy : enemiesToRemove) {
            world.removeEntity(enemy);
        }

        LOGGER.log(Level.INFO, "Removed {0} total enemies", enemiesToRemove.size());
    }
}