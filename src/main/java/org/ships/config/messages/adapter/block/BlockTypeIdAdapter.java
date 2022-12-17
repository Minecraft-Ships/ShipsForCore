package org.ships.config.messages.adapter.block;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;

import java.util.Set;
import java.util.stream.Collectors;

public class BlockTypeIdAdapter implements MessageAdapter<BlockType> {
    @Override
    public String adapterText() {
        return "Block Type Id";
    }

    @Override
    public Set<String> examples() {
        return TranslateCore
                .getPlatform()
                .getBlockTypes()
                .stream()
                .map(Identifiable::getId)
                .collect(Collectors.toSet());
    }

    @Override
    public AText process(@NotNull BlockType obj) {
        return AText.ofPlain(obj.getId());
    }
}
