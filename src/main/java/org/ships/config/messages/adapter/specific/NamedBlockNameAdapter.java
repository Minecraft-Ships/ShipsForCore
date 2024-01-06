package org.ships.config.messages.adapter.specific;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class NamedBlockNameAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Block Name";
    }

    @Override
    public Class<?> adaptingType() {
        return String.class;
    }

    @Override
    public Set<String> examples() {
        return Set.of("Cannot find %Block Name%");
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
