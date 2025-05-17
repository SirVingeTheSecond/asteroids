package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPostProcessingService;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayerMatrix;
import dk.sdu.mmmi.cbse.commoncollision.ICollisionSPI;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System for collision detection and resolution.
 */
public class CollisionSystem implements IPostProcessingService, ICollisionSPI {
    private static final Logger LOGGER = Logger.getLogger(CollisionSystem.class.getName());

    private final CollisionDetector detector;
    private final CollisionResolver resolver;

    /**
     * Create new collision system
     */
    public CollisionSystem() {
        this.detector = new CollisionDetector();
        this.resolver = new CollisionResolver();

        LOGGER.log(Level.INFO, "CollisionSystem initialized");
    }

    @Override
    public void process(GameData gameData, World world) {
        // Detect collisions
        List<Pair<Entity, Entity>> collisions = detector.detectCollisions(gameData, world);

        if (!collisions.isEmpty()) {
            LOGGER.log(Level.FINE, "Detected {0} collisions", collisions.size());

            // Resolve collisions and get entities to remove
            List<Entity> entitiesToRemove = resolver.resolveCollisions(collisions, gameData, world);

            // Remove entities marked for removal
            for (Entity entity : entitiesToRemove) {
                world.removeEntity(entity);
                LOGGER.log(Level.FINE, "Removed entity: {0}", entity.getID());
            }
        }
    }

    @Override
    public List<Pair<Entity, Entity>> detectCollisions(GameData gameData, World world) {
        return detector.detectCollisions(gameData, world);
    }

    @Override
    public boolean isColliding(Entity entity1, Entity entity2) {
        return detector.isColliding(entity1, entity2);
    }

    @Override
    public boolean canLayersCollide(CollisionLayer layer1, CollisionLayer layer2) {
        return CollisionLayerMatrix.getInstance().canLayersCollide(layer1, layer2);
    }
}