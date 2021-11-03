package org.ships.movement.result.data;

import org.core.inventory.item.ItemType;

import java.util.Collection;

public class RequiredFuelMovementData {

    protected final int requiredConsumption;
    protected final Collection<ItemType> acceptedFuels;

    public RequiredFuelMovementData(int consumption, Collection<ItemType> acceptedFuels) {
        this.requiredConsumption = consumption;
        this.acceptedFuels = acceptedFuels;
    }

    public Collection<ItemType> getAcceptedFuels() {
        return this.acceptedFuels;
    }

    public int getRequiredConsumption() {
        return this.requiredConsumption;
    }
}
