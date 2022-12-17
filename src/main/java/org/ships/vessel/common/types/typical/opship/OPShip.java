package org.ships.vessel.common.types.typical.opship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class OPShip extends AbstractShipsVessel implements AirType {

    public OPShip(LiveTileEntity licence, ShipType<? extends OPShip> origin) throws NoLicencePresent {
        super(licence, origin);
    }

    public OPShip(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends OPShip> origin) {
        super(ste, position, origin);
    }

    @Override
    public @NotNull Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(@NotNull ConfigurationStream file) {
        return new HashMap<>();
    }

    @Override
    public @NotNull AbstractShipsVessel deserializeExtra(@NotNull ConfigurationStream file) {
        return this;
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        return new HashMap<>();
    }

    @Override
    public @NotNull Vessel setMaxSize(@Nullable Integer size) {
        return this;
    }

    @Override
    public boolean isMaxSizeSpecified() {
        return false;
    }

    @Override
    public @NotNull Vessel setMinSize(@Nullable Integer size) {
        return this;
    }

    @Override
    public boolean isMinSizeSpecified() {
        return false;
    }
}
