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
 * Plugin for enemy system.
 */
public class EnemyPlugin implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(EnemyPlugin.class.getName());
    private final List<Entity> enemies = new ArrayList<>();
    private final EnemyFactory enemyFactory;

    /**
     * Create a new enemy plugin
     */
    public EnemyPlugin() {
        this.enemyFactory = new EnemyFactory();
        LOGGER.log(Level.INFO, "EnemyPlugin initialized");
    }

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "EnemyPlugin starting");

        // Create initial enemies
        for (int i = 0; i < 3; i++) {
            EnemyType type = EnemyType.values()[i % EnemyType.values().length];
            Entity enemy = enemyFactory.createEnemy(type, gameData, world);
            world.addEntity(enemy);
            enemies.add(enemy);
        }

        LOGGER.log(Level.INFO, "Created {0} initial enemies", enemies.size());
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "EnemyPlugin stopping");

        // Remove tracked enemies
        for (Entity enemy : enemies) {
            world.removeEntity(enemy);
        }
        enemies.clear();

        // Remove any other enemies
        for (Entity entity : world.getEntities()) {
            TagComponent tag = entity.getComponent(TagComponent.class);
            if (tag != null && tag.hasType(EntityType.ENEMY)) {
                world.removeEntity(entity);
            }
        }

        LOGGER.log(Level.INFO, "All enemies removed");
    }
}