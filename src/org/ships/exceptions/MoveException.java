package org.ships.exceptions;

import org.ships.movement.result.FailedMovement;

import java.io.IOException;

public class MoveException extends IOException {

    private FailedMovement<? extends Object> movement;

    public MoveException(FailedMovement<? extends Object> failed){
        super("Failed to move due to " + failed.getResult().getClass().getSimpleName());
        this.movement = failed;
    }

    public FailedMovement<? extends Object> getMovement(){
        return this.movement;
    }
}
