package dk.sdu.mmmi.cbse.collision;

import dk.sdu.mmmi.cbse.common.Pair;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.TagComponent;
import dk.sdu.mmmi.cbse.common.components.TransformComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.data.World;
import dk.sdu.mmmi.cbse.commoncollision.ColliderComponent;
import dk.sdu.mmmi.cbse.commoncollision.CollisionLayerMatrix;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Collision detection system based on four different phases of collision checking:
 * <br>
 * 1. Spatial partitioning (broad-phase)
 * <br>
 * 2. Circle-circle test (medium-phase)
 * <br>
 * 3. Point-in-polygon for bullets (narrow-phase)
 * <br>
 * 4. SAT polygon-polygon (precise-phase)
 */
public class CollisionDetector {
    private static final Logger LOGGER = Logger.getLogger(CollisionDetector.class.getName());
    private final CollisionLayerMatrix layerMatrix;
    private final SpatialGrid spatialGrid;

    // Grid configuration for spatial partitioning
    private static final int GRID_SIZE = 64; // Cells of 64x64 pixels

    public CollisionDetector() {
        this.layerMatrix = CollisionLayerMatrix.getInstance();
        this.spatialGrid = new SpatialGrid(GRID_SIZE);
        LOGGER.log(Level.INFO, "CollisionDetector initialized with spatial partitioning");
    }

    /**
     * Detect all collisions using spatial partitioning for O(n) instead of O(n²)
     */
    public List<Pair<Entity, Entity>> detectCollisions(GameData gameData, World world) {
        List<Pair<Entity, Entity>> collisions = new ArrayList<>();
        List<Entity> collidableEntities = getCollidableEntities(world);

        // Early exit if too few entities
        if (collidableEntities.size() < 2) {
            return collisions;
        }

        // Update spatial grid
        spatialGrid.clear();
        spatialGrid.updateGrid(collidableEntities, gameData);

        // Check collisions only within same grid cells
        for (Entity entity1 : collidableEntities) {
            List<Entity> nearbyEntities = spatialGrid.getNearbyEntities(entity1);

            for (Entity entity2 : nearbyEntities) {
                // Avoid duplicate checks and self-collision
                if (System.identityHashCode(entity1) >= System.identityHashCode(entity2)) {
                    continue;
                }

                if (canCollide(entity1, entity2) && isColliding(entity1, entity2)) {
                    collisions.add(Pair.ordered(entity1, entity2));
                    LOGGER.log(Level.FINE, "Collision detected between entities {0} and {1}",
                            new Object[]{entity1.getID(), entity2.getID()});
                }
            }
        }

        return collisions;
    }

    /**
     * Optimized four-tier collision detection
     */
    public boolean isColliding(Entity entity1, Entity entity2) {
        TransformComponent transform1 = entity1.getComponent(TransformComponent.class);
        TransformComponent transform2 = entity2.getComponent(TransformComponent.class);

        if (transform1 == null || transform2 == null) {
            return false;
        }

        // Tier 1: Circle-based broad-phase test (fastest)
        if (!circleCircleCollision(transform1, transform2)) {
            return false;  // Definitely not colliding
        }

        // Tier 2: Handle bullet collisions (treat bullets as points)
        TagComponent tag1 = entity1.getComponent(TagComponent.class);
        TagComponent tag2 = entity2.getComponent(TagComponent.class);

        if (tag1 != null && tag1.hasType(EntityType.BULLET)) {
            return polygonPointCollision(transform2, transform1.getPosition());
        }

        if (tag2 != null && tag2.hasType(EntityType.BULLET)) {
            return polygonPointCollision(transform1, transform2.getPosition());
        }

        // Tier 3: SAT polygon collision for precision
        if (hasPolygonShape(transform1) && hasPolygonShape(transform2)) {
            return satPolygonCollision(transform1, transform2);
        }

        // Tier 4: Default to circle collision
        return true;
    }

    /**
     * Optimized SAT (Separating Axis Theorem) polygon collision
     * More efficient than line-line intersection for convex polygons
     */
    private boolean satPolygonCollision(TransformComponent t1, TransformComponent t2) {
        double[] poly1 = transformPolygonToWorld(t1);
        double[] poly2 = transformPolygonToWorld(t2);

        // Test separation on axes from polygon 1
        if (isSeparatedOnAxes(poly1, poly2)) {
            return false;
        }

        // Test separation on axes from polygon 2
        if (isSeparatedOnAxes(poly2, poly1)) {
            return false;
        }

        return true; // No separating axis found, polygons are colliding
    }

    /**
     * Test if polygons are separated on any axis of the first polygon
     */
    private boolean isSeparatedOnAxes(double[] poly1, double[] poly2) {
        int numPoints1 = poly1.length / 2;

        for (int i = 0; i < numPoints1; i++) {
            int next = (i + 1) % numPoints1;

            // Get edge vector
            double edgeX = poly1[next * 2] - poly1[i * 2];
            double edgeY = poly1[next * 2 + 1] - poly1[i * 2 + 1];

            // Get perpendicular (normal) vector
            double normalX = -edgeY;
            double normalY = edgeX;

            // Normalize the normal
            double length = Math.sqrt(normalX * normalX + normalY * normalY);
            if (length < 0.000001) continue;

            normalX /= length;
            normalY /= length;

            // Project both polygons onto this axis
            double[] proj1 = projectPolygon(poly1, normalX, normalY);
            double[] proj2 = projectPolygon(poly2, normalX, normalY);

            // Check for separation
            if (proj1[1] < proj2[0] || proj2[1] < proj1[0]) {
                return true; // Separated on this axis
            }
        }

        return false; // No separation found
    }

    /**
     * Project polygon onto axis and return [min, max] projection values
     */
    private double[] projectPolygon(double[] polygon, double axisX, double axisY) {
        int numPoints = polygon.length / 2;
        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;

        for (int i = 0; i < numPoints; i++) {
            double dot = polygon[i * 2] * axisX + polygon[i * 2 + 1] * axisY;
            min = Math.min(min, dot);
            max = Math.max(max, dot);
        }

        return new double[]{min, max};
    }

    // [Rest of the existing methods remain unchanged]
    private boolean canCollide(Entity entity1, Entity entity2) {
        ColliderComponent collider1 = entity1.getComponent(ColliderComponent.class);
        ColliderComponent collider2 = entity2.getComponent(ColliderComponent.class);

        if (collider1 == null || collider2 == null) {
            return false;
        }

        return layerMatrix.canLayersCollide(collider1.getLayer(), collider2.getLayer());
    }

    private boolean circleCircleCollision(TransformComponent t1, TransformComponent t2) {
        float dx = t1.getX() - t2.getX();
        float dy = t1.getY() - t2.getY();
        float distanceSquared = dx * dx + dy * dy;

        float radiiSum = t1.getRadius() + t2.getRadius();
        float radiiSumSquared = radiiSum * radiiSum;

        return distanceSquared <= radiiSumSquared;
    }

    private boolean polygonPointCollision(TransformComponent polygon, Vector2D point) {
        if (!hasPolygonShape(polygon)) {
            // Fall back to circle collision if no polygon defined
            float dx = polygon.getX() - point.x();
            float dy = polygon.getY() - point.y();
            float distanceSquared = dx * dx + dy * dy;
            return distanceSquared <= polygon.getRadius() * polygon.getRadius();
        }

        // Transform polygon coordinates to world space
        double[] worldCoords = transformPolygonToWorld(polygon);

        // Ray casting algorithm for point-in-polygon test
        boolean inside = false;
        int numPoints = worldCoords.length / 2;

        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            float xi = (float) worldCoords[i * 2];
            float yi = (float) worldCoords[i * 2 + 1];
            float xj = (float) worldCoords[j * 2];
            float yj = (float) worldCoords[j * 2 + 1];

            boolean intersect = ((yi > point.y()) != (yj > point.y())) &&
                    (point.x() < (xj - xi) * (point.y() - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    private double[] transformPolygonToWorld(TransformComponent transform) {
        double[] localCoords = transform.getPolygonCoordinates();

        if (localCoords == null) {
            // Create a circle approximation if no polygon defined
            localCoords = approximateCircleAsPolygon(transform.getRadius(), 8);
        }

        double[] worldCoords = new double[localCoords.length];
        float cos = (float) Math.cos(Math.toRadians(transform.getRotation()));
        float sin = (float) Math.sin(Math.toRadians(transform.getRotation()));

        for (int i = 0; i < localCoords.length / 2; i++) {
            // Rotate
            double x = localCoords[i * 2];
            double y = localCoords[i * 2 + 1];
            double rotX = x * cos - y * sin;
            double rotY = x * sin + y * cos;

            // Translate
            worldCoords[i * 2] = rotX + transform.getX();
            worldCoords[i * 2 + 1] = rotY + transform.getY();
        }

        return worldCoords;
    }

    private double[] approximateCircleAsPolygon(float radius, int sides) {
        double[] coords = new double[sides * 2];
        double angleStep = 2 * Math.PI / sides;

        for (int i = 0; i < sides; i++) {
            double angle = i * angleStep;
            coords[i * 2] = radius * Math.cos(angle);
            coords[i * 2 + 1] = radius * Math.sin(angle);
        }

        return coords;
    }

    private boolean hasPolygonShape(TransformComponent transform) {
        double[] coords = transform.getPolygonCoordinates();
        return coords != null && coords.length >= 6; // At least 3 points
    }

    private List<Entity> getCollidableEntities(World world) {
        List<Entity> collidableEntities = new ArrayList<>();

        for (Entity entity : world.getEntities()) {
            // Entity must have transform and collider components
            if (entity.hasComponent(TransformComponent.class) &&
                    entity.hasComponent(ColliderComponent.class)) {
                collidableEntities.add(entity);
            }
        }

        return collidableEntities;
    }
}