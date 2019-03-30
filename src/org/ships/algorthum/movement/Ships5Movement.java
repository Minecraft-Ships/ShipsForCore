package org.ships.algorthum.movement;

import org.core.entity.Entity;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ships5Movement implements BasicMovement {

    @Override
    public Result move(Vessel vessel, MovingBlockSet set, Map<Entity, MovingBlock> map) {
        List<MovingBlock> blocks = set.order(MovingBlockSet.ORDER_ON_PRIORITY);
        int waterLevel = -1;
        if(vessel instanceof WaterType){
            Optional<Integer> opWaterLevel = ((WaterType)vessel).getWaterLevel();
            if(opWaterLevel.isPresent()){
                waterLevel = opWaterLevel.get();
            }
        }
        final int finalWaterLevel = waterLevel;
        blocks.stream().forEach(m -> {
            if(finalWaterLevel > m.getAfterPosition().getY()){
                m.removeBeforePositionUnderWater();
            }else{
                m.removeBeforePositionOverAir();
            }
        });
        blocks.forEach(m -> m.setMovingTo());
        return Result.DEFAULT_RESULT;
    }

    @Override
    public String getId() {
        return "ships:movement_ships_five";
    }

    @Override
    public String getName() {
        return "Ships 5 Movement";
    }
}
