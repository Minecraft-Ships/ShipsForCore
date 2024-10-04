package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MapToAdapter<O, T> implements MessageAdapter<O> {

    private final MessageAdapter<T> adapter;
    private final Function<O, T> to;
    private final Class<?> adapterType;

    public MapToAdapter(Class<?> type, MessageAdapter<T> adapter, Function<O, T> to) {
        this.to = to;
        this.adapter = adapter;
        this.adapterType = type;
    }

    @Override
    public String adapterText() {
        return this.adapter.adapterText();
    }

    @Override
    public Class<?> adaptingType() {
        return this.adapterType;
    }

    @Override
    public Collection<String> examples() {
        return this.adapter.examples();
    }

    @Override
    public Collection<AdapterCategory<O>> categories() {
        return this.adapter
                .categories()
                .parallelStream()
                .map(t -> t.<O>map(this.adapterType))
                .collect(Collectors.toList());
    }

    @Override
    public Component processMessage(@NotNull O obj) {
        return this.adapter.processMessage(this.to.apply(obj));
    }
}
