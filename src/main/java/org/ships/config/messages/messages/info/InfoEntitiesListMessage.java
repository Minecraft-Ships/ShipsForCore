package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.HashSet;
import java.util.Set;

public class InfoEntitiesListMessage implements Message<Entity<?>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Entities", "List"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(Message.ENTITY_NAME.adapterTextFormat()).color(NamedTextColor.GOLD);
    }

    @Override
    public Set<MessageAdapter<?>> getAdapters() {
        return new HashSet<>(this.getExactAdapters());
    }

    @Override
    public Component processMessage(@NotNull Component text, Entity<?> obj) {
        for (MessageAdapter<Entity<?>> adapter : this.getExactAdapters()) {
            text = adapter.processMessage(obj, text);
        }
        return text;

    }

    private Set<MessageAdapter<Entity<?>>> getExactAdapters() {
        return new HashSet<>(Message.ENTITY_ADAPTERS);
    }
}
