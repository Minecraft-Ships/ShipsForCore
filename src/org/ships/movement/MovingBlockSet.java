package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.ExactPosition;
import org.core.world.position.Positionable;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.ships.vessel.sign.ShipsSign;

import java.util.*;
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
            if(!(bd instanceof TiledBlockDetails)){
                return false;
            }
            TiledBlockDetails tbd = (TiledBlockDetails)bd;
            TileEntitySnapshot snapshot = tbd.getTileEntity();
            if(!(snapshot instanceof SignTileEntity)){
                return false;
            }
            SignTileEntity ste = (SignTileEntity)snapshot;
            return sign.isSign(ste);
        });
    }

    public Optional<MovingBlock> get(Predicate<BlockDetails> predicate){
        return stream().filter(mb -> predicate.test(mb.getCurrentBlockData())).findFirst();
    }

    public MovingBlock getBefore(Positionable positionable){
        BlockPosition position = positionable instanceof BlockPosition ? (BlockPosition)positionable : ((ExactPosition)positionable).toBlockPosition();
        return getBefore(position);
    }

    public MovingBlock getBefore(BlockPosition position){
        return get(position, b -> b.getBeforePosition().equals(position));
    }

    public MovingBlock getAfter(Positionable positionable){
        BlockPosition position = positionable instanceof BlockPosition ? (BlockPosition)positionable : ((ExactPosition)positionable).toBlockPosition();
        return getBefore(position);
    }

    public MovingBlock getAfter(BlockPosition position){
        return get(position, b -> b.getAfterPosition().equals(position));
    }

    private MovingBlock get(BlockPosition position, Predicate<MovingBlock> function){
        return stream().filter(function).findFirst().get();
    }
}
