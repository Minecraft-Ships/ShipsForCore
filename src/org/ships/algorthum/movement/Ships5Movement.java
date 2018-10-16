package org.ships.algorthum.movement;

import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.assits.WaterType;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Optional;

public class Ships5Movement implements BasicMovement {

    @Override
    public Optional<FailedMovement> move(Vessel vessel, MovingBlockSet set) {
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
                m.moveUnderWater();
            }else{
                m.moveOverAir();
            }
        });
        return Optional.empty();
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
