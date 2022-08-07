package org.ships.movement;

import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.keyed.WaterLoggedKeyedData;
import org.core.world.position.block.entity.LiveTileEntity;
import org.core.world.position.block.entity.container.ContainerTileEntity;
import org.core.world.position.flags.physics.ApplyPhysicsFlags;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;

import java.util.Optional;

public interface MovingBlock {

    SyncBlockPosition getBeforePosition();

    MovingBlock setBeforePosition(SyncBlockPosition position);

    SyncBlockPosition getAfterPosition();

    MovingBlock setAfterPosition(SyncBlockPosition position);

    BlockDetails getStoredBlockData();

    MovingBlock setStoredBlockData(BlockDetails blockDetails);

    BlockPriority getBlockPriority();

    default MovingBlock removeBeforePosition(SyncBlockPosition pos) {
        Optional<LiveTileEntity> opLive = pos.getTileEntity();
        if (!opLive.isPresent()) {
            return this;
        }
        if (opLive.get() instanceof ContainerTileEntity) {
            ContainerTileEntity cte = (ContainerTileEntity) opLive.get();
            cte.getInventory().getSlots().forEach(s -> s.setItem(null));
        }
        return this;
    }

    default MovingBlock setMovingTo() {
        BlockDetails details = this.getStoredBlockData();
        this.getAfterPosition().setBlock(details);
        return this;
    }

    default MovingBlock rotateLeft(SyncBlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getZ();
        BlockPosition p = this.getAfterPosition();
        int x = p.getX() - shift;
        int y = p.getY();
        int z = p.getZ() - (p.getZ() - symmetry) * 2 + shift;
        this.setAfterPosition(p.getWorld().getPosition(z, y, x));
        return this;
    }

    default MovingBlock rotateRight(SyncBlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getX();
        BlockPosition p = this.getAfterPosition();
        int x = p.getX() - (p.getX() - symmetry) * 2 - shift;
        int y = p.getY();
        int z = p.getZ() + shift;
        this.setAfterPosition(p.getWorld().getPosition(z, y, x));
        return this;
    }

    default MovingBlock removeBeforePositionOverAir() {
        SyncBlockPosition p = this.getBeforePosition();
        this.removeBeforePosition(p);
        Optional<Boolean> waterLogged = p.getBlockDetails().get(WaterLoggedKeyedData.class);
        if (waterLogged.isPresent() && waterLogged.get()) {
            p.setBlock(BlockTypes.AIR.getDefaultBlockDetails(), ApplyPhysicsFlags.DEFAULT.get());
        } else {
            p.setBlock(BlockTypes.AIR.getDefaultBlockDetails());
        }
        return this;
    }

    default MovingBlock removeBeforePositionUnderWater() {
        SyncBlockPosition p = this.getBeforePosition();
        this.removeBeforePosition(p);
        Optional<Boolean> waterLogged = p.getBlockDetails().get(WaterLoggedKeyedData.class);
        if (waterLogged.isPresent() && waterLogged.get()) {
            p.setBlock(BlockTypes.WATER.getDefaultBlockDetails(), ApplyPhysicsFlags.DEFAULT.get());
        } else {
            p.setBlock(BlockTypes.WATER.getDefaultBlockDetails());
        }
        return this;
    }

    default BlockDetails getCurrentBlockData() {
        return this.getBeforePosition().getBlockDetails();

    }


}
