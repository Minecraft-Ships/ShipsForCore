package org.ships.movement.instruction.actions;

import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.types.Vessel;

public interface PostMovement {

    void postMove(@NotNull Vessel vessel);

}
