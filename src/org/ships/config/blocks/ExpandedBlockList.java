package org.ships.config.blocks;

import org.core.CorePlugin;
import org.core.configuration.ConfigurationFile;
import org.core.world.position.block.BlockType;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
        set.removeAll(set.stream().filter(b -> this.blocks.stream().anyMatch(m -> m.getType().equals(b.getType()))).collect(Collectors.toList()));
        set.addAll(this.blocks);
        return set;
    }

    @Override
    public Set<BlockInstruction> reloadBlockList() {
        this.blocks.clear();
        Set<BlockInstruction> bins = new HashSet<>();
        Collection<BlockType> blocks = CorePlugin.getPlatform().getBlockTypes();
        blocks.forEach(bt -> {
            Optional<BlockInstruction> opBlock = BlockList.getBlockInstruction(ExpandedBlockList.this, bt);
            if (opBlock.isPresent()){
                this.blocks.add(opBlock.get());
                bins.add(opBlock.get());
            }else{
                opBlock = this.expandedOn.getBlockList().stream().filter(bi -> bi.getType().equals(bt)).findAny();
                if(opBlock.isPresent()){
                    bins.add(opBlock.get());
                }else{
                    bins.add(new BlockInstruction(bt));
                }
            }
        });
        return bins;
    }

    @Override
    public ConfigurationFile getFile() {
        return this.file;
    }

    @Override
    public void recreateFile() {

    }
}
