package org.ships.movement;

import org.core.entity.Entity;
import org.core.exceptions.DirectionNotSupported;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.Direction;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.core.world.position.block.details.BlockDetails;
import org.core.world.position.block.details.data.DirectionalData;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.exceptions.MoveException;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.common.types.Vessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;

public class Movement {

    public interface MidMovement {

        RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
        RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
        AddToPosition ADD_TO_POSITION = new AddToPosition();
        TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

        void move(MovingBlock moving);

    }

    public interface PostMovement {

        void postMove(Vessel vessel);

    }

    protected void move(ShipsVessel vessel, MovingBlockSet blocks, BasicMovement movement, MidMovement mid, PostMovement... postMovement) throws MoveException {
        Set<Entity> entities = vessel.getEntities();
        Map<Entity, MovingBlock> entityBlock = new HashMap<>();
        entities.forEach(e -> e.setGravity(false));
        entities.forEach(e -> entityBlock.put(e, blocks.getBefore(e.getPosition().toBlockPosition().getRelative(FourFacingDirection.DOWN)).get()));
        Optional<MovingBlock> opLicence = blocks.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
        if (!opLicence.isPresent()) {
            entities.forEach(e -> e.setGravity(true));
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.NO_LICENCE_FOUND, null));
        }
        if (vessel instanceof AbstractShipsVessel) {
            try {
                ((AbstractShipsVessel) vessel).meetsRequirement(blocks);
            }catch (MoveException e) {
                entities.forEach(entity -> entity.setGravity(true));
                throw e;
            }
        }
        Set<BlockPosition> collided = new HashSet<>();
        blocks.forEach(mb -> {
            if(blocks.stream().anyMatch(mb1 -> mb.getAfterPosition().equals(mb1.getBeforePosition()))){
                return;
            }
            for(BlockType type : vessel.getType().getIgnoredTypes()){
                if(type.equals(mb.getAfterPosition().getBlockType())){
                    return;
                }
            }
            BlockInstruction bi = vessel.getBlockList().getBlockInstruction(mb.getAfterPosition().getBlockType());
            if (!bi.getCollideType().equals(BlockInstruction.CollideType.IGNORE)) {
                collided.add(mb.getAfterPosition());
            }
        });
        if (!collided.isEmpty()) {
            entities.forEach(e -> e.setGravity(true));
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.COLLIDE_DETECTED, collided));
        }
        try {
            Result result = movement.move(vessel, blocks, entityBlock, mid, postMovement);
            result.run(vessel, blocks, entityBlock);
        }catch (MoveException e) {
            entities.forEach(entity -> entity.setGravity(true));
            throw e;
        }
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) throws MoveException {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            move(vessel, set, movement, (mb) -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if(!(opDirectional.isPresent())){
                    return;
                }
                DirectionalData directionalData = opDirectional.get();
                Direction direction = directionalData.getDirection().getRightAngleLeft();
                try {
                    directionalData.setDirection(direction);
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            });
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            move(vessel, set, movement, (mb) -> {
                BlockDetails blockDetails = mb.getStoredBlockData();
                Optional<DirectionalData> opDirectional = blockDetails.getDirectionalData();
                if(!(opDirectional.isPresent())){
                    return;
                }
                DirectionalData directionalData = opDirectional.get();
                Direction direction = directionalData.getDirection().getRightAngleRight();
                try {
                    directionalData.setDirection(direction);
                } catch (DirectionNotSupported directionNotSupported) {
                    directionNotSupported.printStackTrace();
                }
            });

        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition to, BasicMovement movement) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, set, movement, (mb) -> {

            });
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition(){
        }

        public void move(ShipsVessel vessel, int x, int y, int z, BasicMovement movement) throws MoveException{
            move(vessel, new Vector3Int(x, y, z), movement);
        }

        public void move(ShipsVessel vessel, Vector3Int addTo, BasicMovement movement) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, set, movement, (mb) -> {

            });
        }

    }

}
