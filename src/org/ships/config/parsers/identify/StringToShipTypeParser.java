package org.ships.config.parsers.identify;

import org.ships.vessel.common.types.ShipType;

public class StringToShipTypeParser extends StringToIdentifiable<ShipType> {

    public StringToShipTypeParser() {
        super(ShipType.class);
    }
}
