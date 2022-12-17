package org.ships.config.messages.adapter;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public interface MessageAdapter<T> {

    String adapterText();

    Set<String> examples();

    AText process(@NotNull T obj);

    default AText process(@NotNull T obj, @NotNull AText message) {
        AText mapped = this.process(obj);
        return message.withAllAs(this.adapterTextFormat(), mapped);
    }

    default String adapterTextFormat() {
        return "%" + this.adapterText() + "%";
    }

    default boolean containsAdapter(String plain) {
        return plain.contains(this.adapterTextFormat());
    }

    default boolean containsAdapter(AText text) {
        return text.contains(AText.ofPlain(this.adapterTextFormat()));
    }

}
