package dk.sdu.mmmi.cbse.commonui;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for UI-specific properties.
 */
public class UIComponent implements IComponent {
    private UIType type = UIType.GENERIC;
    private String text = "";
    private boolean visible = true;
    private UIAnchor anchor = UIAnchor.TOP_LEFT;
    private Vector2D offset = Vector2D.zero();
    private int fontSize = 16;
    private boolean autoUpdate = true;

    public UIComponent() {
    }

    public UIComponent(UIType type) {
        this.type = type;
    }

    public UIType getType() {
        return type;
    }

    public void setType(UIType type) {
        this.type = type;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public UIAnchor getAnchor() {
        return anchor;
    }

    public void setAnchor(UIAnchor anchor) {
        this.anchor = anchor;
    }

    public Vector2D getOffset() {
        return offset;
    }

    public void setOffset(Vector2D offset) {
        this.offset = offset;
    }

    public int getFontSize() {
        return fontSize;
    }

    public void setFontSize(int fontSize) {
        this.fontSize = fontSize;
    }

    public boolean isAutoUpdate() {
        return autoUpdate;
    }

    public void setAutoUpdate(boolean autoUpdate) {
        this.autoUpdate = autoUpdate;
    }
}