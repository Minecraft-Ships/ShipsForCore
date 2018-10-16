package org.ships.config.blocks;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class DefaultBlockList implements BlockList{

    protected File file;

    @Override
    public Set<BlockInstruction> getBlockList() {
        Set<BlockInstruction> set = new HashSet<>();
        return set;
    }

    @Override
    public File getFile() {
        return this.file;
    }
}
