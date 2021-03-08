package org.ships.algorthum.blockfinder.exact;

import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

public class Ships5ExactBlockFinder implements ExactBlockFinder {

    private final int blockLimit;
    private int blockCount;
    private final BlockList list;
    private final Set<BlockPosition> positions = new HashSet<>();

    public Ships5ExactBlockFinder(BlockList list, int limit){
        this.blockLimit = limit;
        this.list = list;
    }

    @Override
    public Optional<BlockPosition> findFirst(BlockPosition starting, Predicate<BlockPosition> predicate) {
        try {
        BlockPosition blockPos = getNextBlock(predicate, starting, Direction.withYDirections(FourFacingDirection.getFourFacingDirections()));
            return Optional.of(blockPos);
        }catch (ArithmeticException e){
            return Optional.empty();
        }
    }

    private BlockPosition getNextBlock(Predicate<BlockPosition> predicate, BlockPosition position, Direction... directions){
        if(this.blockLimit != -1 && this.blockCount >= this.blockLimit){
            throw new ArithmeticException("Block limit met");
        }
        this.blockCount++;
        for(Direction direction : directions){
            BlockPosition block = position.getRelative(direction);
            BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
            if(bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)){
                    if (predicate.test(position)){
                        return position;
                    }
                if (this.positions.add(block)){
                    return getNextBlock(predicate, block, directions);
                }
            }
        }
        return position;
    }
}
