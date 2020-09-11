package org.ships.plugin.patches;

import org.ships.movement.MovementContext;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;

import java.util.Optional;

public interface AutoRunPatches {

    Runnable NO_GRAVITY_FIX = (() -> ShipsPlugin.getPlugin().getVessels()
            .stream()
            .filter(v -> {
                Optional<MovementContext> opSet = v.getValue(MovingFlag.class);
                return opSet.map(movementContext -> movementContext.getMovingStructure().isEmpty()).orElse(false);
            })
            .forEach(v -> v.getEntities().forEach(e -> e.setGravity(true))));
}
