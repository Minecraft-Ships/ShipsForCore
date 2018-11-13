package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockTypes;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.TiledBlockDetails;
import org.core.world.position.block.entity.TileEntitySnapshot;
import org.core.world.position.block.entity.container.furnace.FurnaceTileEntity;

public interface MovingBlock {

    BlockPosition getBeforePosition();

    BlockPosition getAfterPosition();

    MovingBlock setAfterPosition(BlockPosition position);

    BlockDetails getCurrentBlockData();

    BlockPriority getBlockPriority();

    default MovingBlock remove(BlockDetails beforePos) {
        BlockDetails bd = getCurrentBlockData();
        if (bd instanceof TiledBlockDetails) {
            TileEntitySnapshot tes = ((TiledBlockDetails) bd).getTileEntity();
            if (tes instanceof FurnaceTileEntity) {
                System.out.println("Checking Stores 1: " + tes.toString());
                FurnaceTileEntity fte = (FurnaceTileEntity) tes;
                fte.getInventory().getSlots().stream().forEach(s -> System.out.println("\tSlot: " + s.getPosition().orElse(-1) + " - " + s.getItem().orElse(null)));
            }
        }
        getBeforePosition().setBlock(beforePos);
        BlockDetails bd2 = getCurrentBlockData();
        if (bd2 instanceof TiledBlockDetails) {
            TileEntitySnapshot tes = ((TiledBlockDetails) bd2).getTileEntity();
            if (tes instanceof FurnaceTileEntity) {
                System.out.println("Checking Stores 2: " + tes.toString());
                FurnaceTileEntity fte = (FurnaceTileEntity) tes;
                fte.getInventory().getSlots().stream().forEach(s -> System.out.println("\tSlot: " + s.getPosition().orElse(-1) + " - " + s.getItem().orElse(null)));
            }
        }
        return this;
    }

    default MovingBlock setMovingTo() {
        getAfterPosition().setBlock(getCurrentBlockData());
        return this;
    }

    default MovingBlock rotateLeft(BlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getX();
        int x = getAfterPosition().getX() - (getAfterPosition().getX() - symmetry) * 2 - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(x, y, z));
        return this;
    }

    default MovingBlock rotateRight(BlockPosition position) {
        int shift = position.getX() - position.getZ();
        int symmetry = position.getZ();
        int x = getAfterPosition().getX() - shift;
        int y = getAfterPosition().getY();
        int z = getAfterPosition().getZ() - (getAfterPosition().getZ() - symmetry) * 2 + shift;
        setAfterPosition(getBeforePosition().getWorld().getPosition(x, y, z));
        return this;
    }

    default MovingBlock removeOverAir() {
        return remove(BlockTypes.AIR.getDefaultBlockDetails());
    }

    default MovingBlock removeUnderWater() {
        return remove(BlockTypes.WATER.getDefaultBlockDetails());
    }


}
