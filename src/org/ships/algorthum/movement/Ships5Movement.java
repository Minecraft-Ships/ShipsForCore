package org.ships.algorthum.movement;

import org.core.entity.LiveEntity;
import org.core.world.boss.ServerBossBar;
import org.ships.movement.Movement;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public class Ships5Movement implements BasicMovement {

    @Override
    public Result move(Vessel vessel, MovingBlockSet set, Map<LiveEntity, MovingBlock> map, ServerBossBar bar, Movement.MidMovement midMovement, Movement.PostMovement... movements) {
        List<MovingBlock> blocks = set.order(MovingBlockSet.ORDER_ON_PRIORITY);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();
        if(opWaterLevel.isPresent()){
            waterLevel = opWaterLevel.get();
        }
        final int finalWaterLevel = waterLevel;
        blocks.forEach(m -> {
            if(finalWaterLevel >= m.getAfterPosition().getY()){
                m.removeBeforePositionUnderWater();
            }else{
                m.removeBeforePositionOverAir();
            }
        });
        for(int A = blocks.size(); A > 0; A--) {
            MovingBlock m = blocks.get(A-1);
            midMovement.move(m);
            m.setMovingTo();
        }
        for(Movement.PostMovement movement : movements){
            movement.postMove(vessel);
        }
        vessel.set(MovingFlag.class, null);
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
