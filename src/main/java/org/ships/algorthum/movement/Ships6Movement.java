package org.ships.algorthum.movement;

import net.kyori.adventure.text.Component;
import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.config.ConfigurationStream;
import org.core.config.parser.Parser;
import org.core.schedule.Scheduler;
import org.core.schedule.unit.TimeUnit;
import org.core.world.position.block.details.data.keyed.TileEntityKeyedData;
import org.jetbrains.annotations.NotNull;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
import org.ships.config.messages.Messages;
import org.ships.config.node.DedicatedNode;
import org.ships.config.node.ObjectDedicatedNode;
import org.ships.config.node.RawDedicatedNode;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.exceptions.move.MoveException;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.movement.instruction.actions.PostMovement;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Ships6Movement implements BasicMovement {

    private static final class RemoveBlocks implements Consumer<Scheduler> {

        private final List<? extends MovingBlock> toProcess;
        private final Integer waterLevel;
        private final int current;
        private final int totalBlocks;
        private final MovementContext context;

        private RemoveBlocks(Integer level,
                             int current,
                             int totalBlocks,
                             MovementContext context,
                             List<? extends MovingBlock> blocks) {
            this.toProcess = blocks;
            this.waterLevel = level;
            this.current = current;
            this.totalBlocks = totalBlocks;
            this.context = context;
        }

        @Override
        public void accept(Scheduler scheduler) {
            for (int index = 0; index < this.toProcess.size(); index++) {
                final int finalIndex = index;
                this.context.getAdventureBossBar().ifPresent(bar -> {
                    int blocksFound = this.current + finalIndex;
                    int totalBlocks = Math.max(this.totalBlocks, blocksFound);

                    float progress = (float) blocksFound / totalBlocks;
                    bar.progress(progress);
                    progress = Math.max(progress, 0);
                    progress = Math.min(progress, 1);
                    bar.name(Component.text("Removing: " + blocksFound + " / " + totalBlocks));

                });
                MovingBlock m = this.toProcess.get(index);
                if (this.waterLevel != null && this.waterLevel >= m.getBeforePosition().getY()) {
                    m.removeBeforePositionUnderWater();
                } else {
                    m.removeBeforePositionOverAir();
                }
            }
        }
    }

    private static final class SetBlocks implements Consumer<Scheduler> {

        private final List<? extends MovingBlock> toProcess;
        private final MovementContext context;
        private final int current;
        private final int totalBlocks;


        private SetBlocks(int current, int totalBlocks, MovementContext context, List<? extends MovingBlock> blocks) {
            this.toProcess = blocks;
            this.context = context;
            this.current = current;
            this.totalBlocks = totalBlocks;
        }

        @Override
        public void accept(Scheduler scheduler) {
            for (int index = 0; index < this.toProcess.size(); index++) {
                MovingBlock m = this.toProcess.get(index);
                final int finalIndex = index;
                this.context.getAdventureBossBar().ifPresent(bar -> {
                    int currentBlock = this.current + finalIndex;
                    float progress = (float) currentBlock / this.totalBlocks;
                    progress = Math.max(progress, 0);
                    progress = Math.min(progress, 1);
                    bar.progress(progress);
                    bar.name(Component.text("Placing: " + currentBlock + " / " + this.totalBlocks));
                });
                Stream.of(this.context.getMidMovementProcess()).forEach(mid -> mid.move(m));
                m.setMovingTo();
            }
        }
    }

    @Override
    public Result move(Vessel vessel, MovementContext context) throws MoveException {
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        int stackLimit = config.getDefaultMovementStackLimit();
        TimeUnit stackDelayUnit = config.getDefaultMovementStackDelayUnit();
        int stackDelay = config.getDefaultMovementStackDelay();

        context.getAdventureBossBar().ifPresent(b -> b.progress(0));
        List<MovingBlock> blocks = context.getMovingStructure().order(MovingBlockSet.ORDER_ON_PRIORITY);
        List<List<MovingBlock>> blocksToProcess = new LinkedList<>();
        List<MovingBlock> currentlyAdding = new LinkedList<>();
        context.getAdventureBossBar().ifPresent(bar -> bar.name(Component.text("Movement: optimising ")));
        final int total = blocks.size();
        for (int index = 0; index < blocks.size(); index++) {
            float progress = index / (float) total;
            context.getAdventureBossBar().ifPresent(bar -> bar.progress(progress));
            MovingBlock block = blocks.get(index);
            if (currentlyAdding.size() >= stackLimit) {
                blocksToProcess.add(currentlyAdding);
                currentlyAdding = new ArrayList<>();
            }
            currentlyAdding.add(block);
        }
        blocksToProcess.add(currentlyAdding);
        Integer waterLevel = vessel.getWaterLevel().orElse(null);
        Scheduler scheduler = TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Post Movement")
                .setRunner((sch) -> {
                    context
                            .getAdventureBossBar()
                            .ifPresent(bar -> bar.name(Component.text("Processing: Post movement")));
                    for (PostMovement movement : context.getPostMovementProcess()) {
                        movement.postMove(vessel);
                    }
                    VesselMoveEvent.Post eventPost = new VesselMoveEvent.Post(vessel, context, Result.DEFAULT_RESULT);
                    TranslateCore.getPlatform().callEvent(eventPost);
                    Result result = new Result(Result.DEFAULT_RESULT);
                    result.remove(Result.Run.COMMON_TELEPORT_ENTITIES);
                    result.run(vessel, context);
                    vessel.set(MovingFlag.class, null);
                })
                .buildDelayed(ShipsPlugin.getPlugin());
        int placedBlocks = 0;
        for (int index = 0; index < blocksToProcess.size(); index++) {
            List<MovingBlock> blocks2 = blocksToProcess.get(index);
            scheduler = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDisplayName("Set Block")
                    .setRunner(new SetBlocks(placedBlocks + index, total, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(stackDelay)
                    .setDelayUnit(stackDelayUnit)
                    .buildDelayed(ShipsPlugin.getPlugin());
            placedBlocks = placedBlocks + blocks2.size();
        }
        placedBlocks = 0;
        scheduler = TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Teleport entities")
                .setRunner((sch) -> Result.Run.COMMON_TELEPORT_ENTITIES.run(vessel, context))
                .setToRunAfter(scheduler)
                .setDelay(stackDelay)
                .setDelayUnit(stackDelayUnit)
                .buildDelayed(ShipsPlugin.getPlugin());
        for (int index = 0; index < blocksToProcess.size(); index++) {
            List<MovingBlock> blocks2 = blocksToProcess.get(index);

            scheduler = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDisplayName("Remove Blocks")
                    .setRunner(new RemoveBlocks(waterLevel, placedBlocks + index, total, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(stackDelay)
                    .setDelayUnit(stackDelayUnit)
                    .buildDelayed(ShipsPlugin.getPlugin());
            placedBlocks = placedBlocks + blocks2.size();
        }
        if (scheduler == null) {
            throw new MoveException(context, Messages.ERROR_FAILED_IN_MOVEMENT, vessel);
        }
        scheduler.run();

        return new Result();
    }

    @Override
    public String getId() {
        return "ships:movement_ships_six";
    }

    @Override
    public String getName() {
        return "Ships 6 Movement";
    }
}
