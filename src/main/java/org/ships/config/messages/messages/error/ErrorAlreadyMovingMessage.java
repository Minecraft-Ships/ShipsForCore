package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorAlreadyMovingMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Already Moving"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                MessageAdapters.VESSEL_ID.adapterTextFormat() + " is already moving. Please wait for it to finish");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        var vesselAdapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(Collectors.toList());

        for (MessageAdapter<Vessel> adapter : vesselAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
