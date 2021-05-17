package org.ships.config.messages.adapter;

import org.ships.permissions.vessel.CrewPermission;

import java.util.Collections;
import java.util.Set;

public class CrewIdAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Crew Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(CrewPermission.CREW_MEMBER.getId());
    }
}
