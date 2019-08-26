package org.ships.vessel.common.types;

import org.core.configuration.ConfigurationFile;
import org.core.platform.Plugin;
import org.core.utils.Identifable;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.vessel.common.types.typical.airship.AirshipType;
import org.ships.vessel.common.types.typical.marsship.MarsshipType;
import org.ships.vessel.common.types.typical.opship.OPShipType;
import org.ships.vessel.common.types.typical.submarine.SubmarineType;
import org.ships.vessel.common.types.typical.watership.WaterShipType;

public interface ShipType<T extends Vessel> extends Identifable {

    OPShipType OVERPOWERED_SHIP = new OPShipType.Default();
    MarsshipType MARSSHIP = new MarsshipType();
    AirshipType AIRSHIP = new AirshipType();
    WaterShipType WATERSHIP = new WaterShipType();
    SubmarineType SUBMARINE = new SubmarineType();

    String getDisplayName();
    Plugin getPlugin();
    ExpandedBlockList getDefaultBlockList();
    int getDefaultMaxSpeed();
    int getDefaultAltitudeSpeed();
    ConfigurationFile getFile();
    T createNewVessel(SignTileEntity ste, BlockPosition bPos);
    BlockType[] getIgnoredTypes();

    default T createNewVessel(LiveSignTileEntity position){
        return createNewVessel(position, position.getPosition());
    }

    @Override
    default String getId(){
        return getPlugin().getPluginId() + ":" + getName().toLowerCase();
    }

}
