package org.ships.config.messages.adapter.specific.number;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;

public class NumberAdapter<N extends Number> implements MessageAdapter<N> {

    private final String text;
    private final Function<N, String> to;

    public NumberAdapter(@NotNull String text) {
        this(text, v -> {
            DecimalFormat format = new DecimalFormat("##.00");
            return format.format(v);
        });
    }

    public NumberAdapter(@NotNull String text, @NotNull Function<N, String> function) {
        this.text = text;
        this.to = function;
    }

    @Override
    public String adapterText() {
        return this.text;
    }

    @Override
    public Class<?> adaptingType() {
        return Number.class;
    }

    @Override
    public Set<String> examples() {
        return Set.of(this.adapterTextFormat() + " is the magic number");
    }

    @Override
    public Collection<AdapterCategory<N>> categories() {
        return Collections.emptyList();
    }

    @Override
    public Component processMessage(@NotNull N obj) {
        return Component.text(this.to.apply(obj));
    }
}
