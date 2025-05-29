package dk.sdu.mmmi.cbse.common.components;

import dk.sdu.mmmi.cbse.common.Vector2D;

/**
 * Component for tracking recoil state and providing smooth recoil.
 * Manages the recoil phases: peak recoil with minimal control, then gradual recovery.
 */
public class RecoilComponent implements IComponent {
    private boolean inRecoil = false;
    private float recoilTimer = 0.0f;
    private float recoilDuration = 0.0f;
    private Vector2D recoilVelocity = Vector2D.zero();

    // Recoil phase configuration
    private static final float PEAK_PHASE_RATIO = 0.3f;
    private static final float RECOVERY_PHASE_RATIO = 0.7f;

    // Physics modifiers during recoil
    private static final float PEAK_DRAG = 0.98f;
    private static final float PEAK_INPUT_STRENGTH = 0.2f;
    private static final float MIN_RECOVERY_INPUT = 0.3f;

    /**
     * Create a new recoil component with default state
     */
    public RecoilComponent() {
        // Default constructor
    }

    /**
     * Start a recoil effect with specified velocity and duration
     *
     * @param recoilVelocity The velocity vector for the recoil kick
     * @param duration Duration of the recoil effect in seconds
     */
    public void startRecoil(Vector2D recoilVelocity, float duration) {
        this.inRecoil = true;
        this.recoilTimer = 0.0f;
        this.recoilDuration = duration;
        this.recoilVelocity = recoilVelocity;
    }

    /**
     * Update the recoil timer and check if recoil should end
     *
     * @param deltaTime Time elapsed since last update
     */
    public void updateRecoil(float deltaTime) {
        if (!inRecoil) {
            return;
        }

        recoilTimer += deltaTime;

        if (recoilTimer >= recoilDuration) {
            endRecoil();
        }
    }

    /**
     * End the recoil effect and reset to normal state
     */
    public void endRecoil() {
        this.inRecoil = false;
        this.recoilTimer = 0.0f;
        this.recoilDuration = 0.0f;
        this.recoilVelocity = Vector2D.zero();
    }

    /**
     * Get the current recoil phase (0.0 to 1.0)
     *
     * @return Recoil phase where 0.0 is start and 1.0 is end
     */
    public float getRecoilPhase() {
        if (!inRecoil || recoilDuration <= 0) {
            return 1.0f; // Fully recovered
        }

        return Math.min(1.0f, recoilTimer / recoilDuration);
    }

    /**
     * Check if currently in peak recoil phase (minimal control)
     *
     * @return true if in peak recoil phase
     */
    public boolean isInPeakRecoil() {
        return inRecoil && getRecoilPhase() < PEAK_PHASE_RATIO;
    }

    /**
     * Check if currently in recovery phase (gradual control return)
     *
     * @return true if in recovery phase
     */
    public boolean isInRecovery() {
        return inRecoil && getRecoilPhase() >= PEAK_PHASE_RATIO;
    }

    /**
     * Get the recovery strength (0.0 to 1.0) during recovery phase
     *
     * @return Recovery strength where 0.0 is start of recovery and 1.0 is full recovery
     */
    public float getRecoveryStrength() {
        if (!isInRecovery()) {
            return 0.0f;
        }

        float recoveryPhase = getRecoilPhase() - PEAK_PHASE_RATIO;
        return recoveryPhase / RECOVERY_PHASE_RATIO;
    }

    /**
     * Get the appropriate drag coefficient for current recoil state
     *
     * @param normalDrag The normal drag coefficient when not in recoil
     * @return Modified drag coefficient for current recoil phase
     */
    public float getRecoilDrag(float normalDrag) {
        if (!inRecoil) {
            return normalDrag;
        }

        if (isInPeakRecoil()) {
            return PEAK_DRAG;
        }

        // Gradually transition from peak drag back to normal drag
        float recoveryStrength = getRecoveryStrength();
        return PEAK_DRAG + (recoveryStrength * (normalDrag - PEAK_DRAG));
    }

    /**
     * Get the appropriate input strength for current recoil state
     *
     * @return Input strength multiplier (0.0 to 1.0)
     */
    public float getInputStrength() {
        if (!inRecoil) {
            return 1.0f; // Full input control
        }

        if (isInPeakRecoil()) {
            return PEAK_INPUT_STRENGTH;
        }

        // Gradually return input strength during recovery
        float recoveryStrength = getRecoveryStrength();
        return MIN_RECOVERY_INPUT + (recoveryStrength * (1.0f - MIN_RECOVERY_INPUT));
    }

    // Getters and setters
    public boolean isInRecoil() {
        return inRecoil;
    }

    public float getRecoilTimer() {
        return recoilTimer;
    }

    public float getRecoilDuration() {
        return recoilDuration;
    }

    public Vector2D getRecoilVelocity() {
        return recoilVelocity;
    }
}