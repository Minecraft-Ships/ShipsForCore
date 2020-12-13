package org.ships.vessel.common.assits.shiptype;

import org.core.inventory.item.ItemType;
import org.ships.vessel.common.assits.FuelSlot;
import org.ships.vessel.common.types.ShipType;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.common.types.typical.AbstractShipType;

import java.util.HashSet;
import java.util.Set;

public interface FuelledShipType<V extends Vessel> extends ShipType<V> {

    default int getDefaultFuelConsumption(){
        return this.getFile().getInteger(AbstractShipType.FUEL_CONSUMPTION).orElse(0);
    }

    default FuelSlot getDefaultFuelSlot(){
        return this.getFile().parse(AbstractShipType.FUEL_SLOT).orElse(FuelSlot.BOTTOM);
    }

    default Set<ItemType> getDefaultFuelTypes(){
        return this.getFile().parseCollection(AbstractShipType.FUEL_TYPES, new HashSet<>());
    }

}
