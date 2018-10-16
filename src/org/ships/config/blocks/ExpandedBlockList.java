package org.ships.config.blocks;

import java.io.File;
import java.util.Set;

public class ExpandedBlockList implements BlockList{

    File file;
    BlockList expandedOn;

    public ExpandedBlockList(File file, BlockList expandedOn){
        this.file = file;
        this.expandedOn = expandedOn;
    }

    @Override
    public Set<BlockInstruction> getBlockList() {
        Set<BlockInstruction> set = this.expandedOn.getBlockList();
        return set;
    }

    @Override
    public File getFile() {
        return this.file;
    }
}
