package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.world.position.block.BlockType;

import java.util.HashSet;
import java.util.Set;

public class ExpandedBlockList implements BlockList{

    ConfigurationFile file;
    BlockList expandedOn;
    Set<BlockInstruction> blocks = new HashSet<>();

    public ExpandedBlockList(ConfigurationFile file, BlockList expandedOn){
        this.file = file;
        this.expandedOn = expandedOn;
    }

    public Set<BlockInstruction> getRawBlockList(){
        return this.blocks;
    }

    @Override
    public Set<BlockInstruction> getBlockList() {
        if(blocks.size() == 0){
            reloadBlockList();
        }
        Set<BlockInstruction> set = new HashSet<>(this.expandedOn.getBlockList());
        set.addAll(this.blocks);
        return set;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        blocks.clear();
        CorePlugin.getPlatform().get(BlockType.class).stream().forEach(bt -> BlockList.getBlockInstruction(ExpandedBlockList.this, bt).ifPresent(bi -> blocks.add(bi)));
        return this.blocks;
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {

    }
}
