package org.ships.config.messages.adapter.vessel.crew;

import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.permissions.vessel.CrewPermission;

import java.util.Collections;
import java.util.Set;

public class CrewIdAdapter implements MessageAdapter<CrewPermission> {
    @Override
    public String adapterText() {
        return "Crew Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(CrewPermission.CREW_MEMBER.getId());
    }

    @Override
    public Component processMessage(@NotNull CrewPermission obj) {
        return Component.text(obj.getId());
    }
}
