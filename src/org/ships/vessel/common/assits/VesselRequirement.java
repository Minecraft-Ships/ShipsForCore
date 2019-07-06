package org.ships.vessel.common.assits;

import org.ships.exceptions.MoveException;
import org.ships.movement.MovingBlockSet;

public interface VesselRequirement {

    void meetsRequirements(boolean strict, MovingBlockSet movingBlocks) throws MoveException;
    void processRequirements(boolean strict, MovingBlockSet movingBlocks) throws MoveException;

}
