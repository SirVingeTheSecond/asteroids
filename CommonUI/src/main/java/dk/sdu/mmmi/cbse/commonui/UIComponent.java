package dk.sdu.mmmi.cbse.commonui;

import dk.sdu.mmmi.cbse.common.Vector2D;
import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for UI.
 */
public class UIComponent implements IComponent {
    private UIType uiType = UIType.GENERIC;
    private String displayText = "";
    private boolean visible = true;
    private UIAnchor anchor = UIAnchor.TOP_LEFT;
    private Vector2D offset = Vector2D.zero();
    private int fontSize = 16;
    private boolean autoUpdate = true;

    public UIComponent() {
    }

    /**
     * Create a UI component of specific type
     *
     * @param uiType The type of UI element
     */
    public UIComponent(UIType uiType) {
        this.uiType = uiType;
    }

    public UIType getUIType() {
        return uiType;
    }

    public void setUIType(UIType uiType) {
        this.uiType = uiType;
    }

    public String getDisplayText() {
        return displayText;
    }

    public void setDisplayText(String displayText) {
        this.displayText = displayText;
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