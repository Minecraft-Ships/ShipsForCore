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

public class ErrorPreventMovementMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Prevented"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("An admin has disabled movement for your ship");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.VESSEL);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        List<MessageAdapter<Vessel>> adapters = MessageAdapters.getAdaptersFor(AdapterCategories.VESSEL).toList();
        for (MessageAdapter<Vessel> adapter : adapters) {
            if (adapter.containsAdapter(text)) {
                text = adapter.processMessage(obj, text);
            }
        }
        return text;
    }
}
