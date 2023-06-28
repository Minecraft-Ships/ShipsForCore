package org.ships.vessel.common.loader;

import org.ships.exceptions.load.LoadVesselException;
import org.ships.vessel.common.types.Vessel;

@Deprecated(forRemoval = true)
public interface ShipsLoader {

    Vessel load() throws LoadVesselException;
}
