package org.ships.movement;

import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
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

    public void applyMovingBlocks() {
        this.stream()
                .filter(m -> m.getBeforePosition().isPresent())
                .filter(m -> !m.getBeforePosition().get().getTileEntity().isPresent())
                .filter(m -> m.getAfterPosition().isPresent())
                .forEach(m -> {
                    m.setAfterPosition(null);
                    m.setBeforePosition(null);
                });

    }

    public <T extends Object> Collection<T> to(Function<MovingBlock, T> function){
        Set<T> set = new HashSet<>();
        this.stream().forEach(b -> set.add(function.apply(b)));
        return Collections.unmodifiableCollection(set);
    }

    public List<MovingBlock> order(Comparator<MovingBlock> order){
        List<MovingBlock> blocks = new ArrayList<>(this);
        blocks.sort(order);
        return blocks;
    }

    public Optional<MovingBlock> get(ShipsSign sign){
        return get(bd -> {
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiledEntity = bd.get(KeyedData.TILED_ENTITY);
            if(!(opTiledEntity.isPresent())){
                return false;
            }
            TileEntitySnapshot<? extends TileEntity> snapshot = opTiledEntity.get();
            if(!(snapshot instanceof SignTileEntity)){
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
        SyncBlockPosition position = positionable.getPosition() instanceof SyncBlockPosition ? (SyncBlockPosition)positionable.getPosition()
                : ((SyncExactPosition)positionable.getPosition()).toBlockPosition();
        return getBefore(position);
    }

    public Optional<MovingBlock> getBefore(SyncBlockPosition position){
        return get(position, MovingBlock::getBeforePosition);
    }

    public Optional<MovingBlock> getAfter(Positionable positionable){
        SyncBlockPosition position = positionable.getPosition() instanceof SyncBlockPosition ? (SyncBlockPosition)positionable.getPosition() : ((SyncExactPosition)positionable.getPosition()).toBlockPosition();
        return getAfter(position);
    }

    public Optional<MovingBlock> getAfter(SyncBlockPosition position){
        return get(position, MovingBlock::getAfterPosition);
    }

    private Optional<MovingBlock> get(SyncBlockPosition position, Function<MovingBlock, Optional<SyncBlockPosition>> function){
        return stream().filter(f -> {
            Optional<SyncBlockPosition> opPos = function.apply(f);
            return opPos.map(blockPosition -> blockPosition.equals(position)).orElse(false);
        }).findFirst();
    }
}
