package org.ships.vessel.common.types;

import org.core.configuration.ConfigurationFile;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.vessel.common.types.airship.AirshipType;
import org.ships.vessel.common.types.opship.OPShipType;
import org.ships.vessel.common.types.watership.WaterShipType;

public interface ShipType extends Identifable {

    OPShipType OVERPOWERED_SHIP = new OPShipType.Default();
    AirshipType AIRSHIP = new AirshipType();
    WaterShipType WATERSHIP = new WaterShipType();

    String getDisplayName();
    ExpandedBlockList getDefaultBlockList();
    int getDefaultMaxSpeed();
    int getDefaultAltitudeSpeed();
    ConfigurationFile getFile();
    Vessel createNewVessel(SignTileEntity ste, BlockPosition bPos);
    BlockType[] getIgnoredTypes();

    default Vessel createNewVessel(LiveSignTileEntity position){
        return createNewVessel(position, position.getPosition());
    }

}
