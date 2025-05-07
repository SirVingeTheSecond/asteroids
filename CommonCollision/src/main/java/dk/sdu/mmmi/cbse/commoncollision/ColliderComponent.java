package dk.sdu.mmmi.cbse.commoncollision;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for entity collision properties.
 * Defines collision layer, groups, and active state.
 */
public class ColliderComponent implements IComponent {
    private CollisionLayer layer;
    private int groups;
    private boolean active = true;

    /**
     * Get the collision layer
     * @return The collision layer
     */
    public CollisionLayer getLayer() {
        return layer;
    }

    /**
     * Set the collision layer
     * @param layer The collision layer
     */
    public void setLayer(CollisionLayer layer) {
        this.layer = layer;
    }

    /**
     * Add a collision group
     * @param group The collision group to add
     */
    public void addGroup(CollisionGroup group) {
        groups |= group.getMask();
    }

    /**
     * Remove a collision group
     * @param group The collision group to remove
     */
    public void removeGroup(CollisionGroup group) {
        groups &= ~group.getMask();
    }

    /**
     * Check if entity is in a specific collision group
     * @param group The group to check
     * @return true if entity is in the group
     */
    public boolean isInGroup(CollisionGroup group) {
        return (groups & group.getMask()) != 0;
    }

    /**
     * Get all collision groups as a bitmask
     * @return Bitmask of all groups
     */
    public int getGroups() {
        return groups;
    }

    /**
     * Check if collision is active
     * @return true if collision is active
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Set whether collision is active
     * @param active Whether collision is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}