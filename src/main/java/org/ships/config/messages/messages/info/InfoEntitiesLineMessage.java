package org.ships.config.messages.messages.info;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;

public class InfoEntitiesLineMessage implements Message<Object> {
    @Override
    public String[] getPath() {
        return new String[]{"Info", "Entities", "Line"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Entities:").color(NamedTextColor.AQUA);
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return Collections.emptyList();
    }

    @Override
    public Component processMessage(@NotNull Component text, Object obj) {
        return text;
    }
}
