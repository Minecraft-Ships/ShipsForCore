package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collection;
import java.util.HashSet;

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
    public Collection<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.VESSEL_ADAPTERS);
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            if (adapter.containsAdapter(text)) {
                text = adapter.processMessage(obj, text);
            }
        }
        return text;
    }
}
