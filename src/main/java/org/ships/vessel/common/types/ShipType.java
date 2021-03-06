package org.ships.vessel.common.types;

import org.core.config.ConfigurationStream;
import org.core.platform.Plugin;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
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

public interface ShipType<T extends Vessel> extends Identifiable {

    @Deprecated
    OPShipType OVERPOWERED_SHIP = new OPShipType();
    MarsshipType MARSSHIP = new MarsshipType();
    AirshipType AIRSHIP = new AirshipType();
    WaterShipType WATERSHIP = new WaterShipType();
    SubmarineType SUBMARINE = new SubmarineType();
    PlaneType PLANE = new PlaneType();

    @NotNull String getDisplayName();

    @NotNull Plugin getPlugin();

    @NotNull ExpandedBlockList getDefaultBlockList();

    int getDefaultMaxSpeed();

    int getDefaultAltitudeSpeed();

    @NotNull Optional<Integer> getDefaultMaxSize();

    int getDefaultMinSize();

    @NotNull ConfigurationStream.ConfigurationFile getFile();

    @NotNull T createNewVessel(@NotNull SignTileEntity ste, @NotNull SyncBlockPosition bPos);

    @NotNull BlockType[] getIgnoredTypes();

    @NotNull Set<VesselFlag<?>> getFlags();

    default T createNewVessel(@NotNull LiveSignTileEntity position) {
        return createNewVessel(position, position.getPosition());
    }

    default <E> @NotNull Optional<E> getFlag(@NotNull Class<E> class1) {
        return getFlags().stream().filter(class1::isInstance).map(f -> (E) f).findAny();
    }

    default <E> @NotNull Optional<E> getFlagValue(@NotNull Class<? extends VesselFlag<E>> class1) {
        Optional<? extends VesselFlag<E>> opFlag = getFlag(class1);
        if (!opFlag.isPresent()) {
            return Optional.empty();
        }
        return opFlag.get().getValue();
    }

    @Override
    default @NotNull String getId() {
        return getPlugin().getPluginId() + ":" + getName().toLowerCase();
    }

}
