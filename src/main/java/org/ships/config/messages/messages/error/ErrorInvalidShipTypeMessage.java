package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class ErrorInvalidShipTypeMessage implements Message<String> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Creation", "Invalid Ship Type"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Invalid Shiptype of '" + Message.INVALID_NAME.adapterTextFormat() + "'");
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return Collections.emptyList();
    }

    @Override
    public Collection<MessageAdapter<?>> getAdapters() {
        return List.of(Message.INVALID_NAME);
    }

    @Override
    public Component processMessage(@NotNull Component text, String obj) {
        return Message.INVALID_NAME.processMessage(obj, text);
    }
}
