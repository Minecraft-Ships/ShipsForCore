package org.ships.algorthum.movement;

import org.core.entity.Entity;
import org.ships.algorthum.Algorthum;
import org.ships.exceptions.MoveException;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.vessel.common.types.Vessel;

import java.util.Map;

public interface BasicMovement extends Algorthum {

    Ships5Movement SHIPS_FIVE = new Ships5Movement();
    Ships6Movement SHIPS_SIX = new Ships6Movement();

    Result move(Vessel vessel, MovingBlockSet set, Map<Entity, MovingBlock> entity, Movement.MidMovement midMovement, Movement.PostMovement... postMovements) throws MoveException;
}
