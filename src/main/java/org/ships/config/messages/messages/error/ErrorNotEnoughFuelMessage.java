package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.core.inventory.item.ItemType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.adapter.misc.CollectionAdapter;
import org.ships.config.messages.adapter.specific.number.NumberAdapter;
import org.ships.config.messages.messages.error.data.FuelRequirementMessageData;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorNotEnoughFuelMessage implements Message<FuelRequirementMessageData> {

    public static final NumberAdapter<Integer> TAKE_AMOUNT = new NumberAdapter<>("Take Amount");


    @Override
    public String[] getPath() {
        return new String[]{"Error", "Requirement", "Fuel", "Not Enough"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                "Not enough fuel, you need " + Message.FUEL_LEFT_REQUIREMENT.adapterTextFormat() + " more of either "
                        + new CollectionAdapter<>(Message.ITEM_NAME).adapterTextFormat());
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL,
                       AdapterCategories.ITEM_TYPE.<Collection<ItemType>>map(Collection.class));
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> adapters = new HashSet<>(Message.super.getAdapters());
        adapters.add(TAKE_AMOUNT);
        return Collections.unmodifiableCollection(adapters);
    }

    @Override
    public Component processMessage(@NotNull Component text, FuelRequirementMessageData obj) {
        Vessel vessel = obj.getVessel();
        int toTakeAmount = obj.getToTakeAmount();
        Collection<ItemType> fuelTypes = obj.getFuelTypes();

        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(
                Collectors.toList());
        List<CollectionAdapter<ItemType>> itemTypeAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.ITEM_TYPE)
                .map(CollectionAdapter::new)
                .collect(Collectors.toList());


        for (MessageAdapter<Vessel> vesselAdapter : vesselAdapters) {
            text = vesselAdapter.processMessage(vessel, text);
        }
        for (MessageAdapter<Collection<ItemType>> itemAdapter : itemTypeAdapters) {
            text = itemAdapter.processMessage(fuelTypes, text);
        }
        text = TAKE_AMOUNT.processMessage(toTakeAmount, text);
        return text;
    }
}
