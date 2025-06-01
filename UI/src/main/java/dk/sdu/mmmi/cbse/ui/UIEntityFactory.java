package dk.sdu.mmmi.cbse.ui;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.RendererComponent;
import dk.sdu.mmmi.cbse.common.data.Entity;
import dk.sdu.mmmi.cbse.common.data.EntityType;
import dk.sdu.mmmi.cbse.common.data.GameData;
import dk.sdu.mmmi.cbse.common.utils.EntityBuilder;
import dk.sdu.mmmi.cbse.commonui.UIAnchor;
import dk.sdu.mmmi.cbse.commonui.UIComponent;
import dk.sdu.mmmi.cbse.commonui.UIType;
import javafx.scene.paint.Color;

/**
 * Factory for creating UI entities.
 */
public class UIEntityFactory {

    public Entity createHealthDisplay(GameData gameData) {
        Vector2D position = calculateAnchoredPosition(UIAnchor.TOP_LEFT,
                new Vector2D(20, 30), gameData);

        UIComponent uiComponent = new UIComponent(UIType.HEALTH_DISPLAY);
        uiComponent.setAnchor(UIAnchor.TOP_LEFT);
        uiComponent.setOffset(new Vector2D(20, 30));
        uiComponent.setFontSize(18);
        uiComponent.setDisplayText("Health: ♥ ♥ ♥");

        RendererComponent renderer = createUIRenderer(Color.WHITE);

        return EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(position)
                .with(uiComponent)
                .with(renderer)
                .build();
    }

    public Entity createLivesDisplay(GameData gameData) {
        Vector2D position = calculateAnchoredPosition(UIAnchor.TOP_LEFT,
                new Vector2D(20, 60), gameData);

        UIComponent uiComponent = new UIComponent(UIType.LIVES_DISPLAY);
        uiComponent.setAnchor(UIAnchor.TOP_LEFT);
        uiComponent.setOffset(new Vector2D(20, 60));
        uiComponent.setFontSize(18);
        uiComponent.setDisplayText("Lives: 3");

        RendererComponent renderer = createUIRenderer(Color.WHITE);

        return EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(position)
                .with(uiComponent)
                .with(renderer)
                .build();
    }

    public Entity createWeaponDisplay(GameData gameData) {
        // Estimate text width for centering (approximate character width * expected text length)
        String defaultText = "Weapon: AUTOMATIC";
        int fontSize = 18;
        float estimatedTextWidth = estimateTextWidth(defaultText, fontSize);
        float centeringOffset = -estimatedTextWidth / 2.0f;

        Vector2D position = calculateAnchoredPosition(UIAnchor.BOTTOM_CENTER,
                new Vector2D(centeringOffset, -30), gameData);

        UIComponent uiComponent = new UIComponent(UIType.WEAPON_DISPLAY);
        uiComponent.setAnchor(UIAnchor.BOTTOM_CENTER);
        uiComponent.setOffset(new Vector2D(centeringOffset, -30));
        uiComponent.setFontSize(fontSize);
        uiComponent.setDisplayText(defaultText);

        RendererComponent renderer = createUIRenderer(Color.LIGHTBLUE);

        return EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(position)
                .with(uiComponent)
                .with(renderer)
                .build();
    }

    /**
     * Create score display
     */
    public Entity createScoreDisplay(GameData gameData) {
        Vector2D position = calculateAnchoredPosition(UIAnchor.TOP_RIGHT,
                new Vector2D(-150, 30), gameData);

        UIComponent uiComponent = new UIComponent(UIType.SCORE_DISPLAY);
        uiComponent.setAnchor(UIAnchor.TOP_RIGHT);
        uiComponent.setOffset(new Vector2D(-150, 30));
        uiComponent.setFontSize(20);
        uiComponent.setDisplayText("Score: 0 ★"); // ★ = microservice, ⚠ = fallback

        RendererComponent renderer = createUIRenderer(Color.GOLD);

        return EntityBuilder.create()
                .withType(EntityType.OBSTACLE)
                .atPosition(position)
                .with(uiComponent)
                .with(renderer)
                .build();
    }

    private RendererComponent createUIRenderer(Color color) {
        RendererComponent renderer = new RendererComponent();
        renderer.setShapeType(RendererComponent.ShapeType.TEXT);
        renderer.setStrokeColor(color);
        renderer.setFillColor(color);
        renderer.setRenderLayer(RenderLayer.UI);
        renderer.setFilled(true);
        return renderer;
    }

    /**
     * Estimate text width for UI centering purposes.
     * Uses approximate character width based on font size.
     *
     * @param text The text to measure
     * @param fontSize Font size in pixels
     * @return Estimated width in pixels
     */
    private float estimateTextWidth(String text, int fontSize) {
        if (text == null || text.isEmpty()) {
            return 0.0f;
        }

        // Approximate character width as 60% of font size (typical for proportional fonts)
        float avgCharWidth = fontSize * 0.6f;
        return text.length() * avgCharWidth;
    }

    private Vector2D calculateAnchoredPosition(UIAnchor anchor, Vector2D offset, GameData gameData) {
        float screenWidth = gameData.getDisplayWidth();
        float screenHeight = gameData.getDisplayHeight();

        return switch (anchor) {
            case TOP_LEFT -> new Vector2D(offset.x(), offset.y());
            case TOP_CENTER -> new Vector2D((screenWidth / 2) + offset.x(), offset.y());
            case TOP_RIGHT -> new Vector2D(screenWidth + offset.x(), offset.y());
            case BOTTOM_LEFT -> new Vector2D(offset.x(), screenHeight + offset.y());
            case BOTTOM_CENTER -> new Vector2D((screenWidth / 2) + offset.x(), screenHeight + offset.y());
            case BOTTOM_RIGHT -> new Vector2D(screenWidth + offset.x(), screenHeight + offset.y());
        };
    }
}