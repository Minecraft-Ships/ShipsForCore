package org.ships.algorthum.blockfinder;

import org.core.CorePlugin;
import org.core.schedule.Scheduler;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.async.ASyncBlockPosition;
import org.ships.algorthum.blockfinder.typeFinder.BasicTypeBlockFinder;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Ships6AsyncBlockFinder implements BasicBlockFinder {

    private class Overtime {

        private class OvertimeSection {

            private Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
            private List<ASyncBlockPosition> ret = new ArrayList<>();
            private List<ASyncBlockPosition> process = new ArrayList<>();
            private List<ASyncBlockPosition> ignore = new ArrayList<>();
            private Runnable runnable = () -> {
                for (int A = 0; A < this.process.size(); A++) {
                    ASyncBlockPosition proc = this.process.get(A);
                    for (Direction face : this.directions) {
                        ASyncBlockPosition block = proc.getRelative(face);
                        if(ignore.stream().anyMatch(b -> b.equals(block))){
                            continue;
                        }
                        if (ret.stream().anyMatch(b -> b.equals(block))) {
                            continue;
                        }
                        BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                            this.ret.add(block);
                        }
                    }
                }
            };

            public OvertimeSection(Collection<ASyncBlockPosition> collection, Collection<ASyncBlockPosition> ignore){
                this.process.addAll(collection);
                this.ignore.addAll(ignore);
            }

        }

        private PositionableShipsStructure pss;
        private List<ASyncBlockPosition> process = new ArrayList<>();
        private List<ASyncBlockPosition> ret = new ArrayList<>();
        private List<ASyncBlockPosition> total = new ArrayList<>();
        private OvertimeBlockFinderUpdate update;
        private Runnable runnable = () -> {
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            List<List<ASyncBlockPosition>> collections = new ArrayList<>();
            List<ASyncBlockPosition> current = new ArrayList<>();
            for(int A = 0; A < this.process.size(); A++){
                current.add(this.process.get(A));
                if(current.size() >= config.getDefaultFinderStackLimit()){
                    collections.add(current);
                    current = new ArrayList<>();
                }
            }
            collections.add(current);

            Scheduler scheduler = CorePlugin
                    .createSchedulerBuilder()
                    .setAsync(true)
                    .setDelay(config.getDefaultFinderStackDelay())
                    .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                    .setExecutor(() -> {
                        if ((this.total.size() <= Ships6AsyncBlockFinder.this.limit) && (!this.ret.isEmpty())) {
                            this.process = this.ret;
                            this.ret = new ArrayList<>();

                            CorePlugin.createSchedulerBuilder().
                                    setAsync(true).
                                    setDelay(config.getDefaultFinderStackDelay()).
                                    setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                                    setExecutor(this.runnable).
                                    setDisplayName("Ships 6 ASync Block Finder").
                                    build(ShipsPlugin.getPlugin()).run();
                        }else{
                            this.update.onShipsStructureUpdated(this.pss);
                        }
                    })
                    .setDisplayName("Ships 6 async block finder")
                    .build(ShipsPlugin.getPlugin());

            for(List<ASyncBlockPosition> list : collections){
                scheduler = CorePlugin.createSchedulerBuilder()
                        .setAsync(true)
                        .setDelay(config.getDefaultFinderStackDelay())
                        .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                        .setExecutor(() -> {
                            OvertimeSection section = new OvertimeSection(list, this.total);
                            section.runnable.run();
                            section.ret.forEach(p -> {
                                if (this.update.onBlockFind(this.pss, p)){
                                    this.pss.addPosition(p);
                                    this.ret.add(p);
                                    this.total.add(p);
                                }
                            });
                        })
                        .setToRunAfter(scheduler)
                        .setDisplayName("ASync Ships 6 Block finder")
                        .build(ShipsPlugin.getPlugin());
            }
            scheduler.run();
        };

        public Overtime(ASyncBlockPosition position, OvertimeBlockFinderUpdate update){
            this.pss = new AbstractPosititionableShipsStructure(Position.toSync(position));
            this.update = update;
            process.add(position);
        }

    }

    protected int limit;
    private BlockList list;
    private Vessel vessel;

    @Override
    public Ships6AsyncBlockFinder init() {
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
        PositionableShipsStructure pss = new AbstractPosititionableShipsStructure(Position.toSync(position));
        List<ASyncBlockPosition> ret = new ArrayList<>();
        List<ASyncBlockPosition> target = new ArrayList<>();
        List<ASyncBlockPosition> process = new ArrayList<>();
        process.add(Position.toASync(position));
        while (count != this.limit) {
            if (process.isEmpty()) {
                ret.forEach(bp -> pss.addPosition(bp));
                return pss;
            }
            for (int A = 0; A < process.size(); A++) {
                ASyncBlockPosition proc = process.get(A);
                count++;
                for (Direction face : directions) {
                    ASyncBlockPosition block = proc.getRelative(face);
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
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Overtime overtime = new Overtime(Position.toASync(position), runAfterFullSearch);
        CorePlugin.createSchedulerBuilder().
                setAsync(true).
                setDelay(config.getDefaultFinderStackDelay()).
                setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                setExecutor(overtime.runnable).
                setDisplayName("Ships 6 async block finder").
                build(ShipsPlugin.getPlugin()).
                run();

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
    public BasicTypeBlockFinder getTypeFinder() {
        return BasicTypeBlockFinder.SHIPS_SIX;
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six_async";
    }

    @Override
    public String getName() {
        return "Ships 6 ASync BlockFinder";
    }
}
