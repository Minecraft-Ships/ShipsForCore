package org.ships.vessel.common.loader;

import org.ships.vessel.common.types.Vessel;

import java.io.IOException;

public interface ShipsLoader {

    public Vessel load() throws IOException;
}
