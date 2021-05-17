package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Set;

public class VesselFlagIdAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Key Id";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("ships.is_moving");
    }
}
