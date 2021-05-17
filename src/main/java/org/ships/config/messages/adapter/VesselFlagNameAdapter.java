package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Set;

public class VesselFlagNameAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Key Name";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Is Moving");
    }
}
