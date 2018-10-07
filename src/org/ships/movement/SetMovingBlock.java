package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.AttachableDetails;
import org.core.world.position.block.details.BlockDetails;

public class SetMovingBlock implements MovingBlock {

    protected BlockPosition before;
    protected BlockPosition after;
    protected BlockDetails details;

    public SetMovingBlock(BlockPosition before, BlockPosition after){
        this(before, after, before.getBlockDetails());
    }

    public SetMovingBlock(BlockPosition before, BlockPosition after, BlockDetails details){
        this.before = before;
        this.after = after;
        this.details = details;
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
    public BlockDetails getCurrentBlockData() {
        return this.details;
    }

    @Override
    public BlockPriority getBlockPriority() {
        if((this.details.getType().equals(BlockTypes.AIR))){
            return BlockPriority.AIR;
        }else if(this.details instanceof AttachableDetails){
            return BlockPriority.ATTACHED;
        }
        return BlockPriority.NORMAL;
    }
}
