package org.ships.movement.result;

import org.core.source.viewer.CommandViewer;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;

public interface FailedMovement<E extends Object> {

    MovementResult<E> getResult();

    Vessel getShip();

    Optional<E> getValue();

    default void sendMessage(CommandViewer viewer, E value) {
        this.getResult().sendMessage(this.getShip(), viewer, value);
    }

    default void sendMessage(CommandViewer viewer) {
        this.getResult().sendMessage(this.getShip(), viewer, this.getValue().orElse(null));
    }

}
