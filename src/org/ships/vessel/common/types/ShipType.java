package org.ships.vessel.common.types;

import org.core.configuration.ConfigurationFile;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
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
    Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos);

    default Vessel createNewVessel(LiveSignTileEntity position){
        return createNewVessel(position, position.getPosition());
    }

}
