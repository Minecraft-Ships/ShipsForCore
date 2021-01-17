package org.ships.algorthum.blockfinder.typeFinder;

import org.core.CorePlugin;
import org.core.entity.living.human.player.LivePlayer;
import org.core.schedule.Scheduler;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.sync.SyncBlockPosition;
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
            private List<SyncBlockPosition> ret = new ArrayList<>();
            private List<SyncBlockPosition> process = new ArrayList<>();
            private List<SyncBlockPosition> ignore = new ArrayList<>();
            private Runnable runnable = () -> {
                for (int A = 0; A < this.process.size(); A++) {
                    SyncBlockPosition proc = this.process.get(A);
                    for (Direction face : this.directions) {
                        SyncBlockPosition block = proc.getRelative(face);
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

            public OvertimeSection(Collection<SyncBlockPosition> collection, Collection<SyncBlockPosition> ignore){
                this.process.addAll(collection);
                this.ignore.addAll(ignore);
            }

        }

        private List<SyncBlockPosition> process = new ArrayList<>();
        private List<SyncBlockPosition> ret = new ArrayList<>();
        private List<SyncBlockPosition> total = new ArrayList<>();

        private OvertimeBlockTypeFinderUpdate update;
        private boolean found;
        private Predicate<SyncBlockPosition> predicate;
        private Runnable runnable = () -> {
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            List<List<SyncBlockPosition>> collections = new ArrayList<>();
            List<SyncBlockPosition> current = new ArrayList<>();
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

            for(List<SyncBlockPosition> list : collections){
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

        public Overtime(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate, OvertimeBlockTypeFinderUpdate update){
            this.update = update;
            this.predicate = predicate;
            process.add(position);
        }

    }

    protected int limit;
    private BlockList list;
    private Vessel vessel;
    private LivePlayer removeThis;

    public void setPlayer(LivePlayer player){
        this.removeThis = player;
    }

    @Override
    public Ships6BlockTypeFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public Optional<SyncBlockPosition> findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate) {
        int count = 0;
        Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        List<SyncBlockPosition> ret = new ArrayList<>();
        List<SyncBlockPosition> target = new ArrayList<>();
        List<SyncBlockPosition> process = new ArrayList<>();
        process.add(position);
        if(this.removeThis != null){
            this.removeThis.sendMessagePlain("Block Added: Count: " + count + " | Limit: " + this.limit);
        }
        while (count != this.limit) {
            if (process.isEmpty()) {
                if(this.removeThis != null){
                    this.removeThis.sendMessagePlain("Process empty");
                }
                return Optional.empty();
            }
            for (int A = 0; A < process.size(); A++) {
                SyncBlockPosition proc = process.get(A);
                count++;
                for (Direction face : directions) {
                    SyncBlockPosition block = proc.getRelative(face);
                    if (!ret.stream().anyMatch(b -> b.equals(block))) {
                        if(this.removeThis != null){
                            this.removeThis.sendMessagePlain("Checking Block");
                        }
                        BlockInstruction bi = list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType().equals(BlockInstruction.CollideType.MATERIAL)) {
                            if(this.removeThis != null){
                                this.removeThis.sendMessagePlain("Block met requirements");
                            }
                            if(predicate.test(block)){
                                return Optional.of(block);
                            }else {
                                ret.add(block);
                                target.add(block);
                                if(this.removeThis != null){
                                    this.removeThis.sendMessagePlain("Added to ret and target");
                                }
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
    public void findBlock(SyncBlockPosition position, Predicate<SyncBlockPosition> predicate, OvertimeBlockTypeFinderUpdate runAfterFullSearch) {
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
