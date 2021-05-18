package org.ships.config.messages.adapter.vessel.info;

import org.core.adventureText.AText;
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
    public AText process(AText message, String obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj));
    }
}
