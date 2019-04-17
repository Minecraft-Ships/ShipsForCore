package org.ships.algorthum.movement;

import org.core.CorePlugin;
import org.core.entity.Entity;
import org.core.schedule.Scheduler;
import org.ships.exceptions.MoveException;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class Ships6Movement implements BasicMovement {

    private static class ProcessBlocks implements Runnable {

        private List<MovingBlock> toProcess;
        private int waterLevel;
        private Movement.MidMovement midMovement;

        public ProcessBlocks(int level, List<MovingBlock> blocks, Movement.MidMovement midMovement){
            this.toProcess = blocks;
            this.waterLevel = level;
            this.midMovement = midMovement;
        }

        @Override
        public void run() {
            this.toProcess.forEach(m -> {
                if(this.waterLevel > m.getAfterPosition().getY()){
                    m.removeBeforePositionUnderWater();
                }else{
                    m.removeBeforePositionOverAir();
                }
            });
            this.toProcess.forEach(m -> {
                midMovement.move(m);
                m.setMovingTo();
            });
        }
    }

    @Override
    public Result move(Vessel vessel, MovingBlockSet set, Map<Entity, MovingBlock> entity, Movement.MidMovement midMovement) throws MoveException {
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
        Scheduler scheduler = CorePlugin.createSchedulerBuilder().setExecutor(() -> Result.DEFAULT_RESULT.run((ShipsVessel)vessel, set, entity)).build(ShipsPlugin.getPlugin());
        for(int A = blocksToProcess.size(); A > 0; A--){
            List<MovingBlock> blocks2 = blocksToProcess.get(A);
            scheduler = CorePlugin.createSchedulerBuilder().setExecutor(new ProcessBlocks(waterLevel, blocks2, midMovement)).setToRunAfter(scheduler).setDelay(1).setDelayUnit(TimeUnit.SECONDS).build(ShipsPlugin.getPlugin());
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
