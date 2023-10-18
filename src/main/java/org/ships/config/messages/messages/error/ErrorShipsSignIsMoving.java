package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.config.ConfigAdapter;

import java.util.HashSet;
import java.util.Set;

public class ErrorShipsSignIsMoving implements Message<Object> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "ShipSignIsMoving"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Ships sign is already moving ship").color(NamedTextColor.RED);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(Message.CONFIG_ADAPTERS);
    }

    @Override
    public Component processMessage(@NotNull Component text, Object obj) {
        for (ConfigAdapter<?> adapter : Message.CONFIG_ADAPTERS) {
            text = adapter.processMessage(text);
        }
        return text;
    }
}
