package org.ships.config.messages.adapter.vessel.info;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Collections;
import java.util.Set;

public class VesselInfoKeyAdapter implements MessageAdapter<String> {
    @Override
    public String adapterText() {
        return "Vessel Info Key";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton("Fuel Consumption");
    }

    @Override
    public AText process(AText message, String obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj));
    }
}
