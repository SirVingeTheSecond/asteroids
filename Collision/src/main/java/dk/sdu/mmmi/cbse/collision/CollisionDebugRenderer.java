// CollisionGridVisualizer.java (improved)
package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.collision.CollisionComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.common.services.ICollisionService;
import dk.sdu.mmmi.cbse.common.services.IDebugService;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ServiceLoader;

public class CollisionDebugRenderer implements IDebugService {
    private static final int CELL_SIZE = 64;
    private boolean enabled = false;
    private final ICollisionService collisionService;

    public CollisionDebugRenderer() {
        this.collisionService = ServiceLoader.load(ICollisionService.class)
                .findFirst()
                .orElseThrow(() -> new RuntimeException("No ICollisionService implementation found"));
    }

    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        // Also enable debug for the collision service itself
        if (collisionService != null) {
            collisionService.setDebugEnabled(enabled);
        }
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public void render(GraphicsContext gc, GameData gameData, World world) {
        if (!enabled) return;

        drawGrid(gc, gameData);
        drawCollisionEntities(gc, world);
    }

    private void drawGrid(GraphicsContext gc, GameData gameData) {
        // Draw grid lines
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(0.5);

        // Vertical lines
        for (int x = 0; x <= gameData.getDisplayWidth(); x += CELL_SIZE) {
            gc.strokeLine(x, 0, x, gameData.getDisplayHeight());
        }

        // Horizontal lines
        for (int y = 0; y <= gameData.getDisplayHeight(); y += CELL_SIZE) {
            gc.strokeLine(0, y, gameData.getDisplayWidth(), y);
        }
    }

    private void drawCollisionEntities(GraphicsContext gc, World world) {
        for (Entity entity : world.getEntities()) {
            CollisionComponent cc = entity.getComponent(CollisionComponent.class);
            TransformComponent transform = entity.getComponent(TransformComponent.class);

            if (cc == null || transform == null || !cc.isActive()) {
                continue;
            }

            // Highlight cell containing entity
            int cellX = (int) (transform.getX() / CELL_SIZE);
            int cellY = (int) (transform.getY() / CELL_SIZE);

            gc.setFill(Color.rgb(0, 255, 0, 0.15));
            gc.fillRect(
                    cellX * CELL_SIZE,
                    cellY * CELL_SIZE,
                    CELL_SIZE,
                    CELL_SIZE
            );

            // Draw collision radius
            gc.setStroke(Color.RED);
            gc.setLineWidth(1.5);
            gc.strokeOval(
                    transform.getX() - transform.getRadius(),
                    transform.getY() - transform.getRadius(),
                    transform.getRadius() * 2,
                    transform.getRadius() * 2
            );
        }
    }
}