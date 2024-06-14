package org.ships.vessel.common.types;

import org.core.config.ConfigurationStream;
import org.core.permission.CorePermission;
import org.core.platform.plugin.Plugin;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
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

    @Deprecated(forRemoval = true)
    OPShipType OVERPOWERED_SHIP = ShipTypes.OVERPOWERED_SHIP;

    @Deprecated
    MarsshipType MARSSHIP = ShipTypes.MARSSHIP;

    @Deprecated(forRemoval = true)
    AirshipType AIRSHIP = ShipTypes.AIRSHIP;

    @Deprecated(forRemoval = true)
    WaterShipType WATERSHIP = ShipTypes.WATERSHIP;

    @Deprecated(forRemoval = true)
    SubmarineType SUBMARINE = ShipTypes.SUBMARINE;

    @Deprecated(forRemoval = true)
    PlaneType PLANE = ShipTypes.PLANE;

    @NotNull String getDisplayName();

    @NotNull Plugin getPlugin();

    int getDefaultMaxSpeed();

    int getDefaultAltitudeSpeed();

    @NotNull ConfigurationStream.ConfigurationFile getFile();

    @NotNull T createNewVessel(@NotNull SignSide side, @NotNull SyncBlockPosition bPos);

    @NotNull BlockType[] getIgnoredTypes();

    @NotNull Set<VesselFlag<?>> getFlags();

    @NotNull CorePermission getMoveOwnPermission();

    @NotNull CorePermission getMoveOtherPermission();

    @NotNull CorePermission getMakePermission();

    default <E> @NotNull Optional<E> getFlag(@NotNull Class<E> class1) {
        return this.getFlags().stream().filter(class1::isInstance).map(f -> (E) f).findAny();
    }

    default <E> @NotNull Optional<E> getFlagValue(@NotNull Class<? extends VesselFlag<E>> class1) {
        return this.getFlag(class1).flatMap(VesselFlag::getValue);
    }

    @Override
    default @NotNull String getId() {
        return this.getPlugin().getPluginId() + ":" + this.getName().toLowerCase();
    }

}
