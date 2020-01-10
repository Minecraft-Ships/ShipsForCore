package org.ships.config.parsers.identify;

import org.ships.vessel.common.flag.VesselFlag;

public class StringToVesselFlag extends StringToIdentifiable<VesselFlag.Serializable> {

    public StringToVesselFlag() {
        super(VesselFlag.Serializable.class);
    }
}
