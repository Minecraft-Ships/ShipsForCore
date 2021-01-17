package org.ships.vessel.common.types;

import org.core.config.ConfigurationStream;
import org.core.platform.Plugin;
import org.core.utils.Identifable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
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

    OPShipType OVERPOWERED_SHIP = new OPShipType();
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
    ConfigurationStream.ConfigurationFile getFile();
    T createNewVessel(SignTileEntity ste, SyncBlockPosition bPos);
    BlockType[] getIgnoredTypes();

    Set<VesselFlag<?>> getFlags();

    default T createNewVessel(LiveSignTileEntity position){
        return createNewVessel(position, position.getPosition());
    }

    default <E> Optional<E> getFlag(Class<E> class1){
        return (Optional<E>) getFlags().stream().filter(class1::isInstance).findAny();
    }

    default <E> Optional<E> getFlagValue(Class<? extends VesselFlag<E>> class1){
        Optional<? extends VesselFlag<E>> opFlag = getFlag(class1);
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
