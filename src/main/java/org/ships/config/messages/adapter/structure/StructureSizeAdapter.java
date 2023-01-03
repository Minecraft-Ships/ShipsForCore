package org.ships.config.messages.adapter.structure;

import org.core.adventureText.AText;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.security.SecureRandom;
import java.util.Collections;
import java.util.Set;

public class StructureSizeAdapter implements MessageAdapter<PositionableShipsStructure> {
    @Override
    public String adapterText() {
        return "Structure size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new SecureRandom().nextInt(99) + "");
    }

    @Override
    public AText process(@NotNull PositionableShipsStructure obj) {
        return AText.ofPlain((obj.getOriginalRelativePositionsToCenter().size() + 1) + "");
    }
}
