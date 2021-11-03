package org.ships.config.messages.adapter.vessel.error;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class VesselSizeErrorAdapter implements MessageAdapter<Integer> {
    @Override
    public String adapterText() {
        return "Vessel Size Error";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public AText process(AText message, Integer obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.toString()));
    }
}
