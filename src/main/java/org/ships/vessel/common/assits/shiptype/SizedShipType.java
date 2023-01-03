package org.ships.vessel.common.assits.shiptype;

import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.util.Optional;

public interface SizedShipType<V extends Vessel> extends ShipType<V> {

    MinSizeRequirement getMinimumSizeRequirement();

    MaxSizeRequirement getMaxSizeRequirement();

    default Optional<Integer> getMaxSize() {
        return this.getFile().getInteger(AbstractShipType.MAX_SIZE);
    }

    default int getMinSize() {
        return this.getFile().getInteger(AbstractShipType.MIN_SIZE).orElse(0);
    }
}
