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
 * Collision detection system with tiered approach for optimal performance.
 */
public class CollisionDetector {
    private static final Logger LOGGER = Logger.getLogger(CollisionDetector.class.getName());
    private final CollisionLayerMatrix layerMatrix;

    public CollisionDetector() {
        this.layerMatrix = CollisionLayerMatrix.getInstance();
        LOGGER.log(Level.INFO, "CollisionDetector initialized");
    }

    /**
     * Detect all collisions in the world
     */
    public List<Pair<Entity, Entity>> detectCollisions(GameData gameData, World world) {
        List<Pair<Entity, Entity>> collisions = new ArrayList<>();
        List<Entity> collidableEntities = getCollidableEntities(world);

        // Check each entity against all others (optimized to avoid duplicate checks)
        for (int i = 0; i < collidableEntities.size(); i++) {
            Entity entity1 = collidableEntities.get(i);

            for (int j = i + 1; j < collidableEntities.size(); j++) {
                Entity entity2 = collidableEntities.get(j);

                if (canCollide(entity1, entity2) && isColliding(entity1, entity2)) {
                    // Use ordered pair to maintain consistent collision pair ordering
                    collisions.add(Pair.ordered(entity1, entity2));
                    LOGGER.log(Level.FINE, "Collision detected between entities {0} and {1}",
                            new Object[]{entity1.getID(), entity2.getID()});
                }
            }
        }

        return collisions;
    }

    /**
     * Tiered collision detection with optimizations for different entity types
     */
    public boolean isColliding(Entity entity1, Entity entity2) {
        TransformComponent transform1 = entity1.getComponent(TransformComponent.class);
        TransformComponent transform2 = entity2.getComponent(TransformComponent.class);

        if (transform1 == null || transform2 == null) {
            return false;
        }

        // 1: Circle-based test
        if (!circleCircleCollision(transform1, transform2)) {
            return false;  // Definitely not colliding
        }

        // 2: Handle bullet collisions (treat bullets as points)
        TagComponent tag1 = entity1.getComponent(TagComponent.class);
        TagComponent tag2 = entity2.getComponent(TagComponent.class);

        if (tag1 != null && tag1.hasType(EntityType.BULLET)) {
            return polygonPointCollision(transform2, transform1.getPosition());
        }

        if (tag2 != null && tag2.hasType(EntityType.BULLET)) {
            return polygonPointCollision(transform1, transform2.getPosition());
        }

        // 3: Full polygon collision for precision
        if (hasPolygonShape(transform1) && hasPolygonShape(transform2)) {
            return polygonPolygonCollision(transform1, transform2);
        }

        return true;
    }

    /**
     * Check if two entities can collide based on their collision layers
     */
    private boolean canCollide(Entity entity1, Entity entity2) {
        ColliderComponent collider1 = entity1.getComponent(ColliderComponent.class);
        ColliderComponent collider2 = entity2.getComponent(ColliderComponent.class);

        if (collider1 == null || collider2 == null) {
            return false;
        }

        return layerMatrix.canLayersCollide(collider1.getLayer(), collider2.getLayer());
    }

    /**
     * Circle-Circle collision test (fast broad-phase)
     */
    private boolean circleCircleCollision(TransformComponent t1, TransformComponent t2) {
        float dx = t1.getX() - t2.getX();
        float dy = t1.getY() - t2.getY();
        float distanceSquared = dx * dx + dy * dy;

        float radiiSum = t1.getRadius() + t2.getRadius();
        float radiiSumSquared = radiiSum * radiiSum;

        return distanceSquared <= radiiSumSquared;
    }

    /**
     * Check if a point is inside a polygon (for bullet collisions)
     */
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

    /**
     * Check collision between two polygon shapes
     */
    private boolean polygonPolygonCollision(TransformComponent t1, TransformComponent t2) {
        double[] poly1 = transformPolygonToWorld(t1);
        double[] poly2 = transformPolygonToWorld(t2);

        // Check if any line of poly1 intersects with any line of poly2
        int numPoints1 = poly1.length / 2;
        int numPoints2 = poly2.length / 2;

        for (int i = 0; i < numPoints1; i++) {
            int nextI = (i + 1) % numPoints1;
            float x1 = (float) poly1[i * 2];
            float y1 = (float) poly1[i * 2 + 1];
            float x2 = (float) poly1[nextI * 2];
            float y2 = (float) poly1[nextI * 2 + 1];

            for (int j = 0; j < numPoints2; j++) {
                int nextJ = (j + 1) % numPoints2;
                float x3 = (float) poly2[j * 2];
                float y3 = (float) poly2[j * 2 + 1];
                float x4 = (float) poly2[nextJ * 2];
                float y4 = (float) poly2[nextJ * 2 + 1];

                if (lineLineIntersection(x1, y1, x2, y2, x3, y3, x4, y4)) {
                    return true;  // Lines intersect
                }
            }
        }

        // Check if one polygon is completely inside the other
        return pointInsidePolygon(poly1[0], poly1[1], poly2) ||
                pointInsidePolygon(poly2[0], poly2[1], poly1);
    }

    /**
     * Check if line segments intersect
     */
    private boolean lineLineIntersection(float x1, float y1, float x2, float y2,
                                         float x3, float y3, float x4, float y4) {
        // Calculate the denominator of the intersection formula
        float denominator = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);

        // Lines are parallel if denominator is zero
        if (denominator == 0) {
            return false;
        }

        float uA = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denominator;
        float uB = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denominator;

        // If uA and uB are between 0-1, lines are intersecting
        return (uA >= 0 && uA <= 1 && uB >= 0 && uB <= 1);
    }

    /**
     * Check if a point is inside a polygon
     */
    private boolean pointInsidePolygon(double x, double y, double[] polygon) {
        boolean inside = false;
        int numPoints = polygon.length / 2;

        for (int i = 0, j = numPoints - 1; i < numPoints; j = i++) {
            float xi = (float) polygon[i * 2];
            float yi = (float) polygon[i * 2 + 1];
            float xj = (float) polygon[j * 2];
            float yj = (float) polygon[j * 2 + 1];

            boolean intersect = ((yi > y) != (yj > y)) &&
                    (x < (xj - xi) * (y - yi) / (yj - yi) + xi);

            if (intersect) {
                inside = !inside;
            }
        }

        return inside;
    }

    /**
     * Transform polygon local coordinates to world space
     */
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

    /**
     * Create a polygon approximation of a circle
     */
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

    /**
     * Check if transform has a polygon shape defined
     */
    private boolean hasPolygonShape(TransformComponent transform) {
        double[] coords = transform.getPolygonCoordinates();
        return coords != null && coords.length >= 6; // At least 3 points
    }

    /**
     * Get all entities that can collide
     */
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