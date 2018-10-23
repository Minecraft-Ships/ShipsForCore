package org.ships.algorthum.movement;

import org.ships.algorthum.Algorthum;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface BasicMovement extends Algorthum {

    Ships5Movement SHIPS_FIVE = new Ships5Movement();
    Ships6Movement SHIPS_SIX = new Ships6Movement();

    Optional<FailedMovement> move(Vessel vessel, MovingBlockSet set);
}
