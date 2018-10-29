package org.ships.movement;

import org.core.CorePlugin;
import org.core.entity.Entity;
import org.core.vector.types.Vector3Int;
import org.core.world.direction.FourFacingDirection;
import org.core.world.position.BlockPosition;
import org.core.world.position.block.BlockType;
import org.ships.algorthum.movement.BasicMovement;
import org.ships.config.blocks.BlockInstruction;
import org.ships.movement.result.AbstractFailedMovement;
import org.ships.movement.result.FailedMovement;
import org.ships.movement.result.MovementResult;
import org.ships.plugin.ShipsPlugin;
import org.ships.vessel.common.types.AbstractShipsVessel;
import org.ships.vessel.common.types.ShipsVessel;
import org.ships.vessel.sign.LicenceSign;
import org.ships.vessel.structure.PositionableShipsStructure;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public class Movement{

    public static final RotateLeftAroundPosition ROTATE_LEFT_AROUND_POSITION = new RotateLeftAroundPosition();
    public static final RotateRightAroundPosition ROTATE_RIGHT_AROUND_POSITION = new RotateRightAroundPosition();
    public static final AddToPosition ADD_TO_POSITION = new AddToPosition();
    public static final TeleportToPosition TELEPORT_TO_POSITION = new TeleportToPosition();

    protected Optional<FailedMovement> move(ShipsVessel vessel, MovingBlockSet blocks, BasicMovement movement) {
        Set<Entity> entities = vessel.getEntities();
        Map<Entity, MovingBlock> entityBlock = new HashMap<>();
        entities.stream().forEach(e -> e.setGravity(true));
        entities.stream().forEach(e -> entityBlock.put(e, blocks.getBefore(e.getPosition().toBlockPosition().getRelative(FourFacingDirection.DOWN)).get()));
        System.out.println("Move: Size: " + blocks.size());
        Optional<MovingBlock> opLicence = blocks.get(ShipsPlugin.getPlugin().get(LicenceSign.class).get());
        if (!opLicence.isPresent()) {
            System.out.println("No Licence sign");
            return Optional.of(new AbstractFailedMovement(vessel, MovementResult.NO_LICENCE_FOUND));
        }
        if (vessel instanceof AbstractShipsVessel) {
            System.out.println("Vessel is AbstractShipsVessel");
            Optional<FailedMovement> opRequirements = ((AbstractShipsVessel) vessel).meetsRequirement(blocks);
            if (opRequirements.isPresent()) {
                System.out.println("failed to meet requirements");
                return opRequirements;
            }
        }
        System.out.println("Move stream");
        if (blocks.stream().anyMatch(mb -> {
            if(blocks.stream().anyMatch(mb1 -> {
                boolean check = mb.getAfterPosition().equals(mb1.getBeforePosition());
                System.out.println("X: " + mb.getAfterPosition().getX() + "," + mb.getAfterPosition().getY() + "," + mb.getAfterPosition().getZ() + " - " + mb1.getBeforePosition().getX() + "," + mb1.getBeforePosition().getY() + "," + mb1.getBeforePosition().getZ() + " - " + check);
                return check;
            })){
                return false;
            }
            System.out.println("Ignore Types: " + CorePlugin.toString(", ", bt -> bt.getId(), vessel.getType().getIgnoredTypes()));
            for(BlockType type : vessel.getType().getIgnoredTypes()){
                if(type.equals(mb.getAfterPosition().getBlockType())){
                    return false;
                }
            }
            BlockInstruction bi = vessel.getBlockList().getBlockInstruction(mb.getAfterPosition().getBlockType());
            if (!bi.getCollideType().equals(BlockInstruction.CollideType.IGNORE)) {
                System.out.println("\t- FoundIssue: " + bi.getType().getId() + ": " + bi.getCollideType().name() + " at " + mb.getAfterPosition().getX() + "," + mb.getAfterPosition().getY() + "," + mb.getAfterPosition().getZ());
                return true;
            }
            return false;
        })) {
            System.out.println("collide detected");
            return Optional.of(new AbstractFailedMovement(vessel, MovementResult.COLLIDE_DETECTED));
        }
        Optional<FailedMovement> opMovement = movement.move(vessel, blocks);
        if (opMovement.isPresent()) {
            System.out.println("Movement.move failed");
            return opMovement;
        }
        System.out.println("reposising entities");
        entityBlock.entrySet().stream().forEach(e -> e.getKey().setPosition(e.getValue().getAfterPosition()));
        System.out.println("resetting gravity");
        entities.stream().forEach(e -> e.setGravity(false));
        return Optional.empty();
    }

    public static class RotateLeftAroundPosition extends Movement {

        private RotateLeftAroundPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateLeft(rotateAround);
                set.add(block);
            });
            return move(vessel, set, movement);
        }

    }

    public static class RotateRightAroundPosition extends Movement {

        private RotateRightAroundPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition rotateAround, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            vessel.getStructure().getPositions().stream().forEach(s -> {
                MovingBlock block = new SetMovingBlock(s, s).rotateRight(rotateAround);
                set.add(block);
            });
            return move(vessel, set, movement);
        }

    }

    public static class TeleportToPosition extends Movement {

        private TeleportToPosition(){

        }

        public Optional<FailedMovement> move(ShipsVessel vessel, BlockPosition to, BasicMovement movement) {
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = to.getRelative(f);
                set.add(new SetMovingBlock(vp, vp2));
            });
            return move(vessel, set, movement);
        }

    }

    public static class AddToPosition extends Movement {

        private AddToPosition(){
        }

        public Optional<FailedMovement> move(ShipsVessel vessel, int x, int y, int z, BasicMovement movement){
            return move(vessel, new Vector3Int(x, y, z), movement);
        }

        public Optional<FailedMovement> move(ShipsVessel vessel, Vector3Int addTo, BasicMovement movement){
            MovingBlockSet set = new MovingBlockSet();
            PositionableShipsStructure pss = vessel.getStructure();
            pss.getRelativePositions().stream().forEach(f -> {
                BlockPosition vp = pss.getPosition().getRelative(f);
                BlockPosition vp2 = vp.getRelative(addTo);
                set.add(new SetMovingBlock(vp, vp2));
            });
            return move(vessel, set, movement);
        }

    }

}
