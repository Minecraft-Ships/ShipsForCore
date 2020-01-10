package org.ships.vessel.common.assits;

import org.ships.vessel.common.types.ShipType;

import java.io.IOException;

public interface SwitchableVessel <V extends ShipType> {

    SwitchableVessel<V> setType(V type) throws IOException;
}
