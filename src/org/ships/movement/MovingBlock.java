package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;

public interface MovingBlock {

    BlockPosition getBeforePosition();
    BlockPosition getAfterPosition();
    MovingBlock setAfterPosition(BlockPosition position);
    BlockDetails getCurrentBlockData();
    BlockPriority getBlockPriority();

    default MovingBlock move(BlockDetails beforePos){
        getBeforePosition().setBlock(beforePos);
        getAfterPosition().setBlock(getCurrentBlockData());
        return this;
    }

    default MovingBlock rotateLeft(BlockPosition position){
        //TODO ROTATE BLOCK LEFT
        int shift = position.getX() - position.getZ();
        int symmetry = position.getX();
        int x = getAfterPosition().getX() - (getAfterPosition().getX() - symmetry) * 2 - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(x, y, z));
        return this;
    }

    default MovingBlock rotateRight(BlockPosition position){
        //TODO ROTATE BLOCK RIGHT
        int shift = position.getX() - position.getZ();
        int symmetry = position.getZ();
        int x = getAfterPosition().getX() - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() - (getAfterPosition().getZ() - symmetry) * 2 + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(x, y, z));
        return this;
    }

    default MovingBlock moveOverAir(){
        return move(BlockTypes.AIR.getDefaultBlockDetails());
    }

    default MovingBlock moveUnderWater(){
        return move(BlockTypes.WATER.getDefaultBlockDetails());
    }



}
