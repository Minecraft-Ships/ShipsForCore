package org.ships.vessel.common.types.typical.opship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.exceptions.NoLicencePresent;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.HashMap;
import java.util.Map;

@Deprecated(forRemoval = true)
public class OPShip extends AbstractShipsVessel implements AirType {

    @Deprecated(forRemoval = true)
    public OPShip(LiveTileEntity licence, ShipType<? extends OPShip> origin) throws NoLicencePresent {
        super(licence, origin);
    }

    @Deprecated(forRemoval = true)
    public OPShip(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends OPShip> origin) {
        super(ste, position, origin);
    }

    public OPShip(@NotNull LiveSignTileEntity licence,
                  boolean isFrontOfSign,
                  @NotNull ShipType<? extends AbstractShipsVessel> type) {
        super(licence, isFrontOfSign, type);
    }

    public OPShip(SignSide signSide, SyncBlockPosition position, ShipType<? extends AbstractShipsVessel> type) {
        super(signSide, position, type);
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
}
