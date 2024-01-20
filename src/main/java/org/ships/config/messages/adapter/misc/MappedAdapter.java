package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class MappedAdapter<M, T> implements MessageAdapter<M> {

    private final @NotNull Function<? super M, ? extends T> function;
    private final @NotNull MessageAdapter<? super T> adapter;
    private final @NotNull Class<?> type;

    public MappedAdapter(@NotNull Class<?> type,
                         @NotNull MessageAdapter<? super T> adapter,
                         @NotNull Function<? super M, ? extends T> function) {
        this.function = function;
        this.adapter = adapter;
        this.type = type;
    }


    @Override
    public String adapterText() {
        return this.adapter.adapterText();
    }

    @Override
    public Class<?> adaptingType() {
        return this.type;
    }

    @Override
    public Collection<String> examples() {
        return this.adapter.examples();
    }

    @Override
    public Collection<AdapterCategory<M>> categories() {
        return this.adapter
                .categories()
                .parallelStream()
                .map(category -> category.map(this.adaptingType()))
                .map(category -> (AdapterCategory<M>) category)
                .collect(Collectors.toList());
    }

    @Override
    public Component processMessage(@NotNull M obj) {
        return this.adapter.processMessage(this.function.apply(obj));
    }

    @Override
    public Component processMessage(@NotNull M obj, @NotNull Component message) {
        T mapped = this.function.apply(obj);
        return this.adapter.processMessage(mapped, message);
    }

    @Override
    public String adapterTextFormat() {
        return this.adapter.adapterTextFormat();
    }

    @Override
    public boolean containsAdapter(String plain) {
        return this.adapter.containsAdapter(plain);
    }

    @Override
    public boolean containsAdapter(@NotNull Component text) {
        return this.adapter.containsAdapter(text);
    }
}
