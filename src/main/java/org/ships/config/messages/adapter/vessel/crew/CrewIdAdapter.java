package org.ships.config.messages.adapter.vessel.crew;

import org.core.adventureText.AText;
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
    public AText process(@NotNull CrewPermission obj) {
        return AText.ofPlain(obj.getId());
    }
}
