package org.ships.movement.autopilot.path;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.LinkedTransferQueue;

public class FlightPathManager {

    private Collection<FlightPath> paths = new LinkedTransferQueue<>();

    public Collection<FlightPath> getPaths() {
        return Collections.unmodifiableCollection(this.paths);
    }

    public void registerPath(@NotNull FlightPath path) {
        this.paths.add(path);
    }

    public void unregisterPath(@NotNull FlightPath path) {
        this.paths.remove(path);
    }

}
