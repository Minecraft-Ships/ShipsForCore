package org.ships.algorthum.movement;

import org.core.CorePlugin;
import org.core.schedule.Scheduler;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.*;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Ships6Movement implements BasicMovement {

    private static class RemoveBlocks implements Runnable {

        private List<MovingBlock> toProcess;
        private int waterLevel;
        private int attempt;
        private MovementContext context;

        public RemoveBlocks(int level, int attempt, MovementContext context, List<MovingBlock> blocks){
            this.toProcess = blocks;
            this.waterLevel = level;
            this.attempt = attempt;
            this.context = context;
        }

        @Override
        public void run() {
            for(int A = 0; A < this.toProcess.size(); A++){
                final int B = A;
                context.getBar().ifPresent(bar -> {
                    try {
                        bar.setValue((attempt-1) + B, context.getMovingStructure().size());
                    }catch (IllegalArgumentException e){
                    }
                });
                MovingBlock m = this.toProcess.get(A);
                if(m.getBeforePosition().isPresent()) {
                    if (this.waterLevel >= m.getBeforePosition().get().getY()) {
                        m.removeBeforePositionUnderWater();
                    } else {
                        m.removeBeforePositionOverAir();
                    }
                }
            }
        }
    }

    private static class SetBlocks implements Runnable {

        private List<MovingBlock> toProcess;
        private int attempt;
        private MovementContext context;
        private int totalBlocks;


        public SetBlocks(int attempt, int totalBlocks, MovementContext context, List<MovingBlock> blocks){
            this.toProcess = blocks;
            this.context = context;
            this.attempt = attempt;
            this.totalBlocks = totalBlocks;
        }

        @Override
        public void run() {
            for(int A = 0; A < this.toProcess.size(); A++){
                MovingBlock m = this.toProcess.get(A);
                final int B = A;
                context.getBar().ifPresent(bar -> {
                    try{
                        bar.setValue(attempt*B, (totalBlocks*2)+1);
                    }catch (IllegalArgumentException e){
                    }
                });
                Stream.of(context.getMidMovementProcess()).forEach(mid -> mid.move(m));
                m.setMovingTo();
            }
        }
    }

    @Override
    public Result move(Vessel vessel, MovementContext context) throws MoveException {
        context.getBar().ifPresent(b -> b.setValue(0));
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        context.getMovingStructure().applyMovingBlocks();
        List<MovingBlock> blocks = context.getMovingStructure().order(MovingBlockSet.ORDER_ON_PRIORITY);
        List<List<MovingBlock>> blocksToProcess = new ArrayList<>();
        List<List<MovingBlock>> blocksToRemove = new ArrayList<>();
        List<MovingBlock> currentlyAdding = new ArrayList<>();
        List<MovingBlock> currentlyRemoving = new ArrayList<>();
        context.getBar().ifPresent(b -> b.setMessage(CorePlugin.buildText("Creating stacks")));
        for(int A = 0; A < blocks.size(); A++){
            final int B = A;
            context.getBar().ifPresent(bar -> bar.setValue(B, blocks.size()));
            MovingBlock block = blocks.get(A);
            if(currentlyAdding.size() >= config.getDefaultMovementStackLimit()){
                blocksToProcess.add(currentlyAdding);
                currentlyAdding = new ArrayList<>();
            }
            if(currentlyRemoving.size() >= config.getDefaultMovementStackLimit()){
                blocksToRemove.add(currentlyRemoving);
                currentlyRemoving = new ArrayList<>();
            }
            if(block.getAfterPosition().isPresent()) {
                currentlyAdding.add(block);
            }
            if(block.getBeforePosition().isPresent()){
                currentlyRemoving.add(block);
            }
        }
        blocksToProcess.add(currentlyAdding);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();
        if(opWaterLevel.isPresent()){
            waterLevel = opWaterLevel.get();
        }
        final int total = blocks.size();
        Scheduler scheduler = CorePlugin.createSchedulerBuilder().setExecutor(() -> {
            context.getBar().ifPresent(bar -> bar.setMessage(CorePlugin.buildText("Processing: Post movement")));
            for(Movement.PostMovement movement : context.getPostMovementProcess()){
                movement.postMove(vessel);
            }
            Result.DEFAULT_RESULT.run(vessel, context);
            vessel.set(MovingFlag.class, null);
        }).build(ShipsPlugin.getPlugin());
        for(int A = 0; A < blocksToProcess.size(); A++){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin
                    .createSchedulerBuilder()
                    .setExecutor(new SetBlocks(A, total, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(config.getDefaultMovementStackDelay())
                    .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                    .build(ShipsPlugin.getPlugin());
        }
        for(int A = 0; A < blocksToProcess.size(); A++){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin
                    .createSchedulerBuilder()
                    .setExecutor(new RemoveBlocks(waterLevel, A, context, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(config.getDefaultMovementStackDelay())
                    .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                    .build(ShipsPlugin.getPlugin());
        }
        if(scheduler == null){
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.UNKNOWN, null));
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
