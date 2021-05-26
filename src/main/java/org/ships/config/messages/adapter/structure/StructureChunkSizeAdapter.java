package org.ships.config.messages.adapter.structure;

import org.core.adventureText.AText;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.vessel.structure.PositionableShipsStructure;
import org.ships.vessel.structure.ShipsStructure;

import java.util.Collections;
import java.util.Random;
import java.util.Set;

public class StructureChunkSizeAdapter implements MessageAdapter<ShipsStructure> {
    @Override
    public String adapterText() {
        return "Structure Chunk Size";
    }

    @Override
    public Set<String> examples() {
        return Collections.singleton(new Random().nextInt(99) + "");
    }

    @Override
    public AText process(AText message, ShipsStructure obj) {
        if (!(obj instanceof PositionableShipsStructure)) {
            return message.withAllAs(this.adapterTextFormat(), AText.ofPlain("Unknown"));
        }
        PositionableShipsStructure structure = (PositionableShipsStructure) obj;
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(structure.getChunks().size() + ""));
    }
}
