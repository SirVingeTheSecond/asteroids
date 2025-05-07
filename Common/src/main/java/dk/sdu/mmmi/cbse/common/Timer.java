package dk.sdu.mmmi.cbse.common;

import dk.sdu.mmmi.cbse.common.components.IComponent;

public class Timer implements IComponent {
    private float moveTimer;
    private float shootTimer;

    public float getMoveTimer() {
        return moveTimer;
    }

    public void setMoveTimer(float timer) {
        this.moveTimer = timer;
    }

    public float getShootTimer() {
        return shootTimer;
    }

    public void setShootTimer(float timer) {
        this.shootTimer = timer;
    }
}