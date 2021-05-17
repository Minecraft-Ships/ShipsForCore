package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Set;

public class VesselInfoValueAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Info Value";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("1");
    }
}
