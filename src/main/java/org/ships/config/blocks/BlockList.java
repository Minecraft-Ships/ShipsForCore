package org.ships.config.blocks;

import org.array.utils.ArrayUtils;
import org.core.config.ConfigurationNode;
import org.core.world.position.block.BlockType;
import org.ships.config.Config;
import org.ships.config.parsers.ShipsParsers;

import java.util.Optional;
import java.util.Set;

public interface BlockList extends Config {

    Set<BlockInstruction> getBlockList();
    Set<BlockInstruction> reloadBlockList();
    BlockList replaceBlockInstruction(BlockInstruction blockInstruction);
    BlockList saveChanges();

    default BlockInstruction getBlockInstruction(BlockType type){
        return getBlockList().stream().filter(b -> b.getType().equals(type)).findFirst().get();
    }

    static Optional<BlockInstruction> getBlockInstruction(BlockList list, BlockType type, String... extraNodes){
        String[] idSplit = type.getId().split(":");
        ConfigurationNode.KnownParser.ChildKnown<BlockInstruction> node = new ConfigurationNode.KnownParser.ChildKnown<>(ShipsParsers.NODE_TO_BLOCK_INSTRUCTION, ArrayUtils.join(String.class, extraNodes, new String[]{"BlockList", idSplit[0], idSplit[1]}));
        return list.getFile().parse(node);
    }
}
