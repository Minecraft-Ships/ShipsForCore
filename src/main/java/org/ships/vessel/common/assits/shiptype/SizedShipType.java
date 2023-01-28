package org.ships.vessel.common.assits.shiptype;

import org.ships.vessel.common.requirement.MaxSizeRequirement;
import org.ships.vessel.common.requirement.MinSizeRequirement;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;

import java.util.Optional;
import java.util.OptionalInt;

public interface SizedShipType<V extends Vessel> extends ShipType<V> {

    MinSizeRequirement getMinimumSizeRequirement();

    @Deprecated(forRemoval = true)
    default MaxSizeRequirement getMaxSizeRequirement() {
        return this.getMaximumSizeRequirement();
    }

    MaxSizeRequirement getMaximumSizeRequirement();

    @Deprecated(forRemoval = true)
    default Optional<Integer> getMaxSize() {
        return this.getMaximumSize().stream().boxed().findAny();
    }

    default OptionalInt getMaximumSize() {
        return this.getMaxSizeRequirement().getMaxSize();
    }

    @Deprecated(forRemoval = true)
    default int getMinSize() {
        return this.getMinimumSize();
    }

    default int getMinimumSize() {
        return this.getMinimumSizeRequirement().getMinimumSize();

    }
}
