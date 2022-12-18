package org.ships.config.messages.adapter.misc;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.function.Function;

public class MappedAdapter<M, T> implements MessageAdapter<M> {

    private final Function<? super M, ? extends T> function;
    private final MessageAdapter<? super T> adapter;

    public MappedAdapter(MessageAdapter<? super T> adapter, Function<? super M, ? extends T> function) {
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
    public AText process(@NotNull M obj) {
        return this.adapter.process(this.function.apply(obj));
    }

    @Override
    public AText process(@NotNull M obj, @NotNull AText message) {
        return this.adapter.process(this.function.apply(obj), message);
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
    public boolean containsAdapter(AText text) {
        return this.adapter.containsAdapter(text);
    }
}
