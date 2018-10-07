package org.ships.movement;

import org.core.world.position.BlockPosition;
import org.ships.movement.result.FailedMovement;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.Optional;

public class Movement {

    public static final RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
    public static final RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
    public static final TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

    protected Optional<FailedMovement> move(Vessel vessel, MovingBlockSet blocks) {
        return Optional.empty();
    }

    private static class RotateLeftAroundPosition extends Movement {

        public Optional<FailedMovement> move(Vessel vessel, BlockPosition rotateAround) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            return move(vessel, set);
        }

    }

    private static class RotateRightAroundPosition extends Movement {

        public Optional<FailedMovement> move(Vessel vessel, BlockPosition rotateAround) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            return move(vessel, set);
        }

    }

    private static class TeleportToPosition extends Movement {

        public Optional<FailedMovement> move(Vessel vessel, BlockPosition to){
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelitivePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            return move(vessel, set);
        }

    }

}
