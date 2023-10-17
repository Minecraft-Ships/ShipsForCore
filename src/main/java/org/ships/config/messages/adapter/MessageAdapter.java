package org.ships.config.messages.adapter;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.core.adventureText.AText;
import org.core.adventureText.adventure.AdventureText;
import org.core.utils.ComponentUtils;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.regex.Pattern;

public interface MessageAdapter<T> {

    String adapterText();

    Set<String> examples();

    @Deprecated(forRemoval = true)
    default AText process(@NotNull T obj) {
        return new AdventureText(processMessage(obj));
    }

    Component processMessage(@NotNull T obj);

    @Deprecated(forRemoval = true)
    default AText process(@NotNull T obj, @NotNull AText message) {
        AText mapped = this.process(obj);
        return message.withAllAsIgnoreCase(this.adapterTextFormat(), mapped);
    }

    default Component processMessage(@NotNull T obj, @NotNull Component message) {
        Component component = this.processMessage(obj);
        return message.replaceText(TextReplacementConfig
                                           .builder()
                                           .match(Pattern.compile(this.adapterTextFormat(), Pattern.CASE_INSENSITIVE))
                                           .replacement(component)
                                           .build());
    }

    default String adapterTextFormat() {
        return "%" + this.adapterText() + "%";
    }

    default boolean containsAdapter(String plain) {
        return plain.toLowerCase().contains(this.adapterTextFormat().toLowerCase());
    }

    @Deprecated(forRemoval = true)
    default boolean containsAdapter(AText text) {
        return text.containsIgnoreCase(this.adapterTextFormat());
    }

    default boolean containsAdapter(@NotNull Component component){
        return ComponentUtils.toPlain(component).contains(this.adapterTextFormat());
    }

}
