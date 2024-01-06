package org.ships.config.messages.adapter.block;

import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
import org.core.utils.Identifiable;
import org.core.world.position.block.BlockType;
import org.jetbrains.annotations.NotNull;
import org.ships.config.messages.adapter.MessageAdapter;
import org.ships.config.messages.adapter.category.AdapterCategories;
import org.ships.config.messages.adapter.category.AdapterCategory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class BlockTypeIdAdapter implements MessageAdapter<BlockType> {
    @Override
    public String adapterText() {
        return "Block Type Id";
    }

    @Override
    public Class<?> adaptingType() {
        return BlockType.class;
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
    public Collection<AdapterCategory<BlockType>> categories() {
        return List.of(AdapterCategories.BLOCK_TYPE);
    }

    @Override
    public Component processMessage(@NotNull BlockType obj) {
        return Component.text(obj.getId());
    }
}
