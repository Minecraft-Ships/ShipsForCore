package org.ships.config.messages.adapter.vessel;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.common.types.Vessel;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class VesselSizeAdapter implements MessageAdapter<Vessel> {
    @Override
    public String adapterText() {
        return "Size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new Random().nextInt(99) + "");
    }

    @Override
    public AText process(AText message, Vessel obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getStructure().getOriginalRelativePositions().size() + ""));
    }
}
