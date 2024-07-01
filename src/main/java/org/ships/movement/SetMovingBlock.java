package org.ships.movement;

import org.core.vector.type.Vector3;
import org.core.world.WorldExtent;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.movement.action.MovementAction;

public class SetMovingBlock implements MovingBlock {

    protected SyncBlockPosition before;
    protected BlockDetails detail;
    private MovementAction action;

    public SetMovingBlock(SyncBlockPosition before, MovementAction action) {
        this(before, action, before.getBlockDetails());
    }

    public SetMovingBlock(SyncBlockPosition before, MovementAction action, BlockDetails details) {
        this.before = before;
        this.action = action;
        this.detail = details;
    }

    public SetMovingBlock(SyncBlockPosition before, SyncBlockPosition after) {
        this(before, after, before.getBlockDetails());
    }

    @Deprecated
    public SetMovingBlock(SyncBlockPosition before, SyncBlockPosition after, BlockDetails details) {
        this.before = before;
        this.action = MovementAction.plus(after.getPosition().minus(before.getPosition()));
        this.detail = details;
    }

    @Override
    public SyncBlockPosition getBeforePosition() {
        return this.before;
    }

    @Override
    public MovingBlock setBeforePosition(SyncBlockPosition position) {
        this.before = position;
        return this;
    }

    @Override
    public SyncBlockPosition getAfterPosition() {
        WorldExtent world = this.before.getWorld();
        Vector3<Integer> newPos = this.action.move(this.before.getPosition());
        return (SyncBlockPosition) world.getPosition(newPos);
    }

    @Override
    @Deprecated(forRemoval = true)
    public MovingBlock setAfterPosition(SyncBlockPosition position) {
        this.action = MovementAction.plus(position.getPosition().minus(this.before.getPosition()));
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
    public MovementAction getAction() {
        return this.action;
    }

    @Override
    public MovingBlock setAction(@NotNull MovementAction action) {
        this.action = action;
        return this;
    }

    @Override
    public BlockPriority getBlockPriority() {
        if ((this.detail.getType().equals(BlockTypes.AIR))) {
            return BlockPriorities.AIR;
        } else if (this.detail.get(KeyedData.ATTACHABLE).isPresent()) {
            return BlockPriorities.ATTACHED;
        } else if (this.detail.getDirectionalData().isPresent()) {
            return BlockPriorities.DIRECTIONAL;
        }
        return BlockPriorities.NORMAL;
    }
}
