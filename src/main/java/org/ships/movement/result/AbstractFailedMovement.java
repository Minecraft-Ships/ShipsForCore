package org.ships.movement.result;

import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public class AbstractFailedMovement<E extends Object> implements FailedMovement<E> {

    protected final MovementResult<E> result;
    protected final Vessel ship;
    protected final E value;

    public AbstractFailedMovement(Vessel vessel, MovementResult<E> mr, E value) {
        this.result = mr;
        this.ship = vessel;
        this.value = value;
    }

    @Override
    public MovementResult<E> getResult() {
        return this.result;
    }

    @Override
    public Vessel getShip() {
        return this.ship;
    }

    @Override
    public Optional<E> getValue() {
        return Optional.ofNullable(this.value);
    }
}
