package dk.sdu.mmmi.cbse.enemy;

import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.enemy.IEnemySpawner;
import dk.sdu.mmmi.cbse.common.services.IEntityProcessingService;

import java.util.ServiceLoader;

public class EnemySpawnSystem implements IEntityProcessingService {
    private final IEnemySpawner spawner;

    public EnemySpawnSystem() {
        // Load spawner in constructor using ServiceLoader
        this.spawner = ServiceLoader.load(IEnemySpawner.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No IEnemySpawner implementation found"));
    }

    @Override
    public void process(GameData gameData, World world) {
        spawner.spawnEnemies(gameData, world);
    }
}
