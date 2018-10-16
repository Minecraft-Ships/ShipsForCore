package org.ships.algorthum.movement;

import org.core.CorePlugin;
import org.core.schedule.Scheduler;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Ships6Movement implements BasicMovement {

    private static class ProcessBlocks implements Runnable {

        List<MovingBlock> toProcess;
        int waterLevel;

        public ProcessBlocks(int level, List<MovingBlock> blocks){
            this.toProcess = blocks;
            this.waterLevel = level;
        }

        @Override
        public void run() {
            this.toProcess.stream().forEach(m -> {
                if(this.waterLevel > m.getAfterPosition().getY()){
                    m.moveUnderWater();
                }else{
                    m.moveOverAir();
                }
            });
        }
    }

    @Override
    public Optional<FailedMovement> move(Vessel vessel, MovingBlockSet set) {
        List<MovingBlock> blocks = set.order(MovingBlockSet.ORDER_ON_PRIORITY);
        List<List<MovingBlock>> blocksToProcess = new ArrayList<>();
        List<MovingBlock> currentlyAdding = new ArrayList<>();
        for(MovingBlock block : blocks){
            if(currentlyAdding.size() <= 100){
                blocksToProcess.add(currentlyAdding);
                currentlyAdding = new ArrayList<>();
            }
            currentlyAdding.add(block);
        }
        int waterLevel = -1;
        if(vessel instanceof WaterType){
            Optional<Integer> opWaterLevel = ((WaterType)vessel).getWaterLevel();
            if(opWaterLevel.isPresent()){
                waterLevel = opWaterLevel.get();
            }
        }
        Scheduler scheduler = null;
        for(int A = blocksToProcess.size(); A > 0; A--){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin.createSchedulerBuilder().setExecutor(new ProcessBlocks(waterLevel, blocks2)).setToRunAfter(scheduler).setDelay(1).setDelayUnit(TimeUnit.SECONDS).build(ShipsPlugin.getPlugin());
        }
        if(scheduler == null){
            return Optional.of(new AbstractFailedMovement(vessel, MovementResult.UNKNOWN));
        }
        scheduler.run();

        return Optional.empty();
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
