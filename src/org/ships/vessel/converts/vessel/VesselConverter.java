package org.ships.vessel.converts.vessel;

import org.core.utils.Identifable;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.converts.ShipsConverter;

public interface VesselConverter <V extends Vessel & Identifable> extends ShipsConverter<V> {

}
