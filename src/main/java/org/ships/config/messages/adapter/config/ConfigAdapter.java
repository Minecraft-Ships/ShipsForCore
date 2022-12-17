package org.ships.config.messages.adapter.config;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.plugin.ShipsPlugin;

public interface ConfigAdapter<T> extends MessageAdapter<T> {

    AText process(@NotNull ShipsConfig config);

    default AText process() {
        return this.process(ShipsPlugin.getPlugin().getConfig());
    }

    default AText process(@NotNull ShipsConfig config, @NotNull AText message) {
        AText mapped = this.process(config);
        return message.withAllAs(this.adapterTextFormat(), mapped);
    }

    default AText process(@NotNull AText message) {
        AText mapped = this.process();
        return message.withAllAs(this.adapterTextFormat(), mapped);
    }
}
