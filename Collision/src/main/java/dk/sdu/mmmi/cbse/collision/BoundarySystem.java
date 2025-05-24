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
 * System that creates invisible boundary walls around the screen edges.
 */
public class BoundarySystem implements IPluginService {
    private static final Logger LOGGER = Logger.getLogger(BoundarySystem.class.getName());

    private final List<Entity> boundaryWalls = new ArrayList<>();
    private static final float WALL_THICKNESS = 50.0f;
    private static final boolean DEBUG_VISIBLE = false; // Set to true to see boundaries

    @Override
    public void start(GameData gameData, World world) {
        createBoundaryWalls(gameData, world);
        LOGGER.log(Level.INFO, "BoundarySystem created {0} boundary walls", boundaryWalls.size());
    }

    @Override
    public void stop(GameData gameData, World world) {
        // Remove all boundary walls
        for (Entity wall : boundaryWalls) {
            world.removeEntity(wall);
        }
        boundaryWalls.clear();
        LOGGER.log(Level.INFO, "BoundarySystem removed all boundary walls");
    }

    /**
     * Create invisible walls around the screen perimeter
     */
    private void createBoundaryWalls(GameData gameData, World world) {
        float screenWidth = gameData.getDisplayWidth();
        float screenHeight = gameData.getDisplayHeight();

        // Top wall
        Entity topWall = createWall(
                screenWidth / 2, -WALL_THICKNESS / 2,
                screenWidth, WALL_THICKNESS
        );

        // Bottom wall
        Entity bottomWall = createWall(
                screenWidth / 2, screenHeight + WALL_THICKNESS / 2,
                screenWidth, WALL_THICKNESS
        );

        // Left wall
        Entity leftWall = createWall(
                -WALL_THICKNESS / 2, screenHeight / 2,
                WALL_THICKNESS, screenHeight
        );

        // Right wall
        Entity rightWall = createWall(
                screenWidth + WALL_THICKNESS / 2, screenHeight / 2,
                WALL_THICKNESS, screenHeight
        );

        // Add walls to world and track them
        Entity[] walls = {topWall, bottomWall, leftWall, rightWall};
        for (Entity wall : walls) {
            world.addEntity(wall);
            boundaryWalls.add(wall);
        }
    }

    /**
     * Create a single boundary wall entity
     */
    private Entity createWall(float x, float y, float width, float height) {
        // Create rectangular shape coordinates
        float halfWidth = width / 2;
        float halfHeight = height / 2;
        double[] shape = {
                -halfWidth, -halfHeight,  // Top-left
                halfWidth, -halfHeight,  // Top-right
                halfWidth,  halfHeight,  // Bottom-right
                -halfWidth,  halfHeight   // Bottom-left
        };

        // Create collider component
        ColliderComponent collider = new ColliderComponent();
        collider.setLayer(CollisionLayer.OBSTACLE);

        // Create renderer component (invisible unless debug mode)
        RendererComponent renderer = new RendererComponent();
        renderer.setVisible(DEBUG_VISIBLE);
        if (DEBUG_VISIBLE) {
            renderer.setStrokeColor(Color.RED);
            renderer.setFillColor(Color.TRANSPARENT);
            renderer.setStrokeWidth(2.0f);
            renderer.setRenderLayer(RenderLayer.UI);
        }

        // Create wall entity
        Entity wall = EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(x, y)
                .withRadius(Math.max(width, height) / 2) // larger for broad-phase collision
                .withShape(shape)
                .with(collider)
                .with(renderer)
                .build();

        LOGGER.log(Level.FINE, "Created boundary wall at ({0}, {1}) with size ({2}, {3})",
                new Object[]{x, y, width, height});

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
            }
        }
        LOGGER.log(Level.INFO, "Boundary debug visualization: {0}", visible ? "enabled" : "disabled");
    }
}