package org.ships.algorthum.blockfinder;

import org.core.TranslateCore;
import org.core.schedule.Scheduler;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.Position;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.blocks.BlockInstruction;
import org.ships.config.blocks.BlockList;
import org.ships.config.configuration.ShipsConfig;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPosititionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class Ships6BlockFinder implements BasicBlockFinder {

    private class Overtime {

        private class OvertimeSection {

            private final Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
            private final Collection<SyncBlockPosition> ret = new ArrayList<>();
            private final Collection<SyncBlockPosition> process = new ArrayList<>();
            private final Collection<SyncBlockPosition> ignore = new ArrayList<>();
            private final Runnable runnable = () -> {
                for (SyncBlockPosition proc : this.process) {
                    for (Direction face : this.directions) {
                        SyncBlockPosition block = proc.getRelative(face);
                        if (this.ignore.stream().anyMatch(b -> b.equals(block))) {
                            continue;
                        }
                        if (this.ret.stream().anyMatch(b -> b.equals(block))) {
                            continue;
                        }
                        BlockInstruction bi = Ships6BlockFinder.this.list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType()==BlockInstruction.CollideType.MATERIAL) {
                            this.ret.add(block);
                        }
                    }
                }
            };

            private OvertimeSection(Collection<? extends SyncBlockPosition> collection, Collection<? extends SyncBlockPosition> ignore) {
                this.process.addAll(collection);
                this.ignore.addAll(ignore);
            }

        }

        private PositionableShipsStructure pss;
        private List<SyncBlockPosition> process = new ArrayList<>();
        private List<SyncBlockPosition> ret = new ArrayList<>();
        private final List<SyncBlockPosition> total = new ArrayList<>();
        private OvertimeBlockFinderUpdate update;
        private final Runnable runnable = () -> {
            ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            Collection<List<SyncBlockPosition>> collections = new ArrayList<>();
            List<SyncBlockPosition> current = new ArrayList<>();
            for (SyncBlockPosition syncBlockPosition : this.process) {
                current.add(syncBlockPosition);
                if (current.size() >= config.getDefaultFinderStackLimit()) {
                    collections.add(current);
                    current = new ArrayList<>();
                }
            }
            collections.add(current);

            Scheduler scheduler = TranslateCore
                    .createSchedulerBuilder()
                    .setDelay(config.getDefaultFinderStackDelay())
                    .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                    .setExecutor(() -> {
                        if ((this.total.size() <= Ships6BlockFinder.this.limit) && (!this.ret.isEmpty())) {
                            this.process = this.ret;
                            this.ret = new ArrayList<>();

                            TranslateCore.createSchedulerBuilder().
                                    setDelay(config.getDefaultFinderStackDelay()).
                                    setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                                    setExecutor(this.runnable).
                                    setDisplayName("Ships 6 ASync Block Finder").
                                    build(ShipsPlugin.getPlugin()).run();
                        } else {
                            this.update.onShipsStructureUpdated(this.pss);
                        }
                    })
                    .setDisplayName("Ships 6 block finder")
                    .build(ShipsPlugin.getPlugin());

            for (List<SyncBlockPosition> list : collections) {
                scheduler = TranslateCore.createSchedulerBuilder()
                        .setDelay(config.getDefaultFinderStackDelay())
                        .setDelayUnit(config.getDefaultFinderStackDelayUnit())
                        .setExecutor(() -> {
                            OvertimeSection section = new OvertimeSection(list, this.total);
                            section.runnable.run();
                            section.ret.forEach(p -> {
                                OvertimeBlockFinderUpdate.BlockFindControl blockFind = this.update.onBlockFind(this.pss, p);
                                if (blockFind==OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                    return;
                                }
                                this.pss.addPosition(p);
                                this.ret.add(p);
                                this.total.add(p);
                            });
                        })
                        .setToRunAfter(scheduler)
                        .setDisplayName("Ships 6 Block finder")
                        .build(ShipsPlugin.getPlugin());
            }
            scheduler.run();
        };

        private Overtime(SyncBlockPosition position, OvertimeBlockFinderUpdate update) {
            this.pss = new AbstractPosititionableShipsStructure(position);
            this.update = update;
            this.process.add(position);
        }

    }

    protected int limit;
    private BlockList list;
    private Vessel vessel;

    @Override
    public @NotNull Ships6BlockFinder init() {
        ShipsPlugin plugin = ShipsPlugin.getPlugin();
        ShipsConfig config = plugin.getConfig();
        this.limit = config.getDefaultTrackSize();
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    public PositionableShipsStructure getConnectedBlocks(BlockPosition position) {
        int count = 0;
        Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        PositionableShipsStructure pss = new AbstractPosititionableShipsStructure(Position.toSync(position));
        Collection<SyncBlockPosition> ret = new ArrayList<>();
        Collection<SyncBlockPosition> target = new ArrayList<>();
        Collection<SyncBlockPosition> process = new ArrayList<>();
        process.add(Position.toSync(position));
        while (count != this.limit) {
            if (process.isEmpty()) {
                ret.forEach(pss::addPosition);
                return pss;
            }
            for (SyncBlockPosition proc : process) {
                count++;
                for (Direction face : directions) {
                    SyncBlockPosition block = proc.getRelative(face);
                    if (ret.stream().noneMatch(b -> b.equals(block))) {
                        BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollideType()==BlockInstruction.CollideType.MATERIAL) {
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
        ret.forEach(pss::addPosition);
        return pss;
    }

    @Override
    public void getConnectedBlocksOvertime(@NotNull BlockPosition position, @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        Overtime overtime = new Overtime(Position.toSync(position), runAfterFullSearch);
        TranslateCore.createSchedulerBuilder().
                setDelay(config.getDefaultFinderStackDelay()).
                setDelayUnit(config.getDefaultFinderStackDelayUnit()).
                setExecutor(overtime.runnable).
                setDisplayName("Ships 6 block finder").
                build(ShipsPlugin.getPlugin()).
                run();

    }

    @Override
    public int getBlockLimit() {
        return this.limit;
    }

    @Override
    public @NotNull BasicBlockFinder setBlockLimit(int limit) {
        this.limit = limit;
        return this;
    }

    @Override
    public Optional<Vessel> getConnectedVessel() {
        return Optional.ofNullable(this.vessel);
    }

    @Override
    public @NotNull BasicBlockFinder setConnectedVessel(@Nullable Vessel vessel) {
        this.vessel = vessel;
        this.list = ShipsPlugin.getPlugin().getBlockList();
        return this;
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six";
    }

    @Override
    public String getName() {
        return "Ships 6 R2 BlockFinder";
    }
}
