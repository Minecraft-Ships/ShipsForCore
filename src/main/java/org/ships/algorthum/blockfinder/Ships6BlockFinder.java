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
import org.core.world.position.impl.Position;
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
import java.util.function.Consumer;

public class Ships6BlockFinder implements BasicBlockFinder {

    private class Overtime {

        private class OvertimeSection {

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


        private Overtime(SyncBlockPosition position, OvertimeBlockFinderUpdate update, ConfigurationStream config) {
            this.pss = new AbstractPositionableShipsStructure(position);
            this.update = update;
            this.process.add(position);
            this.stackLimit = config.getInteger(STACK_LIMIT_NODE.getNode()).orElse(7);
            this.stackDelay = config.getInteger(STACK_DELAY.getNode()).orElse(1);
            this.stackDelayUnit = config.parse(STACK_DELAY_UNIT.getNode()).orElse(TimeUnit.MINECRAFT_TICKS);

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
                                    .build(ShipsPlugin.getPlugin())
                                    .run();
                        } else {
                            this.update.onShipsStructureUpdated(this.pss);
                        }
                    })
                    .setDisplayName("Ships 6 block finder")
                    .build(ShipsPlugin.getPlugin());

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
                        .build(ShipsPlugin.getPlugin());
            }
            scheduler.run();
        };


    }

    private final @NotNull RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> STACK_DELAY = RawDedicatedNode.integer(
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Advanced", "Movement", "Stack",
                                                            "Delay"), "Advanced.Block.Movement.Stack.Delay");
    private final ObjectDedicatedNode<TimeUnit, ConfigurationNode.KnownParser.SingleKnown<TimeUnit>> STACK_DELAY_UNIT = new ObjectDedicatedNode<>(
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_MINECRAFT_TIME_UNIT, "Advanced",
                                                            "Movement", "Stack", "DelayUnit"),
            "Advanced.Block.Movement.Stack.DelayUnit");

    private final @NotNull RawDedicatedNode<Integer, ConfigurationNode.KnownParser.SingleKnown<Integer>> STACK_LIMIT_NODE = RawDedicatedNode.integer(
            new ConfigurationNode.KnownParser.SingleKnown<>(Parser.STRING_TO_INTEGER, "Stack", "Limit"),
            "Advanced.Block.Movement.Stack.Limit");

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
    public void getConnectedBlocksOvertime(@NotNull BlockPosition position,
                                           @NotNull OvertimeBlockFinderUpdate runAfterFullSearch) {
        //ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        ConfigurationStream configuration = this
                .configuration()
                .orElseThrow(() -> new RuntimeException("Configuration is optional empty"));
        Overtime overtime = new Overtime(Position.toSync(position), runAfterFullSearch, configuration);
        TranslateCore
                .getScheduleManager()
                .schedule()
                .setDelay(configuration.getInteger(STACK_DELAY.getNode()).orElse(1))
                .setDelayUnit(configuration.parse(STACK_DELAY_UNIT.getNode()).orElse(TimeUnit.MINECRAFT_TICKS))
                .setRunner(overtime.runnable)
                .setDisplayName("Ships 6 block finder")
                .build(ShipsPlugin.getPlugin())
                .run();

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

    public PositionableShipsStructure getConnectedBlocks(BlockPosition position) {
        int count = 0;
        Direction[] directions = FourFacingDirection.withYDirections(FourFacingDirection.getFourFacingDirections());
        PositionableShipsStructure pss = new AbstractPositionableShipsStructure(Position.toSync(position));
        Collection<SyncBlockPosition> ret = new ArrayList<>();
        Collection<SyncBlockPosition> target = new ArrayList<>();
        Collection<SyncBlockPosition> process = new ArrayList<>();
        process.add(Position.toSync(position));
        while (count != this.limit) {
            if (process.isEmpty()) {
                ret.forEach(pss::addPositionRelativeToWorld);
                return pss;
            }
            for (SyncBlockPosition proc : process) {
                count++;
                for (Direction face : directions) {
                    SyncBlockPosition block = proc.getRelative(face);
                    if (ret.stream().noneMatch(b -> b.equals(block))) {
                        BlockInstruction bi = this.list.getBlockInstruction(block.getBlockType());
                        if (bi.getCollide() == CollideType.MATERIAL) {
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
        ret.forEach(pss::addPositionRelativeToWorld);
        return pss;
    }

    @Override
    public String getId() {
        return "ships:blockfinder_ships_six";
    }

    @Override
    public String getName() {
        return "Ships 6 R2 BlockFinder";
    }

    @Override
    public Collection<DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>>> getNodes() {
        return Arrays.asList(STACK_DELAY, STACK_DELAY_UNIT, STACK_LIMIT_NODE);
    }

    @Override
    public Optional<File> configurationFile() {
        return Optional.of(new File(TranslateCore.getPlatform().getPlatformConfigFolder(),
                                    "Ships/Configuration/BlockFinder/Ships Six." + TranslateCore
                                            .getPlatform()
                                            .getConfigFormat()
                                            .getMediaType()));
    }
}
