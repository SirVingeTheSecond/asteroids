package dk.sdu.mmmi.cbse.commonasteroid;

import dk.sdu.mmmi.cbse.common.components.IComponent;

/**
 * Component for asteroid-specific properties.
 * Stores data related to asteroid behavior and splitting.
 */
public class AsteroidComponent implements IComponent {
    private int splitCount = 0;
    private int maxSplits = 2;
    private float splitSizeRatio = 0.5f;
    private int scoreValue = 100;
    private AsteroidSize size = AsteroidSize.LARGE;

    public AsteroidComponent() {

    }

    /**
     * Create asteroid component with specific size
     *
     * @param size Asteroid size
     */
    public AsteroidComponent(AsteroidSize size) {
        this.size = size;

        switch (size) {
            case LARGE:
                scoreValue = 100;
                break;
            case MEDIUM:
                scoreValue = 150;
                splitCount = 1;
                break;
            case SMALL:
                scoreValue = 200;
                splitCount = 2;
                break;
        }
    }

    /**
     * Get current split count
     *
     * @return Number of times asteroid has split
     */
    public int getSplitCount() {
        return splitCount;
    }

    /**
     * Set current split count
     *
     * @param splitCount Number of times asteroid has split
     */
    public void setSplitCount(int splitCount) {
        this.splitCount = splitCount;
    }

    /**
     * Get maximum number of splits
     *
     * @return Maximum split count
     */
    public int getMaxSplits() {
        return maxSplits;
    }

    /**
     * Set maximum number of splits
     *
     * @param maxSplits Maximum split count
     */
    public void setMaxSplits(int maxSplits) {
        this.maxSplits = maxSplits;
    }

    /**
     * Get size ratio for split asteroids
     *
     * @return Ratio of child asteroid size to parent
     */
    public float getSplitSizeRatio() {
        return splitSizeRatio;
    }

    /**
     * Set size ratio for split asteroids
     *
     * @param splitSizeRatio Ratio of child asteroid size to parent
     */
    public void setSplitSizeRatio(float splitSizeRatio) {
        this.splitSizeRatio = splitSizeRatio;
    }

    /**
     * Get score value when destroyed
     *
     * @return Score value
     */
    public int getScoreValue() {
        return scoreValue;
    }

    /**
     * Set score value when destroyed
     *
     * @param scoreValue Score value
     */
    public void setScoreValue(int scoreValue) {
        this.scoreValue = scoreValue;
    }

    /**
     * Get asteroid size
     *
     * @return Asteroid size
     */
    public AsteroidSize getSize() {
        return size;
    }

    /**
     * Set asteroid size
     *
     * @param size Asteroid size
     */
    public void setSize(AsteroidSize size) {
        this.size = size;
    }
}