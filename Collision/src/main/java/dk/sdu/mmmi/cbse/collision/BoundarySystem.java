package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.IPluginService;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayer;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * System that creates boundary walls around screen edges.
 */
public class BoundarySystem implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(BoundarySystem.class.getName());

    private final List<Entity> boundaryWalls = new ArrayList<>();
    private static final float WALL_THICKNESS = 100.0f;
    private static final float WALL_EXTENSION = 50.0f;
    private static final boolean DEBUG_VISIBLE = false; // ToDo: Not here?

    @Override
    public void start(GameData gameData, World world) {
        LOGGER.log(Level.INFO, "BoundarySystem starting - creating boundary walls that only affect entities");

        createBoundaryWalls(gameData, world);

        LOGGER.log(Level.INFO, "BoundarySystem created {0} boundary walls using BOUNDARY layer",
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
     * Create boundary walls that only affect entities (player, enemies), not projectiles
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

            LOGGER.log(Level.FINE, "Added boundary wall: {0} using BOUNDARY collision layer", wall.getID());
        }
    }

    /**
     * Create a single boundary wall entity using BOUNDARY collision layer
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

        // Create collider components
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.BOUNDARY);

        // Create renderer components
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
                    renderer.setStrokeColor(Color.BLUE);  // Blue for boundaries
                    renderer.setFillColor(Color.TRANSPARENT);
                    renderer.setStrokeWidth(2.0f);
                    renderer.setRenderLayer(RenderLayer.UI);
                }
            }
        }
        LOGGER.log(Level.INFO, "Boundary debug visualization: {0}", visible ? "enabled" : "disabled");
    }

    /**
     * Get the number of boundary walls created
     */
    public int getBoundaryWallCount() {
        return boundaryWalls.size();
    }

    /**
     * Check if boundary system has been initialized
     */
    public boolean isInitialized() {
        return !boundaryWalls.isEmpty();
    }
}