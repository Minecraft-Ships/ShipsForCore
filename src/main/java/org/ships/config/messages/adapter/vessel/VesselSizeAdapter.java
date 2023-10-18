package org.ships.config.messages.adapter.vessel;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Set;

public class VesselSizeAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Vessel size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public Component processMessage(@NotNull Vessel obj) {
        return Component.text((obj.getStructure().getOriginalRelativePositionsToCenter().size() + 1) + "");
    }
}
