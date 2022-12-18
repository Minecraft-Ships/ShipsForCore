package org.ships.config.messages.adapter.specific.number;

import org.core.adventureText.AText;
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
    public AText process(@NotNull N obj) {
        return AText.ofPlain(this.to.apply(obj) + "");
    }
}
