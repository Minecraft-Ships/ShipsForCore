package org.ships.config.blocks;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.world.position.block.BlockType;
import org.ships.config.Config;
import org.ships.config.parsers.ShipsParsers;

import java.util.Collection;
import java.util.Optional;

public interface BlockList extends Config {

    Collection<BlockInstruction> getBlockList();

    Collection<BlockInstruction> reloadBlockList();

    BlockList replaceBlockInstruction(BlockInstruction blockInstruction);

    BlockList saveChanges();

    default BlockInstruction getBlockInstruction(BlockType type) {
        Collection<BlockInstruction> blockList = this.getBlockList();
        return blockList
                .stream()
                .filter(b -> b.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("BlockType of " + type.getId() + " was not registered at runtime."));
    }

    static Optional<BlockInstruction> getBlockInstruction(BlockList list, BlockType type, String... extraNodes) {
        String[] idSplit = type.getId().split(":");
        ConfigurationNode.KnownParser.ChildKnown<BlockInstruction> node = new ConfigurationNode.KnownParser.ChildKnown<>(ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, ArrayUtils.join(String.class, extraNodes, new String[]{"BlockList", idSplit[0], idSplit[1]}));
        return list.getFile().parse(node);
    }
}
