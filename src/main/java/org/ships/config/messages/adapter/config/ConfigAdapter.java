package org.ships.config.messages.adapter.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.core.adventureText.AText;
import org.core.adventureText.adventure.AdventureText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.plugin.ShipsPlugin;

import java.util.regex.Pattern;

public interface ConfigAdapter<T> extends MessageAdapter<T> {

    @Deprecated(forRemoval = true)
    default AText process(@NotNull ShipsConfig config) {
        return new AdventureText(processMessage(config));
    }

    Component processMessage(@NotNull ShipsConfig config);

    @Deprecated
    default AText process() {
        return this.process(ShipsPlugin.getPlugin().getConfig());
    }

    default Component processMessage() {
        return this.processMessage(ShipsPlugin.getPlugin().getConfig());
    }

    @Deprecated(forRemoval = true)
    default AText process(@NotNull ShipsConfig config, @NotNull AText message) {
        AText mapped = this.process(config);
        return message.withAllAs(this.adapterTextFormat(), mapped);
    }

    default Component processMessage(@NotNull ShipsConfig config, @NotNull Component message) {
        Component mapped = this.processMessage(config);
        return message.replaceText(TextReplacementConfig
                                           .builder()
                                           .replacement(mapped)
                                           .match(Pattern.compile(this.adapterTextFormat(), Pattern.CASE_INSENSITIVE))
                                           .build());
    }

    @Deprecated(forRemoval = true)
    default AText process(@NotNull AText message) {
        AText mapped = this.process();
        return message.withAllAs(this.adapterTextFormat(), mapped);
    }

    default Component processMessage(@NotNull Component message) {
        Component mapped = this.processMessage();
        return message.replaceText(TextReplacementConfig
                                           .builder()
                                           .replacement(mapped)
                                           .match(Pattern.compile(this.adapterTextFormat(), Pattern.CASE_INSENSITIVE))
                                           .build());
    }
}
