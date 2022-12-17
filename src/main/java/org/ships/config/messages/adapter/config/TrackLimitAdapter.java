package org.ships.config.messages.adapter.config;

import org.core.adventureText.AText;
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
    public AText process(@NotNull Integer obj) {
        return AText.ofPlain(obj + "");
    }

    @Override
    public AText process(@NotNull ShipsConfig config) {
        return AText.ofPlain(config.getDefaultTrackSize() + "");
    }
}
