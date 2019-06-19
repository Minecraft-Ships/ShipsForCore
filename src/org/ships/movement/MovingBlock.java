package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.container.ContainerTileEntity;

import java.util.Optional;

public interface MovingBlock {

    BlockPosition getBeforePosition();

    BlockPosition getAfterPosition();

    MovingBlock setAfterPosition(BlockPosition position);

    BlockDetails getStoredBlockData();

    MovingBlock setStoredBlockData(BlockDetails blockDetails);

    BlockPriority getBlockPriority();

    default MovingBlock removeBeforePosition(BlockPosition pos) {
        //setStoredBlockData(pos.getBlockDetails());
        Optional<LiveTileEntity> opLive = pos.getTileEntity();
        if(!opLive.isPresent()){
            return this;
        }
        if(opLive.get() instanceof ContainerTileEntity){
            ContainerTileEntity cte = (ContainerTileEntity)opLive.get();
            cte.getInventory().getSlots().forEach(s -> s.setItem(null));
        }
        return this;
    }

    default MovingBlock setMovingTo() {
        BlockDetails details = getStoredBlockData();
        getAfterPosition().setBlock(details);
        return this;
    }

    default MovingBlock rotateLeft(BlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getZ();
        int x = getAfterPosition().getX() - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() - (getAfterPosition().getZ() - symmetry) * 2 + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(z, y, x));
        return this;
    }

    default MovingBlock rotateRight(BlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getX();
        int x = getAfterPosition().getX() - (getAfterPosition().getX() - symmetry) * 2 - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(z, y, x));
        return this;
    }

    default MovingBlock removeBeforePositionOverAir() {
        removeBeforePosition(this.getBeforePosition()).getBeforePosition().setBlock(BlockTypes.AIR.get().getDefaultBlockDetails());
        return this;
    }

    default MovingBlock removeBeforePositionUnderWater() {
        removeBeforePosition(this.getBeforePosition()).getBeforePosition().setBlock(BlockTypes.WATER.get().getDefaultBlockDetails());
        return this;
    }

    default BlockDetails getCurrentBlockData(){
        return this.getBeforePosition().getBlockDetails();
    }


}
