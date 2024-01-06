package org.ships.algorthum.movement;

import org.core.TranslateCore;
import org.core.config.ConfigurationNode;
import org.core.world.position.impl.BlockPosition;
import org.ships.config.node.DedicatedNode;
import org.ships.event.vessel.move.VesselMoveEvent;
import org.ships.movement.MovementContext;
import org.ships.movement.MovingBlock;
import org.ships.movement.MovingBlockSet;
import org.ships.movement.Result;
import org.ships.movement.instruction.actions.PostMovement;
import org.ships.vessel.common.flag.MovingFlag;
import org.ships.vessel.common.types.Vessel;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class Ships5Movement implements BasicMovement {

    @Override
    public Result move(Vessel vessel, MovementContext context) {
        List<MovingBlock> blocks = context.getMovingStructure().order(MovingBlockSet.ORDER_ON_PRIORITY);
        int waterLevel = -1;
        Optional<Integer> opWaterLevel = vessel.getWaterLevel();
        if (opWaterLevel.isPresent()) {
            waterLevel = opWaterLevel.get();
        }
        final int finalWaterLevel = waterLevel;
        blocks.forEach(m -> {
            BlockPosition after = m.getAfterPosition();
            if (finalWaterLevel >= after.getY()) {
                m.removeBeforePositionUnderWater();
            } else {
                m.removeBeforePositionOverAir();
            }
        });
        for (int index = blocks.size(); index > 0; index--) {
            MovingBlock m = blocks.get(index - 1);
            Stream.of(context.getMidMovementProcess()).forEach(mid -> mid.move(m));
            m.setMovingTo();
        }
        for (PostMovement movement : context.getPostMovementProcess()) {
            movement.postMove(vessel);
        }
        vessel.set(MovingFlag.class, null);
        VesselMoveEvent.Post eventPost = new VesselMoveEvent.Post(vessel, context, Result.DEFAULT_RESULT);
        TranslateCore.getPlatform().callEvent(eventPost);
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

    @Override
    public Collection<DedicatedNode<?, ?, ? extends ConfigurationNode.KnownParser<?, ?>>> getNodes() {
        return Collections.emptySet();
    }

    @Override
    public Optional<File> configurationFile() {
        return Optional.empty();
    }
}
