package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Positionable;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.vessel.sign.ShipsSign;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class MovingBlockSet extends HashSet<MovingBlock> {

    public static final Comparator<MovingBlock> ORDER_ON_PRIORITY = (o1, o2) -> {
        int p1 = o1.getBlockPriority().getPriorityNumber();
        int p2 = o2.getBlockPriority().getPriorityNumber();
        if(p1 == p2){
            return 0;
        }else if(p1 > p2){
            return 1;
        }
        return -1;
    };

    public MovingBlockSet(){
        super();
    }

    public MovingBlockSet(Collection<MovingBlock> collection){
        super(collection);
    }

    public List<MovingBlock> order(Comparator<MovingBlock> order){
        List<MovingBlock> blocks = new ArrayList<>(this);
        Collections.sort(blocks, order);
        return blocks;
    }

    public Optional<MovingBlock> get(ShipsSign sign){
        return get(bd -> {
            System.out.println("Block: " + bd.getType().getId());
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiledEntity = bd.get(KeyedData.TILED_ENTITY);
            if(!(opTiledEntity.isPresent())){
                System.out.println("\tFailed: TileEntity");
                return false;
            }
            TileEntitySnapshot<? extends TileEntity> snapshot = opTiledEntity.get();
            if(!(snapshot instanceof SignTileEntity)){
                System.out.println("\tFailed: SignTileEntity");
                return false;
            }
            SignTileEntity ste = (SignTileEntity)snapshot;
            return sign.isSign(ste);
        });
    }

    public Optional<MovingBlock> get(Predicate<BlockDetails> predicate){
        return stream().filter(mb -> predicate.test(mb.getStoredBlockData())).findFirst();
    }

    public Optional<MovingBlock> getBefore(Positionable positionable){
        BlockPosition position = positionable.getPosition() instanceof BlockPosition ? (BlockPosition)positionable.getPosition()
                : ((ExactPosition)positionable.getPosition()).toBlockPosition();
        return getBefore(position);
    }

    public Optional<MovingBlock> getBefore(BlockPosition position){
        return get(position, b -> b.getBeforePosition());
    }

    public Optional<MovingBlock> getAfter(Positionable positionable){
        BlockPosition position = positionable.getPosition() instanceof BlockPosition ? (BlockPosition)positionable.getPosition() : ((ExactPosition)positionable.getPosition()).toBlockPosition();
        return getAfter(position);
    }

    public Optional<MovingBlock> getAfter(BlockPosition position){
        return get(position, b -> b.getAfterPosition());
    }

    private Optional<MovingBlock> get(BlockPosition position, Function<MovingBlock, BlockPosition> function){
        return stream().filter(f -> function.apply(f).equals(position)).findFirst();
    }
}
