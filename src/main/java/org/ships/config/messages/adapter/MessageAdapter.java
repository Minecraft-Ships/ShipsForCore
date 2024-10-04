package org.ships.config.messages.adapter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.core.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.UnmodifiableView;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Set;
import java.util.regex.Pattern;

public interface MessageAdapter<T> {

    @NotNull String adapterText();

    @NotNull Class<?> adaptingType();

    @UnmodifiableView
    @NotNull Collection<String> examples();

    @NotNull Collection<AdapterCategory<T>> categories();

    @NotNull Component processMessage(@NotNull T obj);

    default @NotNull Component processMessage(@NotNull T obj, @NotNull Component message) {
        Component component = this.processMessage(obj);
        return message.replaceText(TextReplacementConfig
                                           .builder()
                                           .match(Pattern.compile(this.adapterTextFormat(), Pattern.CASE_INSENSITIVE))
                                           .replacement(component)
                                           .build());
    }

    default @NotNull String adapterTextFormat() {
        return "%" + this.adapterText() + "%";
    }

    default boolean containsAdapter(@NotNull String plain) {
        return plain.toLowerCase().contains(this.adapterTextFormat().toLowerCase());
    }

    default boolean containsAdapter(@NotNull Component component) {
        return ComponentUtils.toPlain(component).contains(this.adapterTextFormat());
    }

}
