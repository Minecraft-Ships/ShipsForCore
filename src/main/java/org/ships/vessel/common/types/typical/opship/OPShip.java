package org.ships.vessel.common.types.typical.opship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignSide;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.HashMap;
import java.util.Map;

public class OPShip extends AbstractShipsVessel implements AirType {

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
