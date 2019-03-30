package org.ships.movement;

import org.core.entity.Entity;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.exceptions.MoveException;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.*;

public class Movement{

    public static final RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
    public static final RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
    public static final AddToPosition ADD_TO_POSITION = new AddToPosition();
    public static final TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

    protected void move(ShipsVessel vessel, MovingBlockSet blocks, BasicMovement movement) throws MoveException {
        Set<Entity> entities = vessel.getEntities();
        Map<Entity, MovingBlock> entityBlock = new HashMap<>();
        entities.stream().forEach(e -> e.setGravity(false));
        entities.stream().forEach(e -> entityBlock.put(e, blocks.getBefore(e.getPosition().toBlockPosition().getRelative(FourFacingDirection.DOWN)).get()));
        Optional<MovingBlock> opLicence = blocks.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
        if (!opLicence.isPresent()) {
            entities.stream().forEach(e -> e.setGravity(true));
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.NO_LICENCE_FOUND, null));
        }
        if (vessel instanceof AbstractShipsVessel) {
            try {
                ((AbstractShipsVessel) vessel).meetsRequirement(blocks);
            }catch (MoveException e) {
                entities.stream().forEach(entity -> entity.setGravity(true));
                throw e;
            }
        }
        Set<BlockPosition> collided = new HashSet<>();
        blocks.stream().forEach(mb -> {
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
                return;
            }
            return;
        });
        if (!collided.isEmpty()) {
            entities.stream().forEach(e -> e.setGravity(true));
            throw new MoveException(new AbstractFailedMovement(vessel, MovementResult.COLLIDE_DETECTED, collided));
        }
        try {
            Result result = movement.move(vessel, blocks, entityBlock);
            result.run(vessel, blocks, entityBlock);
        }catch (MoveException e) {
            entities.stream().forEach(entity -> entity.setGravity(true));
            throw e;
        }
        /*entityBlock.entrySet().stream().forEach(e ->{
            Entity entity = e.getKey();
            Vector3<Double> position = entity.getPosition().getPosition().minus(e.getValue().getBeforePosition().toExactPosition().getPosition());
            Vector3<Double> position2 = e.getValue().getAfterPosition().toExactPosition().getPosition();
            position = position2.add(position);
            entity.setPosition(position);
        });
        entities.stream().forEach(e -> e.setGravity(true));
        vessel.getStructure().setPosition(opLicence.get().getAfterPosition());
        vessel.save();*/
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) throws MoveException {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            move(vessel, set, movement);
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            move(vessel, set, movement);
        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public void move(ShipsVessel vessel, BlockPosition to, BasicMovement movement) throws MoveException{
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, set, movement);
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
            pss.getRelativePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            move(vessel, set, movement);
        }

    }

}
