package dk.sdu.mmmi.cbse.common.components;

import dk.sdu.mmmi.cbse.common.RenderLayer;
import javafx.scene.paint.Color;

/**
 * Component that stores entity visualization properties.
 * Controls how an entity is rendered on screen.
 */
public class RendererComponent implements IComponent {
    private boolean visible = true;
    private Color strokeColor = Color.WHITE;
    private Color fillColor = Color.TRANSPARENT;
    private float strokeWidth = 1.0f;
    private boolean filled = false;
    private ShapeType shapeType = ShapeType.POLYGON;
    private RenderLayer renderLayer = RenderLayer.OBSTACLE;

    /**
     * Shape types for different rendering styles
     */
    public enum ShapeType {
        POLYGON,    // Uses polygon coordinates from TransformComponent
        CIRCLE,     // Simple circle with radius
        IMAGE       // Image sprite (future extension)
    }

    /**
     * Create a new renderer component with default values
     */
    public RendererComponent() {
        // Use default values
    }

    /**
     * Check if entity should be rendered
     * @return true if visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * Set entity visibility
     * @param visible true to show, false to hide
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    /**
     * Get rendering order layer
     * @return Layer value (higher values render on top)
     */
    public RenderLayer getRenderLayer() {
        return renderLayer;
    }

    /**
     * Set rendering order layer using a predefined layer
     * @param layer The render layer enum value
     */
    public void setRenderLayer(RenderLayer layer) {
        this.renderLayer = layer;
    }

    /**
     * Get outline color
     * @return Stroke color
     */
    public Color getStrokeColor() {
        return strokeColor;
    }

    /**
     * Set outline color
     * @param strokeColor Stroke color
     */
    public void setStrokeColor(Color strokeColor) {
        this.strokeColor = strokeColor;
    }

    /**
     * Get fill color
     * @return Fill color
     */
    public Color getFillColor() {
        return fillColor;
    }

    /**
     * Set fill color
     * @param fillColor Fill color
     */
    public void setFillColor(Color fillColor) {
        this.fillColor = fillColor;
        this.filled = (fillColor != null && !fillColor.equals(Color.TRANSPARENT));
    }

    /**
     * Get outline width
     * @return Stroke width
     */
    public float getStrokeWidth() {
        return strokeWidth;
    }

    /**
     * Set outline width
     * @param strokeWidth Stroke width
     */
    public void setStrokeWidth(float strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    /**
     * Check if shape should be filled
     * @return true if filled
     */
    public boolean isFilled() {
        return filled;
    }

    /**
     * Set fill state
     * @param filled true to fill shape
     */
    public void setFilled(boolean filled) {
        this.filled = filled;
    }

    /**
     * Get shape type
     * @return Shape type
     */
    public ShapeType getShapeType() {
        return shapeType;
    }

    /**
     * Set shape type
     * @param shapeType Shape type
     */
    public void setShapeType(ShapeType shapeType) {
        this.shapeType = shapeType;
    }
}