package org.ships.config.blocks.parsers.identify;

import org.ships.vessel.common.types.ShipType;

public class StringToShipTypeParser extends StringToIdentifiable<ShipType> {

    public StringToShipTypeParser() {
        super(ShipType.class);
    }
}
