package org.ships.config.messages.adapter.misc;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.function.Function;

public class MappedAdapter<M, T> implements MessageAdapter<M> {

    private final @NotNull Function<? super M, ? extends T> function;
    private final @NotNull MessageAdapter<? super T> adapter;

    public MappedAdapter(@NotNull MessageAdapter<? super T> adapter,
                         @NotNull Function<? super M, ? extends T> function) {
        this.function = function;
        this.adapter = adapter;
    }


    @Override
    public String adapterText() {
        return this.adapter.adapterText();
    }

    @Override
    public Set<String> examples() {
        return this.adapter.examples();
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
