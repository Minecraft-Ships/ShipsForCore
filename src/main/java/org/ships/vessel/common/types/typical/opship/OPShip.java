package org.ships.vessel.common.types.typical.opship;

import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.exceptions.NoLicencePresent;
import org.ships.movement.autopilot.FlightPath;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.assits.FlightPathType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.typical.AbstractShipsVessel;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Deprecated
public class OPShip extends AbstractShipsVessel implements AirType, FlightPathType {

    protected @Nullable FlightPath flightPath;

    public OPShip(LiveTileEntity licence, ShipType<? extends OPShip> origin) throws NoLicencePresent {
        super(licence, origin);
    }

    public OPShip(SignTileEntity ste, SyncBlockPosition position, ShipType<? extends OPShip> origin) {
        super(ste, position, origin);
    }

    @Override
    public Map<ConfigurationNode.KnownParser<?, ?>, Object> serialize(ConfigurationStream file) {
        return new HashMap<>();
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationStream file) {
        return this;
    }

    @Override
    public @NotNull Map<String, String> getExtraInformation() {
        return new HashMap<>();
    }

    @Override
    public Optional<FlightPath> getFlightPath() {
        return Optional.ofNullable(this.flightPath);
    }

    @Override
    public FlightPathType setFlightPath(FlightPath path) {
        this.flightPath = path;
        return this;
    }
}
