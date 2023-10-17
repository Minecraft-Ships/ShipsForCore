package org.ships.config.messages.adapter.block;

import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
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
        return TranslateCore
                .getPlatform()
                .getBlockTypes()
                .stream()
                .map(Identifiable::getName)
                .collect(Collectors.toSet());
    }

    @Override
    public Component processMessage(@NotNull BlockType obj) {
        return Component.text(obj.getName());
    }
}
