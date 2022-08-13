package org.ships.vessel.common.assits.shiptype;

import org.core.world.position.block.BlockType;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.util.HashSet;
import java.util.Set;

public interface SpecialBlockShipType<V extends Vessel> extends ShipType<V> {

    @Deprecated(forRemoval = true)
    default float getDefaultSpecialBlockPercent() {
        return this.getDefaultSpecialBlocksPercent();
    }

    default float getDefaultSpecialBlocksPercent() {
        return this.getFile().getDouble(AbstractShipType.SPECIAL_BLOCK_PERCENT).orElse(0.0).floatValue();
    }

    @Deprecated(forRemoval = true)
    default Set<BlockType> getDefaultSpecialBlockType() {
        return this.getDefaultSpecialBlockTypes();
    }

    default Set<BlockType> getDefaultSpecialBlockTypes() {
        return this.getFile().parseCollection(AbstractShipType.SPECIAL_BLOCK_TYPE, new HashSet<>());
    }

}
