package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.ships.vessel.common.types.Vessel;

public interface FailedMovement{

    MovementResult<? extends Object> getResult();
    Vessel getShip();

    default void sendMessage(CommandViewer viewer){
        getResult().sendMessage(getShip(), viewer);
    }

}
