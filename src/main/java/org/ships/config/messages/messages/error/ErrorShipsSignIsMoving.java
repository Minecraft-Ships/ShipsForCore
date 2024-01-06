package org.ships.config.messages.messages.error;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.Message;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.List;

public class ErrorShipsSignIsMoving implements Message<Object> {
    @Override
    public String[] getPath() {
        return new String[]{"Error", "Sign", "Ship Sign Is Moving"};
    }

    @Override
    public Component getDefaultMessage() {
        return Component.text("Ships sign is already moving ship").color(NamedTextColor.RED);
    }

    @Override
    public Collection<AdapterCategory<?>> getCategories() {
        return List.of();
    }

    @Override
    public Component processMessage(@NotNull Component text, Object obj) {
        return text;
    }
}
