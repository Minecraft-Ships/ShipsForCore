package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.ships.vessel.common.types.Vessel;

public interface FailedMovement<E extends Object>{

    MovementResult<E> getResult();
    Vessel getShip();

    default void sendMessage(CommandViewer viewer, E value){
        getResult().sendMessage(getShip(), viewer, value);
    }

}
