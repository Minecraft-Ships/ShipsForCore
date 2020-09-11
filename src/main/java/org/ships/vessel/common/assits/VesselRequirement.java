package org.ships.vessel.common.assits;

import org.ships.exceptions.MoveException;
import org.ships.movement.MovementContext;
import org.ships.vessel.common.types.Vessel;

public interface VesselRequirement extends Vessel {

    void meetsRequirements(MovementContext context) throws MoveException;
    void processRequirements(MovementContext context) throws MoveException;

}
