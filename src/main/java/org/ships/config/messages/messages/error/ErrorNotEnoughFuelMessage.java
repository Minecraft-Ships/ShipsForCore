package org.ships.config.messages.messages.error;

import org.core.adventureText.AText;
import org.core.inventory.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.messages.error.data.FuelRequirementMessageData;
import org.ships.vessel.common.assits.VesselRequirement;
import org.ships.vessel.common.requirement.FuelRequirement;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class ErrorNotEnoughFuelMessage implements Message<FuelRequirementMessageData> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Fuel", "Not Enough"};
    }

    @Override
    public AText getDefault() {
        return AText.ofPlain(
                "Not enough fuel, you need " + Message.FUEL_LEFT_REQUIREMENT.adapterTextFormat() + " more of either "
                        + new CollectionAdapter<>(Message.ITEM_NAME).adapterTextFormat());
    }

    private Collection<CollectionAdapter<ItemType>> getFuelTypesAdapters() {
        return Message.ITEM_ADAPTERS.parallelStream().map(CollectionAdapter::new).collect(Collectors.toSet());
    }

    private Collection<MessageAdapter<Integer>> getAmountAdapters() {
        return List.of(Message.FUEL_FOUND_REQUIREMENT, Message.FUEL_CONSUMPTION_REQUIREMENT);
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> adapters = new HashSet<>();
        adapters.addAll(Message.VESSEL_ADAPTERS);
        adapters.addAll(this.getAmountAdapters());
        adapters.addAll(this.getFuelTypesAdapters());
        return adapters;
    }

    @Override
    public AText process(@NotNull AText text, FuelRequirementMessageData obj) {
        Vessel vessel = obj.getVessel();
        for (MessageAdapter<Vessel> vesselAdapter : Message.VESSEL_ADAPTERS) {
            if (vesselAdapter.containsAdapter(text)) {
                text = vesselAdapter.process(obj.getVessel(), text);
            }
        }
        for (CollectionAdapter<ItemType> adapters : this.getFuelTypesAdapters()) {
            if (adapters.containsAdapter(text)) {
                text = adapters.process(obj.getFuelTypes(), text);
            }
        }
        if (!(vessel instanceof VesselRequirement requirement)) {
            return text;
        }
        Optional<FuelRequirement> opFuelRequirement = requirement.getRequirement(FuelRequirement.class);
        if (opFuelRequirement.isEmpty()) {
            return text;
        }
        FuelRequirement fuelRequirement = opFuelRequirement.get();
        if (Message.FUEL_CONSUMPTION_REQUIREMENT.containsAdapter(text)) {
            text = Message.FUEL_CONSUMPTION_REQUIREMENT.process(fuelRequirement.getConsumption(), text);
        }
        if (Message.FUEL_FOUND_REQUIREMENT.containsAdapter(text)) {
            text = Message.FUEL_FOUND_REQUIREMENT.process(obj.getToTakeAmount(), text);
        }
        if (Message.FUEL_LEFT_REQUIREMENT.containsAdapter(text)) {
            text = Message.FUEL_LEFT_REQUIREMENT.process(fuelRequirement.getConsumption() - obj.getToTakeAmount(),
                                                         text);
        }

        return text;
    }
}
