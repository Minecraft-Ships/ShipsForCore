package org.ships.config.blocks;

import org.core.world.position.block.BlockType;
import org.ships.config.Config;

import java.util.Set;

public interface BlockList extends Config {

    Set<BlockInstruction> getBlockList();

    default BlockInstruction getBlockInstruction(BlockType type){
        return getBlockList().stream().filter(b -> b.getType().equals(type)).findFirst().get();
    }
}
