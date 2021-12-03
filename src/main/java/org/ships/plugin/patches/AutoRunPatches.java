package org.ships.plugin.patches;

import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;

public interface AutoRunPatches {

    @Deprecated(forRemoval = true)
    Runnable NO_GRAVITY_FIX = (() -> ShipsPlugin
            .getPlugin()
            .getVessels()
            .stream()
            .filter(v -> v
                    .getValue(MovingFlag.class)
                    .map(movementContext -> movementContext.getMovingStructure().isEmpty())
                    .orElse(false))
            .forEach(v -> v
                    .getEntities()
                    .forEach(e -> e.setGravity(true))));
}
