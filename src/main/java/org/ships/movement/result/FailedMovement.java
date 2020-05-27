package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface FailedMovement<E extends Object>{

    MovementResult<E> getResult();
    Vessel getShip();
    Optional<E> getValue();

    default void sendMessage(CommandViewer viewer, E value){
        getResult().sendMessage(getShip(), viewer, value);
    }

    default void sendMessage(CommandViewer viewer){
        getResult().sendMessage(getShip(), viewer, getValue().orElse(null));
    }

}
