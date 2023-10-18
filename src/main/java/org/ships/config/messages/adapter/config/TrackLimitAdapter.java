package org.ships.config.messages.adapter.config;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;

import java.util.Collections;
import java.util.Set;

public class TrackLimitAdapter implements ConfigAdapter<Integer> {
    @Override
    public String adapterText() {
        return "Config Track Limit";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(ShipsPlugin.getPlugin().getConfig().getDefaultTrackSize() + "");
    }

    @Override
    public Component processMessage(@NotNull Integer obj) {
        return Component.text(obj);
    }

    @Override
    public Component processMessage(@NotNull ShipsConfig config) {
        return Component.text(config.getDefaultTrackSize());
    }
}
