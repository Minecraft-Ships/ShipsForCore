package org.ships.vessel.common.assits;

import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.io.IOException;

public interface SwitchableVessel<V extends ShipType<? extends Vessel>> {

    SwitchableVessel<V> setType(V type) throws IOException;
}
