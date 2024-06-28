package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.core.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.MessageAdapters;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class InfoEntitiesListMessage implements Message<Entity<?>> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Entities", "List"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text(MessageAdapters.ENTITY_NAME.adapterTextFormat()).color(NamedTextColor.GOLD);
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of(AdapterCategories.ENTITY);
    }

    @Override
    public Component processMessage(@NotNull Component text, Entity<?> obj) {
        List<MessageAdapter<Entity<?>>> entityAdapters = MessageAdapters
                .getAdaptersFor(AdapterCategories.ENTITY)
                .collect(Collectors.toList());
        for (MessageAdapter<Entity<?>> adapter : entityAdapters) {
            text = adapter.processMessage(obj, text);
        }
        return text;
    }
}
