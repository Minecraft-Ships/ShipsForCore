package org.ships.algorthum.blockfinder;

import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public class Ships5BlockFinder implements BasicBlockFinder {

    private int blockLimit;
    private int blockCount = 0;
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
            BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
            OvertimeBlockFinderUpdate.BlockFindControl blockFind = null;
            if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                if (event != null) {
                    blockFind = event.onBlockFind(this.shipsStructure, block);
                    if (blockFind.equals(OvertimeBlockFinderUpdate.BlockFindControl.IGNORE)) {
                        getNextBlock(event, block, directions);
                    }
                }
                if (shipsStructure.addPosition(block)) {
                    if (blockFind != null && blockFind.equals(OvertimeBlockFinderUpdate.BlockFindControl.USE_AND_FINISH)) {
                        return;
                    }
                    getNextBlock(event, block, directions);
                }
            }
        }
    }

    private PositionableShipsStructure getConnectedBlocks(BlockPosition position, OvertimeBlockFinderUpdate update) {
        this.blockCount = 0;
        this.shipsStructure = new AbstractPosititionableShipsStructure(Position.toSync(position));
        this.list = ShipsPlugin.getPlugin().getBlockList();
        Direction[] directions = Direction.withYDirections(FourFacingDirection.getFourFacingDirections());
        getNextBlock(update, position, directions);
        return this.shipsStructure;
    }

    @Override
    public Ships5BlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.blockLimit = config.getDefaultTrackSize();
        return this;
    }

    public PositionableShipsStructure getConnectedBlocks(BlockPosition position) {
        return getConnectedBlocks(position, null);
    }

    @Override
    public void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch) {
        runAfterFullSearch.onShipsStructureUpdated(getConnectedBlocks(position, runAfterFullSearch));
    }

    @Override
    public int getBlockLimit() {
        return this.blockLimit;
    }

    @Override
    public BasicBlockFinder setBlockLimit(int limit) {
        this.blockLimit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public BasicBlockFinder setConnectedVessel(Vessel vessel) {
        this.vessel = vessel;
        if (this.vessel != null && this.vessel instanceof BlockListable) {
            this.list = ((BlockListable) this.vessel).getBlockList();
        } else {
            this.list = ShipsPlugin.getPlugin().getBlockList();
        }
        return this;
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
