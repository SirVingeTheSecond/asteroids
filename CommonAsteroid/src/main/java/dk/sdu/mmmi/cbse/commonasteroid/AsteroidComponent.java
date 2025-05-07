package dk.sdu.mmmi.cbse.commonasteroid;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component that contains asteroid-specific properties.
 */
public class AsteroidComponent implements IComponent {
    private int splitCount;
    private int maxSplits = 2;
    private float splitSizeRatio = 0.5f;

    public AsteroidComponent() {
        this.splitCount = 0;
    }

    /**
     * Get the number of times this asteroid has already split
     */
    public int getSplitCount() {
        return splitCount;
    }

    /**
     * Set the number of times this asteroid has already split
     */
    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    /**
     * Get the maximum number of times an asteroid can split
     */
    public int getMaxSplits() {
        return maxSplits;
    }

    /**
     * Set the maximum number of times an asteroid can split
     */
    public void setMaxSplits(int maxSplits) {
        this.maxSplits = maxSplits;
    }

    /**
     * Get the size ratio for split asteroids (child size relative to parent)
     */
    public float getSplitSizeRatio() {
        return splitSizeRatio;
    }

    /**
     * Set the size ratio for split asteroids (child size relative to parent)
     */
    public void setSplitSizeRatio(float splitSizeRatio) {
        this.splitSizeRatio = splitSizeRatio;
    }

    /**
     * Check if the asteroid can still be split
     */
    public boolean canSplit() {
        return splitCount < maxSplits;
    }

    /**
     * Increment the split count
     */
    public void incrementSplitCount() {
        splitCount++;
    }
}