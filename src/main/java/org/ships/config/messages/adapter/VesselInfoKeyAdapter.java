package org.ships.config.messages.adapter;

import java.util.Collections;
import java.util.Set;

public class VesselInfoKeyAdapter implements MessageAdapter {
    @Override
    public String adapterText() {
        return "Vessel Info Key";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Fuel Consumption");
    }
}
