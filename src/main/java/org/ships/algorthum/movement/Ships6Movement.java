package org.ships.algorthum.movement;

import org.core.TranslateCore;
import org.core.adventureText.AText;
import org.core.schedule.Scheduler;
import org.ships.config.configuration.ShipsConfig;
import org.ships.config.messages.AdventureMessageConfig;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class Ships6Movement implements BasicMovement {

    @Override
    public Result move(Vessel vessel, MovementContext context) throws MoveException {
        context.getBossBar().ifPresent(b -> b.setValue(0));
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        List<MovingBlock> blocks = context.getMovingStructure().order(MovingBlockSet.ORDER_ON_PRIORITY);
        List<List<MovingBlock>> blocksToProcess = new ArrayList<>();
        List<MovingBlock> currentlyAdding = new ArrayList<>();
        for (int A = 0; A < blocks.size(); A++) {
            final int B = A;
            context.getBossBar().ifPresent(bar -> bar.setValue(B, blocks.size() * 3));
            MovingBlock block = blocks.get(A);
            if (currentlyAdding.size() >= config.getDefaultMovementStackLimit()) {
                blocksToProcess.add(currentlyAdding);
                currentlyAdding = new ArrayList<>();
            }
            currentlyAdding.add(block);
        }
        blocksToProcess.add(currentlyAdding);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();

        if (opWaterLevel.isPresent()) {
            waterLevel = opWaterLevel.get();
        }
        final int total = blocks.size();
        Scheduler scheduler = TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Post Movement")
                .setRunner((sch) -> {
                    context.getBossBar().ifPresent(bar -> bar.setTitle(AText.ofPlain("Processing: Post movement")));
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
                .build(ShipsPlugin.getPlugin());
        for (int A = 0; A < blocksToProcess.size(); A++) {
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDisplayName("Set Block")
                    .setRunner(new SetBlocks(A, total, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(config.getDefaultMovementStackDelay())
                    .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                    .build(ShipsPlugin.getPlugin());
        }
        scheduler = TranslateCore
                .getScheduleManager()
                .schedule()
                .setDisplayName("Teleport entities")
                .setRunner((sch) -> Result.Run.COMMON_TELEPORT_ENTITIES.run(vessel, context))
                .setToRunAfter(scheduler)
                .setDelay(config.getDefaultMovementStackDelay())
                .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                .build(ShipsPlugin.getPlugin());
        for (int A = 0; A < blocksToProcess.size(); A++) {
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = TranslateCore
                    .getScheduleManager()
                    .schedule()
                    .setDisplayName("Remove Blocxds67ytyk")
                    .setRunner(new RemoveBlocks(waterLevel, A, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(config.getDefaultMovementStackDelay())
                    .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                    .build(ShipsPlugin.getPlugin());
        }
        if (scheduler == null) {
            throw new MoveException(context, AdventureMessageConfig.ERROR_FAILED_IN_MOVEMENT, vessel);
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

    private static class RemoveBlocks implements Consumer<Scheduler> {

        private final List<? extends MovingBlock> toProcess;
        private final int waterLevel;
        private final int attempt;
        private final MovementContext context;

        private RemoveBlocks(int level, int attempt, MovementContext context, List<? extends MovingBlock> blocks) {
            this.toProcess = blocks;
            this.waterLevel = level;
            this.attempt = attempt;
            this.context = context;
        }

        @Override
        public void accept(Scheduler scheduler) {
            for (int A = 0; A < this.toProcess.size(); A++) {
                final int B = A;
                this.context.getBossBar().ifPresent(bar -> {
                    try {
                        bar.setValue((this.attempt - 1) + B, this.context.getMovingStructure().size());
                    } catch (IllegalArgumentException ignore) {
                    }
                });
                MovingBlock m = this.toProcess.get(A);
                if (this.waterLevel >= m.getBeforePosition().getY()) {
                    m.removeBeforePositionUnderWater();
                } else {
                    m.removeBeforePositionOverAir();
                }
            }
        }
    }

    private static class SetBlocks implements Consumer<Scheduler> {

        private final List<? extends MovingBlock> toProcess;
        private final int attempt;
        private final MovementContext context;
        private final int totalBlocks;


        private SetBlocks(int attempt, int totalBlocks, MovementContext context, List<? extends MovingBlock> blocks) {
            this.toProcess = blocks;
            this.context = context;
            this.attempt = attempt;
            this.totalBlocks = totalBlocks;
        }

        @Override
        public void accept(Scheduler scheduler) {
            for (int A = 0; A < this.toProcess.size(); A++) {
                MovingBlock m = this.toProcess.get(A);
                final int B = A;
                this.context.getBossBar().ifPresent(bar -> {
                    try {
                        bar.setValue(this.attempt * B, (this.totalBlocks * 2) + 1);
                    } catch (IllegalArgumentException ignore) {
                    }
                });
                Stream.of(this.context.getMidMovementProcess()).forEach(mid -> mid.move(m));
                m.setMovingTo();
            }
        }
    }
}
