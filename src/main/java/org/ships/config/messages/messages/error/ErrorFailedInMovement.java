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
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ErrorFailedInMovement implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Unknown Error In Movement"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("A unknown error occurred when moving");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return Collections.singletonList(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        List<MessageAdapter<Vessel>> vesselAdapter = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).collect(
                Collectors.toList());
        for (MessageAdapter<Vessel> adapter : vesselAdapter) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
