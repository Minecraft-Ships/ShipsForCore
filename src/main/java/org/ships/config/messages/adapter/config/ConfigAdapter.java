package org.ships.config.messages.adapter.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.plugin.ShipsPlugin;

import java.util.regex.Pattern;

public interface ConfigAdapter<T> extends MessageAdapter<T> {


    Component processMessage(@NotNull ShipsConfig config);

    default Component processMessage() {
        return this.processMessage(ShipsPlugin.getPlugin().getConfig());
    }

    default Component processMessage(@NotNull ShipsConfig config, @NotNull Component message) {
        Component mapped = this.processMessage(config);
        return message.replaceText(TextReplacementConfig
                                           .builder()
                                           .replacement(mapped)
                                           .match(Pattern.compile(this.adapterTextFormat(), Pattern.CASE_INSENSITIVE))
                                           .build());
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
