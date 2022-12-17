package org.ships.algorthum.movement;

import org.ships.algorthum.Algorithm;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.Result;
import org.ships.vessel.common.types.Vessel;

public interface BasicMovement extends Algorithm {

    Ships5Movement SHIPS_FIVE = new Ships5Movement();
    Ships6Movement SHIPS_SIX = new Ships6Movement();

    Result move(Vessel vessel, MovementContext context) throws MoveException;
}
