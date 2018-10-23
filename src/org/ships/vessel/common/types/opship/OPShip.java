package org.ships.vessel.common.types.opship;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class OPShip extends AbstractShipsVessel implements AirType {

    public OPShip(LiveSignTileEntity licence) {
        super(licence);
    }

    public OPShip(SignTileEntity ste, BlockPosition position){
        super(ste, position);
    }

    @Override
    public Optional<FailedMovement> meetsRequirement(MovingBlockSet movingBlocks) {
        return Optional.empty();
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        return new HashMap<>();
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        return this;
    }

    @Override
    public ShipType getType() {
        return ShipType.OVERPOWERED_SHIP;
    }
}
