package org.ships.vessel.common.assits;

import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;

public interface VesselRequirement {

    void meetsRequirements(MovementContext context) throws MoveException;
    void processRequirements(MovementContext context) throws MoveException;

}
