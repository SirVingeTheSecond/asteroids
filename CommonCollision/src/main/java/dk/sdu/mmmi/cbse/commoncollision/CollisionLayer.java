package dk.sdu.mmmi.cbse.commoncollision;

/**
 * Defines layers for collision filtering.
 * Each layer represents a category of game objects for controlling which objects can collide with each other.
 */
public enum CollisionLayer {
    DEFAULT(0),
    PLAYER(1),
    ENEMY(2),
    ENEMY_PROJECTILE(3),
    PLAYER_PROJECTILE(4),
    OBSTACLE(5),
    BOUNDARY(6),
    TRIGGER(7),
    INVINCIBLE(8);

    private final int value;

    CollisionLayer(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public static CollisionLayer fromValue(int value) {
        for (CollisionLayer layer : values()) {
            if (layer.value == value) {
                return layer;
            }
        }
        return DEFAULT;
    }
}