package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.JoinConfiguration;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CollectionAdapter<T> implements MessageAdapter<Collection<T>> {

    private final @NotNull MessageAdapter<T> adapter;

    public CollectionAdapter(@NotNull MessageAdapter<T> adapter) {
        this.adapter = adapter;
    }

    @Override
    public String adapterText() {
        return this.adapter.adapterText() + 's';
    }

    @Override
    public Set<String> examples() {
        return this.adapter
                .examples()
                .parallelStream()
                .map(example -> example.replaceAll(this.adapter.adapterTextFormat(), this.adapterTextFormat()))
                .collect(Collectors.toSet());
    }

    @Override
    public Component processMessage(@NotNull Collection<T> obj) {
        List<Component> list = obj.parallelStream().map(this.adapter::processMessage).toList();
        if (list.isEmpty()) {
            return Component.text("none");
        }
        return Component.join(JoinConfiguration.builder().separator(Component.text(", ")).build(), list);
    }
}
