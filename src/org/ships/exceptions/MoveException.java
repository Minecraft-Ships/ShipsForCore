package org.ships.exceptions;

import org.ships.movement.result.FailedMovement;

import java.io.IOException;

public class MoveException extends IOException {

    private FailedMovement<?> movement;

    public MoveException(FailedMovement<?> failed){
        super("Failed to move due to " + failed.getResult().getClass().getSimpleName());
        this.movement = failed;
    }

    public FailedMovement<?> getMovement(){
        return this.movement;
    }
}
