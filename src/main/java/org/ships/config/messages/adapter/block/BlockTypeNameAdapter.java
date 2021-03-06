package org.ships.config.messages.adapter.block;

import org.core.CorePlugin;
import org.core.adventureText.AText;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.stream.Collectors;

public class BlockTypeNameAdapter implements MessageAdapter<BlockType> {
    @Override
    public String adapterText() {
        return "Block Type Name";
    }

    @Override
    public Set<String> examples() {
        return CorePlugin.getPlatform().getBlockTypes().stream().map(Identifiable::getName).collect(Collectors.toSet());
    }

    @Override
    public AText process(AText message, BlockType obj) {
        return message.withAllAs(this.adapterTextFormat(), AText.ofPlain(obj.getName()));
    }
}
