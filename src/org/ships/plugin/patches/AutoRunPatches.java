package org.ships.plugin.patches;

import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.loader.shipsvessel.ShipsFileLoader;

public interface AutoRunPatches {

    Runnable NO_GRAVITY_FIX = (() -> ShipsFileLoader.loadAll(e -> {})
            .stream()
            .filter(v -> !v.getValue(MovingFlag.class).get())
            .forEach(v -> v.getEntities().stream().forEach(e -> e.setGravity(true))));
}
