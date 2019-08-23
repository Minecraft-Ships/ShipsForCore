package org.ships.algorthum.movement;

import org.core.CorePlugin;
import org.core.entity.LiveEntity;
import org.core.schedule.Scheduler;
import org.core.world.boss.ServerBossBar;
import org.ships.config.configuration.ShipsConfig;
import org.ships.exceptions.MoveException;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ships6Movement implements BasicMovement {

    private static class RemoveBlocks implements Runnable {

        private List<MovingBlock> toProcess;
        private int totalBlocks;
        private int waterLevel;
        private int attempt;
        private ServerBossBar bar;

        public RemoveBlocks(int level, int attempt, int totalBlocks, ServerBossBar bar, List<MovingBlock> blocks){
            this.toProcess = blocks;
            this.waterLevel = level;
            this.bar = bar;
            this.attempt = attempt;
            this.totalBlocks = totalBlocks;
        }

        @Override
        public void run() {
            for(int A = 0; A < this.toProcess.size(); A++){
                if(bar != null){
                    try {
                        bar.setValue((attempt-1) + A, totalBlocks);
                    }catch (IllegalArgumentException e){
                    }
                }
                MovingBlock m = this.toProcess.get(A);
                if(this.waterLevel >= m.getBeforePosition().getY()){
                    m.removeBeforePositionUnderWater();
                }else{
                    m.removeBeforePositionOverAir();
                }
            }
        }
    }

    private static class SetBlocks implements Runnable {

        private List<MovingBlock> toProcess;
        private Movement.MidMovement midMovement;
        private int attempt;
        private ServerBossBar bar;
        private int totalBlocks;


        public SetBlocks(int attempt, int totalBlocks, ServerBossBar bar, Movement.MidMovement midMovement, List<MovingBlock> blocks){
            this.toProcess = blocks;
            this.midMovement = midMovement;
            this.bar = bar;
            this.attempt = attempt;
            this.totalBlocks = totalBlocks;
        }

        @Override
        public void run() {
            for(int A = 0; A < this.toProcess.size(); A++){
                MovingBlock m = this.toProcess.get(A);
                if(bar != null){
                    try{
                        bar.setValue(attempt*A, (totalBlocks*2)+1);
                    }catch (IllegalArgumentException e){
                    }
                }
                midMovement.move(m);
                m.setMovingTo();
            }
        }
    }

    @Override
    public Result move(Vessel vessel, MovingBlockSet set, Map<LiveEntity, MovingBlock> entity, ServerBossBar bar, Movement.MidMovement midMovement, Movement.PostMovement... movements) throws MoveException {
        if(bar != null){
            bar.setValue(0);
        }
        ShipsConfig config = ShipsPlugin.getPlugin().getConfig();
        List<MovingBlock> blocks = set.order(MovingBlockSet.ORDER_ON_PRIORITY);
        List<List<MovingBlock>> blocksToProcess = new ArrayList<>();
        //List<List<MovingBlock>> blocksToRemove = new ArrayList<>();
        List<MovingBlock> currentlyAdding = new ArrayList<>();
        //List<MovingBlock> currentlyRemoving = new ArrayList<>();
        for(int A = 0; A < blocks.size(); A++){
            if(bar != null){
                bar.setValue(A, blocks.size() * 3);
            }
            MovingBlock block = blocks.get(A);
            if(currentlyAdding.size() >= config.getDefaultMovementStackLimit()){
                blocksToProcess.add(currentlyAdding);
                currentlyAdding = new ArrayList<>();
            }
            currentlyAdding.add(block);
        }
        blocksToProcess.add(currentlyAdding);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();
        if(opWaterLevel.isPresent()){
            waterLevel = opWaterLevel.get();
        }
        final int total = blocks.size();
        Scheduler scheduler = CorePlugin.createSchedulerBuilder().setExecutor(() -> {
            if(bar != null){
                bar.setMessage(CorePlugin.buildText("Processing: Post movement"));
            }
            for(Movement.PostMovement movement : movements){
                movement.postMove(vessel);
            }
            Result.DEFAULT_RESULT.run(vessel, set, bar, entity);
            vessel.set(MovingFlag.class, null);
        }).build(ShipsPlugin.getPlugin());
        for(int A = 0; A < blocksToProcess.size(); A++){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin
                    .createSchedulerBuilder()
                    .setExecutor(new SetBlocks(A, total, bar, midMovement, blocks2))
                    .setToRunAfter(scheduler)
                    .setDelay(config.getDefaultMovementStackDelay())
                    .setDelayUnit(config.getDefaultMovementStackDelayUnit())
                    .build(ShipsPlugin.getPlugin());
        }
        for(int A = 0; A < blocksToProcess.size(); A++){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin
                    .createSchedulerBuilder()
                    .setExecutor(new RemoveBlocks(waterLevel, A, total, bar, blocks2))
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
