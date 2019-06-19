package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;

public class SetMovingBlock implements MovingBlock {

    protected BlockPosition before;
    protected BlockPosition after;
    protected BlockDetails detail;

    public SetMovingBlock(BlockPosition before, BlockPosition after){
        this(before, after, before.getBlockDetails());
    }

    public SetMovingBlock(BlockPosition before, BlockPosition after, BlockDetails details){
        this.before = before;
        this.after = after;
        this.detail = details;
    }

    @Override
    public BlockPosition getBeforePosition() {
        return this.before;
    }

    @Override
    public BlockPosition getAfterPosition() {
        return this.after;
    }

    @Override
    public MovingBlock setAfterPosition(BlockPosition position) {
        this.after = position;
        return this;
    }

    @Override
    public BlockDetails getStoredBlockData() {
        return this.detail;
    }

    @Override
    public MovingBlock setStoredBlockData(BlockDetails blockDetails) {
        this.detail = blockDetails;
        return this;
    }

    @Override
    public BlockPriority getBlockPriority() {
        if((this.detail.getType().equals(BlockTypes.AIR))) {
            return BlockPriority.AIR;
        }else if(this.detail.get(KeyedData.ATTACHABLE).isPresent()){
            return BlockPriority.ATTACHED;
        }else if(this.detail.getDirectionalData().isPresent()){
            return BlockPriority.DIRECTIONAL;
        }
        return BlockPriority.NORMAL;
    }
}
