package org.ships.config.messages.adapter.vessel.info;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class VesselInfoValueAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Vessel Info Value";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("1");
    }

    @Override
    public Component processMessage(@NotNull String obj) {
        return Component.text(obj);
    }
}
