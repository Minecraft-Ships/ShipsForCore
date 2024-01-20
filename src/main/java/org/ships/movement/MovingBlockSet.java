package org.ships.movement;

import org.core.world.position.Positionable;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.KeyedData;
import org.core.world.position.block.entity.TileEntity;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.sign.SignTileEntity;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.impl.sync.SyncExactPosition;
import org.ships.vessel.sign.ShipsSign;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

public class MovingBlockSet extends HashSet<MovingBlock> {

    public static final Comparator<MovingBlock> ORDER_ON_PRIORITY = (o1, o2) -> {
        int p1 = o1.getBlockPriority().getPriorityNumber();
        int p2 = o2.getBlockPriority().getPriorityNumber();
        if (p1 == p2) {
            return 0;
        } else if (p1 > p2) {
            return 1;
        }
        return -1;
    };
    private MovingBlockSet blocks;

    public MovingBlockSet() {

    }

    public MovingBlockSet(Collection<? extends MovingBlock> collection) {
        super(collection);
    }

    public MovingBlockSet getOriginal() {
        if (this.blocks == null) {
            return this;
        }
        return this.blocks;
    }

    public <T> Collection<T> to(Function<? super MovingBlock, ? extends T> function) {
        Set<T> set = new HashSet<>();
        this.forEach(b -> set.add(function.apply(b)));
        return Collections.unmodifiableCollection(set);
    }

    public List<MovingBlock> order(Comparator<? super MovingBlock> order) {
        List<MovingBlock> blocks = new ArrayList<>(this);
        blocks.sort(order);
        return blocks;
    }

    public Optional<MovingBlock> get(ShipsSign sign) {
        return this.get(bd -> {
            Optional<TileEntitySnapshot<? extends TileEntity>> opTiledEntity = bd.get(KeyedData.TILED_ENTITY);
            if (opTiledEntity.isEmpty()) {
                return false;
            }
            TileEntitySnapshot<? extends TileEntity> snapshot = opTiledEntity.get();
            if (!(snapshot instanceof SignTileEntity)) {
                return false;
            }
            SignTileEntity ste = (SignTileEntity) snapshot;
            return sign.isSign(ste);
        });
    }

    public Optional<MovingBlock> get(Predicate<? super BlockDetails> predicate) {
        return this.stream().filter(mb -> predicate.test(mb.getStoredBlockData())).findFirst();
    }

    public Optional<MovingBlock> getBefore(Positionable<?> positionable) {
        SyncBlockPosition position = positionable.getPosition() instanceof SyncBlockPosition ? (SyncBlockPosition) positionable.getPosition() : ((SyncExactPosition) positionable.getPosition()).toBlockPosition();
        return this.getBefore(position);
    }

    public Optional<MovingBlock> getBefore(SyncBlockPosition position) {
        return this.get(position, MovingBlock::getBeforePosition);
    }

    public Optional<MovingBlock> getAfter(Positionable<?> positionable) {
        SyncBlockPosition position = positionable.getPosition() instanceof SyncBlockPosition ? (SyncBlockPosition) positionable.getPosition() : ((SyncExactPosition) positionable.getPosition()).toBlockPosition();
        return this.getAfter(position);
    }

    public Optional<MovingBlock> getAfter(SyncBlockPosition position) {
        return this.get(position, MovingBlock::getAfterPosition);
    }

    private Optional<MovingBlock> get(SyncBlockPosition position,
                                      Function<? super MovingBlock, ? extends SyncBlockPosition> function) {
        return this.stream().filter(f -> {
            SyncBlockPosition pos = function.apply(f);
            return pos.equals(position);
        }).findFirst();
    }
}
