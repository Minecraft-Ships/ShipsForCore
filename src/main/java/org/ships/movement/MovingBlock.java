package org.ships.movement;

import org.core.world.position.impl.sync.SyncBlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.WaterLoggedKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.container.ContainerTileEntity;
import org.core.world.position.flags.physics.ApplyPhysicsFlags;

import java.util.Optional;

public interface MovingBlock {

    Optional<SyncBlockPosition> getBeforePosition();

    Optional<SyncBlockPosition> getAfterPosition();

    MovingBlock setBeforePosition(SyncBlockPosition position);

    MovingBlock setAfterPosition(SyncBlockPosition position);

    BlockDetails getStoredBlockData();

    MovingBlock setStoredBlockData(BlockDetails blockDetails);

    BlockPriority getBlockPriority();

    default MovingBlock removeBeforePosition(SyncBlockPosition pos) {
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
        getAfterPosition().ifPresent(b -> b.setBlock(details));
        return this;
    }

    default MovingBlock rotateLeft(SyncBlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getZ();
        getAfterPosition().ifPresent(p -> {
            int x = p.getX() - shift;
            int y = p.getY();
            int z = p.getZ() - (p.getZ() - symmetry) * 2 + shift;
            setAfterPosition(p.getWorld().getPosition(z, y, x));
        });
        return this;
    }

    default MovingBlock rotateRight(SyncBlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getX();
        getAfterPosition().ifPresent(p -> {
            int x = p.getX() - (p.getX() - symmetry) * 2 - shift;
            int y = p.getY();
            int z = p.getZ() + shift;
            setAfterPosition(p.getWorld().getPosition(z, y, x));
        });
        return this;
    }

    default MovingBlock removeBeforePositionOverAir() {
        getBeforePosition().ifPresent(p -> {
            removeBeforePosition(p);
            Optional<Boolean> waterLogged = p.getBlockDetails().get(WaterLoggedKeyedData.class);
            if(waterLogged.isPresent() && waterLogged.get()){
                p.setBlock(BlockTypes.AIR.get().getDefaultBlockDetails(), ApplyPhysicsFlags.DEFAULT);
            }else{
                p.setBlock(BlockTypes.AIR.get().getDefaultBlockDetails());
            }
        });
        return this;
    }

    default MovingBlock removeBeforePositionUnderWater() {
        getBeforePosition().ifPresent(p -> {
            removeBeforePosition(p);
            Optional<Boolean> waterLogged = p.getBlockDetails().get(WaterLoggedKeyedData.class);
            if(waterLogged.isPresent() && waterLogged.get()) {
                p.setBlock(BlockTypes.WATER.get().getDefaultBlockDetails(), ApplyPhysicsFlags.DEFAULT);
            }else{
                p.setBlock(BlockTypes.WATER.get().getDefaultBlockDetails());
            }
        });
        return this;
    }

    default Optional<BlockDetails> getCurrentBlockData(){
        Optional<SyncBlockPosition> opBlock = getBeforePosition();
        if(opBlock.isPresent()){
            return Optional.of(opBlock.get().getBlockDetails());
        }
        return Optional.empty();

    }


}
