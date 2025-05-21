package dk.sdu.mmmi.cbse.common;

/**
 * Enum defining rendering layers for stacking on the z-axis.
 * Higher values are rendered on top of lower values.
 */
public enum RenderLayer {
    BACKGROUND(100),
    OBSTACLE(200),
    BULLET(300),
    ENEMY(400),
    PLAYER(500),
    EFFECT(600),
    UI(700);

    private final int value;

    RenderLayer(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}