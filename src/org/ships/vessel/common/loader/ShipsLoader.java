package org.ships.vessel.common.loader;

import org.ships.vessel.common.types.Vessel;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public interface ShipsLoader {

    Set<Vessel> VESSELS = new HashSet<>();

    public Vessel load() throws IOException;
}
