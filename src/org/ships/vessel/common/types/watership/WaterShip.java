package org.ships.vessel.common.types.watership;

import org.core.configuration.ConfigurationFile;
import org.core.configuration.ConfigurationNode;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.entity.sign.LiveSignTileEntity;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.movement.MovingBlockSet;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipType;

import java.util.HashMap;
import java.util.Map;

public class WaterShip extends AbstractShipsVessel implements WaterType {

    public WaterShip(LiveSignTileEntity licence) {
        super(licence);
    }

    public WaterShip(SignTileEntity ste, BlockPosition position){
        super(ste, position);
    }

    @Override
    public void meetsRequirement(MovingBlockSet movingBlocks) {
    }

    @Override
    public Map<ConfigurationNode, Object> serialize(ConfigurationFile file) {
        Map<ConfigurationNode, Object> map = new HashMap<>();
        return map;
    }

    @Override
    public AbstractShipsVessel deserializeExtra(ConfigurationFile file) {
        return this;
    }

    @Override
    public ShipType getType() {
        return ShipType.WATERSHIP;
    }
}
