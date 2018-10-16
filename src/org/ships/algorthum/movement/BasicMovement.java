package org.ships.algorthum.movement;

import org.ships.algorthum.Algorthum;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface BasicMovement extends Algorthum {

    Optional<FailedMovement> move(Vessel vessel, MovingBlockSet set);
}
