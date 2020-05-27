package org.ships.vessel.common.types;

import org.core.configuration.ConfigurationFile;
import org.core.platform.Plugin;
import org.core.utils.Identifable;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.config.blocks.ExpandedBlockList;
import org.ships.vessel.common.flag.VesselFlag;
import org.ships.vessel.common.types.typical.airship.AirshipType;
import org.ships.vessel.common.types.typical.marsship.MarsshipType;
import org.ships.vessel.common.types.typical.opship.OPShipType;
import org.ships.vessel.common.types.typical.plane.PlaneType;
import org.ships.vessel.common.types.typical.submarine.SubmarineType;
import org.ships.vessel.common.types.typical.watership.WaterShipType;

import java.util.Optional;
import java.util.Set;

public interface ShipType<T extends Vessel> extends Identifable {

    OPShipType OVERPOWERED_SHIP = new OPShipType.Default();
    MarsshipType MARSSHIP = new MarsshipType();
    AirshipType AIRSHIP = new AirshipType();
    WaterShipType WATERSHIP = new WaterShipType();
    SubmarineType SUBMARINE = new SubmarineType();
    PlaneType PLANE = new PlaneType();

    String getDisplayName();
    Plugin getPlugin();
    ExpandedBlockList getDefaultBlockList();
    int getDefaultMaxSpeed();
    int getDefaultAltitudeSpeed();
    ConfigurationFile getFile();
    T createNewVessel(SignTileEntity ste, SyncBlockPosition bPos);
    BlockType[] getIgnoredTypes();

    Set<VesselFlag<?>> getFlags();

    default T createNewVessel(LiveSignTileEntity position){
        return createNewVessel(position, position.getPosition());
    }

    default <T> Optional<T> getFlag(Class<T> class1){
        return (Optional<T>) getFlags().stream().filter(f -> class1.isInstance(f)).findAny();
    }

    default <T> Optional<T> getFlagValue(Class<? extends VesselFlag<T>> class1){
        Optional<? extends VesselFlag<T>> opFlag = getFlag(class1);
        if(!opFlag.isPresent()){
            return Optional.empty();
        }
        return opFlag.get().getValue();
    }

    @Override
    default String getId(){
        return getPlugin().getPluginId() + ":" + getName().toLowerCase();
    }

}
