package org.ships.algorthum.blockfinder;

import org.core.CorePlugin;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Ships6BlockFinder implements BasicBlockFinder {

    private class Overtime {

        private Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        private PositionableShipsStructure pss;
        private List<BlockPosition> ret = new ArrayList<>();
        private List<BlockPosition> target = new ArrayList<>();
        private List<BlockPosition> process = new ArrayList<>();
        private OvertimeBlockFinderUpdate update;
        private Runnable runnable = () -> {
            for (int A = 0; A < this.process.size(); A++) {
                BlockPosition proc = this.process.get(A);
                for (Direction face : this.directions) {
                    BlockPosition block = proc.getRelative(face);
                    if (!ret.stream().anyMatch(b -> b.equals(block))) {
                        BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                            if (this.update.onBlockFind(this.pss, block)){
                                this.pss.addPosition(block);
                                this.ret.add(block);
                                this.target.add(block);
                            }
                        }
                    }
                }
            }
            this.process.clear();
            this.process.addAll(this.target);
            this.target.clear();
            if ((this.ret.size() <= Ships6BlockFinder.this.limit) && (!this.process.isEmpty())) {
                CorePlugin.createSchedulerBuilder().setDelay(1).setDelayUnit(TimeUnit.MILLISECONDS).setExecutor(this.runnable).build(ShipsPlugin.getPlugin()).run();
            }else{
                this.update.onShipsStructureUpdated(this.pss);
            }
        };

        public Overtime(BlockPosition position, OvertimeBlockFinderUpdate update){
            this.pss = new AbstractPosititionableShipsStructure(position);
            this.update = update;
            process.add(position);
        }

    }

    protected int limit;
    private BlockList list;
    private Vessel vessel;

    @Override
    public Ships6BlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

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
                ret.forEach(bp -> pss.addPosition(bp));
                return pss;
            }
            for (int A = 0; A < process.size(); A++) {
                BlockPosition proc = process.get(A);
                count++;
                for (Direction face : directions) {
                    BlockPosition block = proc.getRelative(face);
                    if (!ret.stream().anyMatch(b -> b.equals(block))) {
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
        Overtime overtime = new Overtime(position, runAfterFullSearch);
        CorePlugin.createSchedulerBuilder().setDelay(1).setDelayUnit(TimeUnit.MILLISECONDS).setExecutor(overtime.runnable).build(ShipsPlugin.getPlugin()).run();

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
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public BasicBlockFinder setConnectedVessel(Vessel vessel) {
        this.vessel = vessel;
        if(this.vessel != null && (this.vessel instanceof BlockListable)) {
            this.list = ((BlockListable)this.vessel).getBlockList();
        }else{
            this.list = ShipsPlugin.getPlugin().getBlockList();
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
