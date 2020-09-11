package org.ships.movement;

import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;

public class SetMovingBlock implements MovingBlock {

    protected SyncBlockPosition before;
    protected SyncBlockPosition after;
    protected BlockDetails detail;

    public SetMovingBlock(SyncBlockPosition before, SyncBlockPosition after){
        this(before, after, before.getBlockDetails());
    }

    public SetMovingBlock(SyncBlockPosition before, SyncBlockPosition after, BlockDetails details){
        this.before = before;
        this.after = after;
        this.detail = details;
    }

    @Override
    public SyncBlockPosition getBeforePosition() {
        return this.before;
    }

    @Override
    public SyncBlockPosition getAfterPosition() {
        return this.after;
    }

    @Override
    public MovingBlock setBeforePosition(SyncBlockPosition position) {
        this.before = position;
        return this;
    }

    @Override
    public MovingBlock setAfterPosition(SyncBlockPosition position) {
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
        if((this.detail.getType().equals(BlockTypes.AIR.get()))) {
            return BlockPriority.AIR;
        }else if(this.detail.get(KeyedData.ATTACHABLE).isPresent()){
            return BlockPriority.ATTACHED;
        }else if(this.detail.getDirectionalData().isPresent()){
            return BlockPriority.DIRECTIONAL;
        }
        return BlockPriority.NORMAL;
    }
}
