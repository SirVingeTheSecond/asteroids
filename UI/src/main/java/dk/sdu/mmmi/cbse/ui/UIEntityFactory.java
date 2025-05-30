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
 * Factory for creating UI entities with proper positioning and styling.
 * Includes score display using microservice integration.
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
        Vector2D position = calculateAnchoredPosition(UIAnchor.TOP_RIGHT,
                new Vector2D(-150, 30), gameData);

        UIComponent uiComponent = new UIComponent(UIType.WEAPON_DISPLAY);
        uiComponent.setAnchor(UIAnchor.TOP_RIGHT);
        uiComponent.setOffset(new Vector2D(-150, 30));
        uiComponent.setFontSize(18);
        uiComponent.setDisplayText("Weapon: STANDARD");

        RendererComponent renderer = createUIRenderer(Color.WHITE);

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
                new Vector2D(-150, 60), gameData);

        UIComponent uiComponent = new UIComponent(UIType.SCORE_DISPLAY);
        uiComponent.setAnchor(UIAnchor.TOP_RIGHT);
        uiComponent.setOffset(new Vector2D(-150, 60));
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

    private Vector2D calculateAnchoredPosition(UIAnchor anchor, Vector2D offset, GameData gameData) {
        float screenWidth = gameData.getDisplayWidth();
        float screenHeight = gameData.getDisplayHeight();

        return switch (anchor) {
            case TOP_LEFT -> new Vector2D(offset.x(), offset.y());
            case TOP_RIGHT -> new Vector2D(screenWidth + offset.x(), offset.y());
            case BOTTOM_LEFT -> new Vector2D(offset.x(), screenHeight + offset.y());
            case BOTTOM_RIGHT -> new Vector2D(screenWidth + offset.x(), screenHeight + offset.y());
        };
    }
}