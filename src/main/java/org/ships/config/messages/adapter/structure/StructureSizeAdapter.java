package org.ships.config.messages.adapter.structure;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.structure.ShipsStructure;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Set;

public class StructureSizeAdapter implements MessageAdapter<ShipsStructure> {
    @Override
    public String adapterText() {
        return "Structure size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public AText process(AText message, ShipsStructure obj) {
        return message.withAllAs(this.adapterTextFormat(),
                AText.ofPlain(obj.getOriginalRelativePositions().size() + ""));
    }
}
