package org.ships.plugin.patches;

import org.ships.movement.MovingBlockSet;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;

import java.util.Optional;

public interface AutoRunPatches {

    Runnable NO_GRAVITY_FIX = (() -> ShipsPlugin.getPlugin().getVessels()
            .stream()
            .filter(v -> {
                Optional<MovingBlockSet> opSet = v.getValue(MovingFlag.class);
                if(!opSet.isPresent()){
                    return false;
                }
                return opSet.get().isEmpty();
            })
            .forEach(v -> v.getEntities().stream().forEach(e -> e.setGravity(true))));
}
