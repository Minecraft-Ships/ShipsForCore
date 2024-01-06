package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.config.messages.adapter.specific.number.NumberAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.*;

public class ErrorOversizedMessage implements Message<Map.Entry<Vessel, Integer>> {

    public static final NumberAdapter<Integer> OVERSIZED_BY = new NumberAdapter<>("Over Sized By");

    @Override
    public String[] getPath() {
        return new String[]{"Error", "Requirement", "Over Sized"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                Message.VESSEL_ID.adapterTextFormat() + " is over the max size of " + OVERSIZED_BY.adapterTextFormat());
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        Collection<MessageAdapter<?>> set = new HashSet<>(Message.super.getAdapters());
        set.add(OVERSIZED_BY);
        return Collections.unmodifiableCollection(set);
    }

    @Override
    public Component processMessage(@NotNull Component text, Map.Entry<Vessel, Integer> obj) {
        List<MessageAdapter<Vessel>> vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj.getKey(), text);
        }
        text = OVERSIZED_BY.processMessage(obj.getValue(), text);

        return text;

    }
}
