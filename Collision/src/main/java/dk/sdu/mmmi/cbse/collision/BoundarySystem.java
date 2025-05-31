package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ILateUpdate;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import dk.sdu.mmmi.cbse.commonphysics.PhysicsComponent;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that creates boundary walls and enforces boundary collision.
 * Runs as both a plugin (for wall creation) and late update (for collision enforcement).
 */
public class BoundarySystem implements IPluginService, ILateUpdate {
    private static final Logger LOGGER = Logger.getLogger(BoundarySystem.class.getName());

    private final List<Entity> boundaryWalls = new ArrayList<>();
    private static final float WALL_THICKNESS = 100.0f;
    private static final float WALL_EXTENSION = 50.0f;
    private static final boolean DEBUG_VISIBLE = false;

    @Override
    public int getPriority() {
        return 150; // Run after physics but before rendering
    }

    @Override
    public void process(GameData gameData, World world) {
        // Enforce boundary collision for entities that should be contained
        for (Entity entity : world.getEntities()) {
            if (shouldEnforceBoundaryCollision(entity)) {
                enforceBoundaryCollision(entity, gameData);
            }
        }
    }

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "BoundarySystem starting - creating boundary walls and enabling collision enforcement");

        createBoundaryWalls(gameData, world);

        LOGGER.log(Level.INFO, "BoundarySystem created {0} boundary walls and will enforce boundary collision",
                boundaryWalls.size());
    }

    @Override
    public void stop(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "BoundarySystem stopping - removing {0} boundary walls", boundaryWalls.size());

        for (Entity wall : boundaryWalls) {
            world.removeEntity(wall);
        }
        boundaryWalls.clear();

        LOGGER.log(Level.INFO, "BoundarySystem stopped - all boundary walls removed");
    }

    /**
     * Determine if an entity should be constrained by boundaries
     */
    private boolean shouldEnforceBoundaryCollision(Entity entity) {
        if (!entity.hasComponent(ColliderComponent.class) ||
                !entity.hasComponent(TransformComponent.class) ||
                !entity.hasComponent(PhysicsComponent.class)) {
            return false;
        }

        ColliderComponent collider = entity.getComponent(ColliderComponent.class);
        CollisionLayer layer = collider.getLayer();

        return layer == CollisionLayer.PLAYER || layer == CollisionLayer.ENEMY;
        // INVINCIBLE (hunters) and OBSTACLE (asteroids) are not constrained
    }

    /**
     * Enforce boundary collision
     */
    private void enforceBoundaryCollision(Entity entity, GameData gameData) {
        TransformComponent transform = entity.getComponent(TransformComponent.class);
        PhysicsComponent physics = entity.getComponent(PhysicsComponent.class);

        if (transform == null || physics == null) {
            return;
        }

        Vector2D currentPosition = transform.getPosition();
        float radius = transform.getRadius();

        // Calculate screen boundaries
        float minX = radius;
        float maxX = gameData.getDisplayWidth() - radius;
        float minY = radius;
        float maxY = gameData.getDisplayHeight() - radius;

        // Clamp position to boundaries
        float clampedX = Math.max(minX, Math.min(maxX, currentPosition.x()));
        float clampedY = Math.max(minY, Math.min(maxY, currentPosition.y()));
        Vector2D clampedPosition = new Vector2D(clampedX, clampedY);

        // If position was clamped, adjust velocity to prevent accumulation against walls
        if (!clampedPosition.equals(currentPosition)) {
            transform.setPosition(clampedPosition);

            Vector2D velocity = physics.getVelocity();
            float newVelX = velocity.x();
            float newVelY = velocity.y();

            // Zero out velocity components that would push into the boundary
            if (clampedX != currentPosition.x()) {
                newVelX = 0; // Hit horizontal boundary
            }
            if (clampedY != currentPosition.y()) {
                newVelY = 0; // Hit vertical boundary
            }

            physics.setVelocity(new Vector2D(newVelX, newVelY));

            LOGGER.log(Level.FINE, "Entity {0} hit boundary - position clamped and velocity adjusted",
                    entity.getID());
        }
    }

    /**
     * Create boundary walls
     */
    private void createBoundaryWalls(GameData gameData, World world) {
        float screenWidth = gameData.getDisplayWidth();
        float screenHeight = gameData.getDisplayHeight();

        float extendedWidth = screenWidth + (2 * WALL_EXTENSION);
        float extendedHeight = screenHeight + (2 * WALL_EXTENSION);

        // Top wall
        Entity topWall = createBoundaryWall(
                "TopWall",
                screenWidth / 2, -WALL_THICKNESS / 2,
                extendedWidth, WALL_THICKNESS
        );

        // Bottom wall
        Entity bottomWall = createBoundaryWall(
                "BottomWall",
                screenWidth / 2, screenHeight + WALL_THICKNESS / 2,
                extendedWidth, WALL_THICKNESS
        );

        // Left wall
        Entity leftWall = createBoundaryWall(
                "LeftWall",
                -WALL_THICKNESS / 2, screenHeight / 2,
                WALL_THICKNESS, extendedHeight
        );

        // Right wall
        Entity rightWall = createBoundaryWall(
                "RightWall",
                screenWidth + WALL_THICKNESS / 2, screenHeight / 2,
                WALL_THICKNESS, extendedHeight
        );

        // Add walls to world and track them
        Entity[] walls = {topWall, bottomWall, leftWall, rightWall};
        for (Entity wall : walls) {
            world.addEntity(wall);
            boundaryWalls.add(wall);

            LOGGER.log(Level.FINE, "Added boundary wall: {0}", wall.getID());
        }
    }

    /**
     * Create a single boundary wall entity
     */
    private Entity createBoundaryWall(String name, float x, float y, float width, float height) {
        // Create rectangular shape coordinates
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        double[] shape = {
                -halfWidth, -halfHeight,  // Bottom-left
                halfWidth, -halfHeight,   // Bottom-right
                halfWidth,  halfHeight,   // Top-right
                -halfWidth,  halfHeight   // Top-left
        };

        // Create collider component
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.BOUNDARY);

        // Create renderer component
        RendererComponent renderer = new RendererComponent();
        renderer.setVisible(DEBUG_VISIBLE);
        if (DEBUG_VISIBLE) {
            renderer.setStrokeColor(Color.BLUE);
            renderer.setFillColor(Color.TRANSPARENT);
            renderer.setStrokeWidth(2.0f);
            renderer.setRenderLayer(RenderLayer.UI);
        }

        // Create boundary wall entity
        Entity wall = EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(x, y)
                .withRadius(Math.max(width, height) / 2)
                .withShape(shape)
                .with(collider)
                .with(renderer)
                .build();

        LOGGER.log(Level.FINE, "Created boundary wall '{0}' with BOUNDARY collision layer", name);

        return wall;
    }

    /**
     * Enable or disable debug visualization of boundaries
     */
    public void setDebugVisible(boolean visible) {
        for (Entity wall : boundaryWalls) {
            RendererComponent renderer = wall.getComponent(RendererComponent.class);
            if (renderer != null) {
                renderer.setVisible(visible);
                if (visible) {
                    renderer.setStrokeColor(Color.BLUE);
                    renderer.setFillColor(Color.TRANSPARENT);
                    renderer.setStrokeWidth(2.0f);
                    renderer.setRenderLayer(RenderLayer.UI);
                }
            }
        }
        LOGGER.log(Level.INFO, "Boundary debug visualization: {0}", visible ? "enabled" : "disabled");
    }
}