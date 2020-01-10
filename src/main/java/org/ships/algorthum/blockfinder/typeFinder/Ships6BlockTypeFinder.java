package org.ships.algorthum.blockfinder.typeFinder;

import org.core.CorePlugin;
import org.core.schedule.Scheduler;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.BlockListable;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class Ships6BlockTypeFinder implements BasicTypeBlockFinder {

    private class Overtime {

        private class OvertimeSection {

            private Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
            private List<BlockPosition> ret = new ArrayList<>();
            private List<BlockPosition> process = new ArrayList<>();
            private List<BlockPosition> ignore = new ArrayList<>();
            private Runnable runnable = () -> {
                for (int A = 0; A < this.process.size(); A++) {
                    BlockPosition proc = this.process.get(A);
                    for (Direction face : this.directions) {
                        BlockPosition block = proc.getRelative(face);
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

            public OvertimeSection(Collection<BlockPosition> collection, Collection<BlockPosition> ignore){
                this.process.addAll(collection);
                this.ignore.addAll(ignore);
            }

        }

        private List<BlockPosition> process = new ArrayList<>();
        private List<BlockPosition> ret = new ArrayList<>();
        private List<BlockPosition> total = new ArrayList<>();

        private OvertimeBlockTypeFinderUpdate update;
        private boolean found;
        private Predicate<BlockPosition> predicate;
        private Runnable runnable = () -> {
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            List<List<BlockPosition>> collections = new ArrayList<>();
            List<BlockPosition> current = new ArrayList<>();
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
                    .setDelay(config.getDefaultFinderStackDelay())
                    .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                    .setExecutor(() -> {
                        if(this.found){
                            return;
                        }
                        if ((this.total.size() <= Ships6BlockTypeFinder.this.limit) && (!this.ret.isEmpty())) {
                            this.process = this.ret;
                            this.ret = new ArrayList<>();

                            CorePlugin.createSchedulerBuilder().
                                    setDelay(config.getDefaultFinderStackDelay()).
                                    setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                                    setExecutor(this.runnable).
                                    build(ShipsPlugin.getPlugin()).run();
                        }else{
                            this.update.onFailedToFind();
                        }
                    })
                    .build(ShipsPlugin.getPlugin());

            for(List<BlockPosition> list : collections){
                scheduler = CorePlugin.createSchedulerBuilder()
                        .setDelay(config.getDefaultFinderStackDelay())
                        .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                        .setExecutor(() -> {
                            OvertimeSection section = new OvertimeSection(list, this.total);
                            section.runnable.run();
                            section.ret.stream().forEach(p -> {
                                if (!this.found && this.predicate.test(p)){
                                    this.found = true;
                                    this.update.onBlockFound(p);
                                }
                            });
                        })
                        .setToRunAfter(scheduler)
                        .build(ShipsPlugin.getPlugin());
            }
            scheduler.run();
        };

        public Overtime(BlockPosition position, Predicate<BlockPosition> predicate, OvertimeBlockTypeFinderUpdate update){
            this.update = update;
            this.predicate = predicate;
            process.add(position);
        }

    }

    protected int limit;
    private BlockList list;
    private Vessel vessel;

    @Override
    public Ships6BlockTypeFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public Optional<BlockPosition> findBlock(BlockPosition position, Predicate<BlockPosition> predicate) {
        int count = 0;
        Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        List<BlockPosition> ret = new ArrayList<>();
        List<BlockPosition> target = new ArrayList<>();
        List<BlockPosition> process = new ArrayList<>();
        process.add(position);
        while (count != this.limit) {
            if (process.isEmpty()) {
                return Optional.empty();
            }
            for (int A = 0; A < process.size(); A++) {
                BlockPosition proc = process.get(A);
                count++;
                for (Direction face : directions) {
                    BlockPosition block = proc.getRelative(face);
                    if (!ret.stream().anyMatch(b -> b.equals(block))) {
                        BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                            if(predicate.test(block)){
                                return Optional.of(block);
                            }else {
                                ret.add(block);
                                target.add(block);
                            }
                        }
                    }
                }
            }
            process.clear();
            process.addAll(target);
            target.clear();
        }
        return Optional.empty();
    }

    @Override
    public void findBlock(BlockPosition position, Predicate<BlockPosition> predicate, OvertimeBlockTypeFinderUpdate runAfterFullSearch) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Overtime overtime = new Overtime(position, predicate, runAfterFullSearch);
        CorePlugin.createSchedulerBuilder().
                setDelay(config.getDefaultFinderStackDelay()).
                setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                setExecutor(overtime.runnable).
                build(ShipsPlugin.getPlugin()).
                run();

    }

    @Override
    public int getBlockLimit() {
        return this.limit;
    }

    @Override
    public BasicTypeBlockFinder setBlockLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public BasicTypeBlockFinder setConnectedVessel(Vessel vessel) {
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
