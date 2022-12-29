package org.ships.movement;

public class PreventMovementManager {

    private boolean preventMovement;

    public boolean isMovementPrevented() {
        return this.preventMovement;
    }

    public void setMovementPrevented(boolean check) {
        this.preventMovement = check;
    }

}
