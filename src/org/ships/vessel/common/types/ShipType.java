package org.ships.vessel.common.types;

import org.core.configuration.ConfigurationFile;
import org.core.utils.Identifable;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.vessel.common.types.opship.OPShipType;

public interface ShipType extends Identifable {

    OPShipType OVERPOWERED_SHIP = new OPShipType();

    String getDisplayName();
    ExpandedBlockList getDefaultBlockList();
    int getDefaultMaxSpeed();
    int getDefaultAltitudeSpeed();
    boolean canAutopilot();
    ConfigurationFile getFile();


}
