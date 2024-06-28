package org.ships.algorthum.blockfinder;

import org.core.config.ConfigurationNode;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.jetbrains.annotations.NotNull;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.node.DedicatedNode;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Ships5BlockFinder implements BasicBlockFinder {

    private int blockLimit;
    private int blockCount;
    private PositionableShipsStructure shipsStructure;
    private Vessel vessel;
    private BlockList list;

    private void getNextBlock(OvertimeBlockFinderUpdate event, BlockPosition position, Direction... directions) {
        if (this.blockLimit != -1 && this.blockCount >= this.blockLimit) {
            return;
        }
        this.blockCount++;
        for (Direction direction : directions) {
            BlockPosition block = position.getRelative(direction);
            BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
            OvertimeBlockFinderUpdate.BlockFindControl blockFind = null;
            if (bi.getCollide() == CollideType.MATERIAL) {
                if (event != null) {
                    blockFind = event.onBlockFind(this.shipsStructure, block.getPosition());
                    if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                        this.getNextBlock(event, block, directions);
                    }
                }
                if (this.shipsStructure.addPositionRelativeToWorld(block)) {
                    if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH) {
                        return;
                    }
                    this.getNextBlock(event, block, directions);
                }
            }
        }
    }

    private PositionableShipsStructure getConnectedBlocks(BlockPosition position, OvertimeBlockFinderUpdate update) {
        this.blockCount = 0;
        this.shipsStructure = new AbstractPositionableShipsStructure(position.toSyncPosition());
        this.list = ShipsPlugin.getPlugin().getBlockList();
        Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
        this.getNextBlock(update, position, directions);
        return this.shipsStructure;
    }

    @Override
    public @NotNull Ships5BlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.blockLimit = config.getDefaultTrackSize();
        return this;
    }

    @Override
    public CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                                                                    @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        return CompletableFuture.completedFuture(this.getConnectedBlocks(position, runAfterFullSearch));
    }

    @Override
    public int getBlockLimit() {
        return this.blockLimit;
    }

    @Override
    public @NotNull BasicBlockFinder setBlockLimit(int limit) {
        this.blockLimit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public @NotNull BasicBlockFinder setConnectedVessel(Vessel vessel) {
        this.vessel = vessel;
        this.list = ShipsPlugin.getPlugin().getBlockList();

        return this;
    }

    public PositionableShipsStructure getConnectedBlocks(BlockPosition position) {
        return this.getConnectedBlocks(position, null);
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_five";
    }

    @Override
    public String getName() {
        return "Ships 5 BlockFinder";
    }
}
