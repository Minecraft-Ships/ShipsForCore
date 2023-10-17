package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.HashSet;
import java.util.Set;

public class ErrorAlreadyMovingMessage implements Message<Vessel> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "AlreadyMoving"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(
                Message.VESSEL_ID.adapterTextFormat() + " is already moving. Please wait for it to finish");
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        Set<MessageAdapter<?>> set = new HashSet<>(Message.VESSEL_ADAPTERS);
        set.addAll(Message.CONFIG_ADAPTERS);
        return set;
    }

    @Override
    public Component processMessage(@NotNull Component text, Vessel obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        for (MessageAdapter<Vessel> adapter : Message.VESSEL_ADAPTERS) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
