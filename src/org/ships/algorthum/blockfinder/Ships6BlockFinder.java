package org.ships.algorthum.blockfinder;

import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Ships6BlockFinder implements BasicBlockFinder {

    protected int limit;
    private BlockList list;
    private ShipsVessel vessel;

    @Override
    public PositionableShipsStructure getConnectedBlocks(BlockPosition position) {
        int count = 0;
        Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        PositionableShipsStructure pss = new AbstractPosititionableShipsStructure(position);
        List<BlockPosition> ret = new ArrayList<>();
        List<BlockPosition> target = new ArrayList<>();
        List<BlockPosition> process = new ArrayList<>();
        process.add(position);
        while (count != this.limit) {
            if (process.isEmpty()) {
                ret.stream().forEach(bp -> pss.addPosition(bp));
                return pss;
            }
            for (int A = 0; A < process.size(); A++) {
                BlockPosition proc = process.get(A);
                count++;
                for (Direction face : directions) {
                    BlockPosition block = proc.getRelative(face);
                    if (ret.stream().anyMatch(b -> b.equals(block))) {
                        BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                            ret.add(block);
                            target.add(block);
                        }
                    }
                }
            }
            process.clear();
            process.addAll(target);
            target.clear();
        }
        ret.stream().forEach(bp -> pss.addPosition(bp));
        return pss;
    }

    @Override
    public void getConnectedBlocksOvertime(BlockPosition position, OvertimeBlockFinderUpdate runAfterFullSearch) {
        runAfterFullSearch.onShipsStructureUpdated(getConnectedBlocks(position));
    }

    @Override
    public int getBlockLimit() {
        return this.limit;
    }

    @Override
    public BasicBlockFinder setBlockLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Optional<ShipsVessel> getConnectedVessel() {
        return Optional.of(this.vessel);
    }

    @Override
    public BasicBlockFinder setConnectedVessel(ShipsVessel vessel) {
        this.vessel = vessel;
        if(this.vessel == null){
            this.list = ShipsPlugin.getPlugin().getBlockList();
        }else{
            this.list = this.vessel.getBlockList();
        }
        return this;
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six";
    }

    @Override
    public String getName() {
        return "Ships 6 BlockFinder";
    }
}
