package org.ships.vessel.common.assits.shiptype;

import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

public interface SerializableShipType<T extends Vessel> extends ShipType<T> {

    void setMaxSpeed(int speed);

    void setAltitudeSpeed(int speed);

    void register(VesselFlag<?> flag);

    void save();

}
