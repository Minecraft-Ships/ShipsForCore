package org.ships.event.vessel;

import org.ships.event.ShipsEvent;
import org.ships.vessel.common.types.Vessel;

public interface VesselEvent extends ShipsEvent {

    Vessel getVessel();
}
