package org.ships.config.messages.adapter.config;

import org.core.adventureText.AText;
import org.ships.plugin.ShipsPlugin;

import java.util.Collections;
import java.util.Set;

public class TrackLimitAdapter implements ConfigAdapter {
    @Override
    public String adapterText() {
        return "Config Track Limit";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(ShipsPlugin.getPlugin().getConfig().getDefaultTrackSize() + "");
    }

    public AText process(AText message) {
        return message.withAllAs(this.adapterTextFormat(),
                AText.ofPlain(ShipsPlugin.getPlugin().getConfig().getDefaultTrackSize() + ""));
    }
}
