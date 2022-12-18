package org.ships.config.messages.messages.error.data;

import org.core.inventory.item.ItemType;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;

public class FuelRequirementMessageData {

    private final Vessel vessel;
    private final Collection<ItemType> fuelTypes;
    private final int toTakeAmount;

    public FuelRequirementMessageData(Vessel vessel, Collection<ItemType> fuelTypes, int toTakeAmount) {
        this.vessel = vessel;
        this.toTakeAmount = toTakeAmount;
        this.fuelTypes = Collections.unmodifiableCollection(fuelTypes);
    }

    public Vessel getVessel() {
        return this.vessel;
    }

    public Collection<ItemType> getFuelTypes() {
        return this.fuelTypes;
    }

    public int getToTakeAmount() {
        return this.toTakeAmount;
    }
}
