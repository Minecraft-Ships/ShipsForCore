package org.ships.movement.result;

import org.ships.vessel.common.types.Vessel;

public class AbstractFailedMovement implements FailedMovement {

    protected MovementResult<? extends Object> result;
    protected Vessel ship;

    public AbstractFailedMovement(Vessel vessel, MovementResult<? extends Object> mr){
        this.result = mr;
        this.ship = vessel;
    }

    @Override
    public MovementResult<? extends Object> getResult() {
        return this.result;
    }

    @Override
    public Vessel getShip() {
        return this.ship;
    }
}
