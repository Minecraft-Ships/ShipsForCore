package org.ships.algorthum.blockfinder;

import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.impl.BlockPosition;
import org.core.world.position.impl.sync.SyncBlockPosition;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ships.config.blocks.BlockList;
import org.ships.config.blocks.instruction.BlockInstruction;
import org.ships.config.blocks.instruction.CollideType;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.config.node.RawDedicatedNode;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.AbstractPositionableShipsStructure;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.io.File;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class Ships6BlockFinder implements BasicBlockFinder {

    private final class Overtime {

        private final class OvertimeSection {

            private final Direction[] directions = FourFacingDirection.withYDirections(
                    FourFacingDirection.getFourFacingDirections());
            private final Collection<SyncBlockPosition> ret = new ArrayList<>();
            private final Collection<SyncBlockPosition> process = new ArrayList<>();
            private final Collection<SyncBlockPosition> ignore = new ArrayList<>();
            private final Consumer<Scheduler> runnable = (sch) -> {
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
                        if (bi.getCollide() == CollideType.MATERIAL) {
                            this.ret.add(block);
                        }
                    }
                }
            };

            private OvertimeSection(Collection<? extends SyncBlockPosition> collection,
                                    Collection<? extends SyncBlockPosition> ignore) {
                this.process.addAll(collection);
                this.ignore.addAll(ignore);
            }

        }

        private final List<SyncBlockPosition> total = new ArrayList<>();
        private PositionableShipsStructure pss;
        private List<SyncBlockPosition> process = new ArrayList<>();
        private List<SyncBlockPosition> ret = new ArrayList<>();
        private OvertimeBlockFinderUpdate update;

        private int stackLimit;
        private int stackDelay;
        private TimeUnit stackDelayUnit;
        private CompletableFuture<PositionableShipsStructure> onComplete;


        private Overtime(SyncBlockPosition position,
                         OvertimeBlockFinderUpdate update,
                         ShipsConfig config,
                         CompletableFuture<PositionableShipsStructure> onComplete) {
            this.pss = new AbstractPositionableShipsStructure(position);
            this.update = update;
            this.process.add(position);
            this.stackLimit = config.getDefaultFinderStackLimit();
            this.stackDelay = config.getDefaultFinderStackDelay();
            this.stackDelayUnit = config.getDefaultFinderStackDelayUnit();
            this.onComplete = onComplete;
        }

        private final Consumer<Scheduler> runnable = (sch) -> {
            //ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
            Collection<List<SyncBlockPosition>> collections = new ArrayList<>();
            List<SyncBlockPosition> current = new ArrayList<>();
            for (SyncBlockPosition syncBlockPosition : this.process) {
                current.add(syncBlockPosition);
                if (current.size() >= this.stackLimit) {
                    collections.add(current);
                    current = new ArrayList<>();
                }
            }
            collections.add(current);

            Scheduler scheduler = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDelay(stackDelay)
                    .setDelayUnit(stackDelayUnit)
                    .setRunner((sch1) -> {
                        if ((this.total.size() <= Ships6BlockFinder.this.limit) && (!this.ret.isEmpty())) {
                            this.process = this.ret;
                            this.ret = new ArrayList<>();

                            TranslateCore
                                    .getScheduleManager()
                                    .schedule()
                                    .setDelay(stackDelay)
                                    .setDelayUnit(stackDelayUnit)
                                    .setRunner(this.runnable)
                                    .setDisplayName("Ships 6 ASync Block Finder")
                                    .buildDelayed(ShipsPlugin.getPlugin())
                                    .run();
                        } else {
                            this.onComplete.complete(this.pss);
                        }
                    })
                    .setDisplayName("Ships 6 block finder")
                    .buildDelayed(ShipsPlugin.getPlugin());

            for (List<SyncBlockPosition> list : collections) {
                scheduler = TranslateCore
                        .getScheduleManager()
                        .schedule()
                        .setDelay(stackDelay)
                        .setDelayUnit(stackDelayUnit)
                        .setRunner((scheduler2) -> {
                            OvertimeSection section = new OvertimeSection(list, this.total);
                            section.runnable.accept(scheduler2);
                            section.ret.forEach(p -> {
                                OvertimeBlockFinderUpdate.BlockFindControl blockFind = this.update.onBlockFind(this.pss,
                                                                                                               p);
                                if (blockFind == OvertimeBlockFinderUpdate.BlockFindControl.IGNORE) {
                                    return;
                                }
                                this.pss.addPositionRelativeToWorld(p);
                                this.ret.add(p);
                                this.total.add(p);
                            });
                        })
                        .setToRunAfter(scheduler)
                        .setDisplayName("Ships 6 Block finder")
                        .buildDelayed(ShipsPlugin.getPlugin());
            }
            scheduler.run();
        };


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

    @Override
    public CompletableFuture<PositionableShipsStructure> getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                                                                    @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        CompletableFuture<PositionableShipsStructure> future = new CompletableFuture<>();
        var configuration = ShipsPlugin.getPlugin().getConfig();
        Overtime overtime = new Overtime(position.toSyncPosition(), runAfterFullSearch, configuration, future);
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDelay(configuration.getDefaultFinderStackDelay())
                .setDelayUnit(configuration.getDefaultFinderStackDelayUnit())
                .setRunner(overtime.runnable)
                .setDisplayName("Ships 6 block finder")
                .buildDelayed(ShipsPlugin.getPlugin())
                .run();
        return future;
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
