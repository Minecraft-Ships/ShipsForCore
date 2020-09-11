package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.config.ConfigurationStream;
import org.core.world.position.block.BlockType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class ExpandedBlockList implements BlockList{

    private final ConfigurationStream.ConfigurationFile file;
    private final BlockList expandedOn;
    private Set<BlockInstruction> originalBlocks = new HashSet<>();
    private Set<BlockInstruction> fullBlocks = new HashSet<>();

    public ExpandedBlockList(ConfigurationStream.ConfigurationFile file, BlockList expandedOn){
        this.file = file;
        if(expandedOn != null){
            expandedOn.reloadBlockList();
        }
        this.expandedOn = expandedOn;
    }

    public Set<BlockInstruction> getRawBlockList(){
        return this.originalBlocks;
    }

    @Override
    public Set<BlockInstruction> getBlockList() {
        if(this.fullBlocks.size() == 0){
            this.file.reload();
            this.originalBlocks = reloadBlockList();
            this.fullBlocks = new HashSet<>(this.expandedOn.getBlockList());
            this.fullBlocks.addAll(this.originalBlocks);
        }
        return this.fullBlocks;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        this.originalBlocks.clear();
        this.fullBlocks.clear();
        Set<BlockInstruction> bins = new HashSet<>();
        Collection<BlockType> blocks = CorePlugin.getPlatform().getBlockTypes();
        blocks.forEach(bt -> {
            Optional<BlockInstruction> opBlock = BlockList.getBlockInstruction(ExpandedBlockList.this, bt);
            if (opBlock.isPresent()){
                this.originalBlocks.add(opBlock.get());
                bins.add(opBlock.get());
            }
        });
        return bins;
    }

    @Override
    public BlockList replaceBlockInstruction(BlockInstruction blockInstruction) {
        Optional<BlockInstruction> opBi = this.originalBlocks.stream().filter(b -> b.getType().equals(blockInstruction.getType())).findAny();
        if(opBi.isPresent()){
            opBi.get().setCollideType(blockInstruction.getCollideType());
        }else if(this.expandedOn != null){
            this.expandedOn.replaceBlockInstruction(blockInstruction);
        }
        return this;
    }

    @Override
    public BlockList saveChanges() {
        return this;
    }

    @Override
    public ConfigurationStream.ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {

    }

    @Override
    public boolean equals(Object obj){
        if(!(obj instanceof BlockList)){
            return false;
        }
        BlockList list = (BlockList)obj;
        return list.getFile().getFile().equals(this.getFile().getFile());
    }
}
