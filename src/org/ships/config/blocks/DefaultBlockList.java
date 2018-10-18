package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.world.position.block.BlockType;

import java.util.HashSet;
import java.util.Set;

public class DefaultBlockList implements BlockList {

    protected ConfigurationFile file;
    protected Set<BlockInstruction> blocks = new HashSet<>();

    @Override
    public Set<BlockInstruction> getBlockList() {
        if(blocks.size() == 0){
            return reloadBlockList();
        }
        return this.blocks;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        blocks.clear();
        CorePlugin.getPlatform().get(BlockType.class).stream().forEach(bt -> BlockList.getBlockInstruction(DefaultBlockList.this, bt).ifPresent(bi -> blocks.add(bi)));
        return this.blocks;
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }
}
