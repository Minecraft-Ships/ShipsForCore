package org.ships.config.messages.adapter.specific.number;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.function.Function;

public class NumberAdapter<N extends Number> implements MessageAdapter<N> {

    private final String text;
    private final Function<N, Number> to;

    public NumberAdapter(@NotNull String text) {
        this(text, v -> v);
    }

    public NumberAdapter(@NotNull String text, @NotNull Function<N, Number> function) {
        this.text = text;
        this.to = function;
    }

    @Override
    public String adapterText() {
        return this.text;
    }

    @Override
    public Set<String> examples() {
        return Set.of(this.adapterTextFormat() + " is the magic number");
    }

    @Override
    public Component processMessage(@NotNull N obj) {
        return Component.text(this.to.apply(obj) + "");
    }
}
