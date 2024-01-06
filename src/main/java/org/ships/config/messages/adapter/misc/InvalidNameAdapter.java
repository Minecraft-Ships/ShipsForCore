package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class InvalidNameAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Invalid Name";
    }

    @Override
    public Class<?> adaptingType() {
        return String.class;
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Invalid");
    }

    @Override
    public Collection<AdapterCategory<String>> categories() {
        return Collections.emptyList();
    }

    @Override
    public Component processMessage(@NotNull String obj) {
        return Component.text(obj);
    }
}
