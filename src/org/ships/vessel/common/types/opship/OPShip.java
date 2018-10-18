package org.ships.vessel.common.types.opship;

import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.assits.AirType;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;

import java.util.Optional;

public class OPShip extends AbstractShipsVessel implements AirType {

    public OPShip(LiveSignTileEntity licence) {
        super(licence);
    }

    @Override
    public Optional<FailedMovement> meetsRequirement(MovingBlockSet movingBlocks) {
        return Optional.empty();
    }

    @Override
    public ShipType getType() {
        return null;
    }
}
